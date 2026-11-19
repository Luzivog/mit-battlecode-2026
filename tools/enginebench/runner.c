// enginebench's runner, the first process inside the sandbox: runner FD CMD ARGS...
// Turns address randomization off, runs CMD as its child, reaps every process of the tree (it is their subreaper,
// so a process that leaves its parent is reaped here too) and writes "MAXRSS_KB EXIT\n" to FD: the largest
// resident set of any process of the tree, and CMD's exit code (128 + signal when killed). FD is closed in CMD and
// this process is not dumpable, so the code under test cannot write the reading.
#include <errno.h>
#include <stdio.h>
#include <string.h>
#include <sys/personality.h>
#include <sys/prctl.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <unistd.h>

int main(int argc, char** argv) {
    if (argc < 3) {
        fprintf(stderr, "usage: runner FD CMD [ARGS...]\n");
        return 2;
    }
    int fd = 0;
    for (const char* p = argv[1]; *p; p++) fd = fd * 10 + (*p - '0');
    if (personality(ADDR_NO_RANDOMIZE) == -1) { perror("runner: personality"); return 2; }
    prctl(PR_SET_DUMPABLE, 0);
    if (prctl(PR_SET_CHILD_SUBREAPER, 1) != 0) { perror("runner: subreaper"); return 2; }
    pid_t child = fork();
    if (child < 0) { perror("runner: fork"); return 2; }
    if (child == 0) {
        close(fd);
        execv(argv[2], argv + 2);
        perror("runner: exec");
        _exit(127);
    }
    long maxrss = 0;
    int code = -1;
    for (;;) {
        int st;
        struct rusage ru;
        pid_t pid = wait4(-1, &st, 0, &ru);
        if (pid < 0) {
            if (errno == EINTR) continue;
            break;  // ECHILD: the whole tree is gone
        }
        if (ru.ru_maxrss > maxrss) maxrss = ru.ru_maxrss;
        if (pid == child) code = WIFEXITED(st) ? WEXITSTATUS(st) : 128 + WTERMSIG(st);
    }
    dprintf(fd, "%ld %d\n", maxrss, code);
    return 0;
}
