package bcx.subst;

import java.util.List;

/** Patch of jdk.internal.util.Preconditions: the same messages without java.util.Formatter. */
public final class Preconditions_p {
    private static String outOfBoundsMessage(String checkKind, List<? extends Number> args) {
        if (checkKind == null && args == null) return "Range check failed";
        else if (checkKind == null) return "Range check failed: " + args;
        else if (args == null) return "Range check failed: " + checkKind;
        int argSize = 0;
        switch (checkKind) {
            case "checkIndex": argSize = 2; break;
            case "checkFromToIndex":
            case "checkFromIndexSize": argSize = 3; break;
            default:
        }
        switch ((args.size() != argSize) ? "" : checkKind) {
            case "checkIndex":
                return "Index " + args.get(0) + " out of bounds for length " + args.get(1);
            case "checkFromToIndex":
                return "Range [" + args.get(0) + ", " + args.get(1) + ") out of bounds for length " + args.get(2);
            case "checkFromIndexSize":
                return "Range [" + args.get(0) + ", " + args.get(0) + " + " + args.get(1) + ") out of bounds for length " + args.get(2);
            default:
                return "Range check failed: " + checkKind + " " + args;
        }
    }
}
