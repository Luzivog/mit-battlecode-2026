// Each robot runs on its own stack. A turn is a switch into the robot's stack; the robot switches back when it
// yields (Clock.yield, out of bytecodes) or ends. This follows SandboxedRobotPlayer and PlayerControlProvider:
// the first turn starts with RobotMonitor.reactivate(), a robot whose run() exits (normally or by any Throwable)
// is terminated, and a robot killed by the game during its own turn finishes that turn until its next
// incrementBytecodes.
#include "sched.h"

#include <sys/mman.h>

#include <cstdio>
#include <unordered_map>

#include "gc.h"
#include "gc/gc_mark.h"

extern "C" void bcx_swap(void** save_sp, void* new_sp);
__asm__(
    ".text\n.globl bcx_swap\n.type bcx_swap,@function\nbcx_swap:\n"
    "  pushq %rbp\n  pushq %rbx\n  pushq %r12\n  pushq %r13\n  pushq %r14\n  pushq %r15\n"
    "  movq %rsp, (%rdi)\n  movq %rsi, %rsp\n"
    "  popq %r15\n  popq %r14\n  popq %r13\n  popq %r12\n  popq %rbx\n  popq %rbp\n  ret\n"
    ".size bcx_swap,.-bcx_swap\n");

static const size_t STACK_SIZE = 16u << 20;  // reserved; pages are touched only as used
static const size_t GUARD = 64u << 10;

struct Robot {
    jint id;
    const ModuleInfo* mod;
    void* statics;
    Obj* rc;
    Monitor mon;
    bool started, terminated, killed, running;
    void* sp;
    char* stack_lo;
    char* stack_hi;
    IdHashTable ih;    // ObjectMethods.codes: object -> code, strong (see DESIGN.md)
    KCache* enums;     // Class.getEnumConstantsShared, per robot as each robot has its own classes
};

static std::unordered_map<jint, Robot*> g_robots;   // robots live in GC-uncollectable memory
static Robot* g_cur;
static void* g_main_sp;
static GC_stack_base g_main_base;
static GC_push_other_roots_proc g_prev_push;

static void GC_CALLBACK push_stacks() {
    if (g_prev_push) g_prev_push();
    if (g_cur) GC_push_all_eager(g_main_sp, g_main_base.mem_base);
    for (auto& kv : g_robots) {
        Robot* r = kv.second;
        if (!r || r == g_cur || !r->started || r->terminated) continue;
        GC_push_all_eager(r->sp, r->stack_hi);
    }
}

void sched_init() {
    extern FILE* g_bc_trace;
    if (const char* f = getenv("BCX_TRACE")) g_bc_trace = fopen(f, "w");
    GC_get_my_stackbottom(&g_main_base);
    g_prev_push = GC_get_push_other_roots();
    GC_set_push_other_roots(push_stacks);
}

static void free_robot(Robot* r) {
    if (r->stack_lo) munmap(r->stack_lo, STACK_SIZE);
    r->stack_lo = r->stack_hi = nullptr;
    r->statics = nullptr;
    r->rc = nullptr;
    r->ih.keys = nullptr;
    r->ih.vals = nullptr;
    r->enums = nullptr;
}

static void robot_main();
static jint g_crash_id = -1;  // the robot whose run() just ended in a crash (sched_crashed)

static void enter(Robot* r) {
    g_cur = r;
    r->running = true;
    g_mon = r->mon;
    extern FILE* g_bc_trace;
    g_mon_slow = g_mon.should_die || g_mon.debug_level != 0 || g_mon.to_remove != 0 || g_bc_trace;
    g_S = r->statics;
    GC_stack_base sb;
    sb.mem_base = r->stack_hi;
    GC_set_stackbottom(nullptr, &sb);
    bcx_swap(&g_main_sp, r->sp);
    // back on the engine's stack
    GC_set_stackbottom(nullptr, &g_main_base);
    r->mon = g_mon;
    r->running = false;
    g_cur = nullptr;
    g_S = nullptr;
    if (r->terminated) free_robot(r);
}

static void leave() {
    Robot* r = g_cur;
    bcx_swap(&r->sp, g_main_sp);
}

void sched_pause() { leave(); }

void sched_robot_die() {
    Robot* r = g_cur;
    r->terminated = true;
    g_exc = nullptr;
    leave();
    rt_fatal("a dead robot was resumed");
}

