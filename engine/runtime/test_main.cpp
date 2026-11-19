// Runs a translated program's first --entry, a static void main(String[]), for translator tests.
#include <cstdio>

#include "gc.h"
#include "sched.h"
#include "rt.h"

void rt_link_arrays();
void rt_init_strings();
void rt_init_core();
extern void* const g_entry_fns[];
extern void (*const g_entry_inits[])();

int main() {
    GC_set_all_interior_pointers(1);
    GC_INIT();
    sched_init();
    rt_link_arrays();
    rt_init_strings();
    rt_init_core();
    if (g_entry_inits[0]) g_entry_inits[0]();
    if (!g_exc) ((void (*)(Obj*))g_entry_fns[0])(nullptr);
    if (g_exc) {
        char b[256];
        fprintf(stderr, "uncaught exception: %s\n", g_exc->klass->name);
        return 1;
    }
    return 0;
}
