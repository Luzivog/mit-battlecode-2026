package experiment.robot.rat;

public enum BabyRatRole {
    MINER(0),
    RUSH(1),
    DEFENDER(2);

    public final int value;

    BabyRatRole(int value) {
        this.value = value;
    }

    public static BabyRatRole fromValue(int value) {
        if (value == RUSH.value) return RUSH;
        if (value == DEFENDER.value) return DEFENDER;
        return MINER;
    }
}