static void robot_main() {
    Robot* r = g_cur;
    // the robot thread paused right after RobotMonitor.init; resuming runs reactivate()
    if (g_mon.should_die) sched_robot_die();
    if (g_mon.left < 0) g_mon.left += g_mon.limit;
    else g_mon.left = g_mon.limit;
    if (r->mod->init_robot_player) r->mod->init_robot_player();
    bool ran = !g_exc;
    if (ran) r->mod->run(r->rc);
    // SandboxedRobotPlayer reports a Throwable out of run() other than RobotDeathException ("Stack trace") and a
    // return ("froze"); a failed RobotPlayer initializer escapes its thread unreported
    if (ran && (!g_exc || strcmp(g_exc->klass->name, "battlecode/instrumenter/RobotDeathException") != 0)) g_crash_id = r->id;
    if (g_exc && getenv("BCX_ROBOT_ERRORS")) {
        extern void rt_describe_exception(Obj* e, char* buf, size_t cap);
        char b[1024];
        rt_describe_exception(g_exc, b, sizeof b);
        fprintf(stderr, "[robot %d] run() ended by %s\n", r->id, b);
    }
    g_exc = nullptr;
    r->terminated = true;
    leave();
    rt_fatal("a finished robot was resumed");
}

void sched_spawn(jint id, Obj* pkg, Obj* rc) {
    char name[256];
    rt_string_utf8(pkg, name, sizeof name);
    const ModuleInfo* mod = nullptr;
    for (const ModuleInfo* m = g_modules; m->name; m++)
        if (strcmp(m->name, name) == 0) mod = m;
    if (!mod) rt_fatal("no translated bot named %s in this build", name);
    Robot* r = (Robot*)GC_MALLOC_UNCOLLECTABLE(sizeof(Robot));
    memset(r, 0, sizeof *r);
    r->id = id;
    r->mod = mod;
    r->statics = GC_MALLOC(mod->statics_size);
    r->rc = rc;
    r->mon.seed = id;
    r->ih.last = -1;
    char* lo = (char*)mmap(nullptr, STACK_SIZE, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS | MAP_NORESERVE, -1, 0);
    if (lo == MAP_FAILED) rt_fatal("cannot map a robot stack");
    mprotect(lo, GUARD, PROT_NONE);
    r->stack_lo = lo;
    r->stack_hi = lo + STACK_SIZE;
    void** sp = (void**)r->stack_hi;
    *--sp = nullptr;                // robot_main never returns
    *--sp = (void*)&robot_main;
    for (int i = 0; i < 6; i++) *--sp = nullptr;
    r->sp = sp;
    auto it = g_robots.find(id);
    if (it != g_robots.end() && it->second) rt_fatal("robot %d spawned twice", id);
    g_robots[id] = r;
}

static Robot* find(jint id) {
    auto it = g_robots.find(id);
    return it == g_robots.end() ? nullptr : it->second;
}

void sched_run(jint id, jint limit) {
    Robot* r = find(id);
    if (!r || r->killed || r->terminated) return;
    r->mon.limit = limit;   // setBytecodeLimit before step()
    r->started = true;
    enter(r);
}

void sched_kill(jint id) {
    Robot* r = find(id);
    if (!r || r->killed) return;
    r->killed = true;
    if (r->running) {
        // terminate() from inside the robot's own turn: it dies at its next incrementBytecodes
        g_mon.should_die = true;
        g_mon_slow = true;
    } else {
        r->terminated = true;
        free_robot(r);
    }
}

jint sched_bytecodes_used(jint id) {
    Robot* r = find(id);
    if (!r || r->killed) return 0;
    return r->mon.limit - r->mon.left;
}

jboolean sched_crashed(jint id) {
    bool c = g_crash_id == id;
    if (c) g_crash_id = -1;
    return c;
}

jboolean sched_terminated(jint id) {
    Robot* r = find(id);
    if (!r || r->killed) return 1;
    return r->terminated;
}

void sched_match_ended() {
    extern FILE* g_bc_trace;
    if (g_bc_trace) fflush(g_bc_trace);
    for (auto& kv : g_robots) {
        Robot* r = kv.second;
        if (!r) continue;
        if (!r->running) free_robot(r);
        GC_FREE(r);
    }
    g_robots.clear();
}

static jint next_code(IdHashTable* h) { return ++h->last; }
jint sched_identity_hash(Obj* o) { return idhash_get(&g_cur->ih, o, next_code); }
Obj** sched_enum_cache(Klass* k) { return kcache_slot(&g_cur->enums, k, false); }
