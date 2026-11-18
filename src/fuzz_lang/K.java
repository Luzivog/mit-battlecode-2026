package fuzz_lang;

// Counters the features report into.
final class K {
    static int caughtCount;
    static int noteAcc;

    static void caught() {
        caughtCount++;
    }

    static void note(int v) {
        noteAcc = noteAcc * 31 + v;
    }
}
