package awubot.robot.rat;

public enum BabyRatRole {
    MINER(0),
    RUSHER(1),
    DECOY(2);

    public final int value;

    BabyRatRole(int aValue) {
        this.value = aValue;
    }

    public static BabyRatRole fromValue(int aValue) {
        if (aValue == 2) return DECOY;
        if (aValue == 1) return RUSHER;
        return MINER;
    }
}
