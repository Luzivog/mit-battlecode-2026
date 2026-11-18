package opp_gst.Communication;

import static opp_gst.SymmetryManager.*;

public record SymmetrySqueakInfo(int symmetry) implements SqueakInfo{

    @Override
    public String toString() {
        return "SymmetrySqueakInfo{" +
                "symmetry=" + symmetry +
                '}';
    }
}
