package opp_gst.RatKingStateUpdaters;

import opp_gst.Behavior;
import opp_gst.StateUpdater;
import opp_gst.RatKingBehaviors.RKFlee;
import opp_gst.RatKingBehaviors.RKHeadquarter;

import static opp_gst.RatKing.nearestCat;
import static opp_gst.RatKing.nearestEnemy;
import static opp_gst.RatKing.inDistress;

public class SimpleRKStateUpdater implements StateUpdater {
    public static final int HQ_CHEESE_THRESHOLD = 1000;

    @Override
    public Behavior decideBehavior() {
        // if (rc.getGlobalCheese() > HQ_CHEESE_THRESHOLD) {
        //     return RKHeadquarter.getInstance();
        // }

        // return RKExplore.getInstance();
        if(inDistress) {
            return RKFlee.getInstance();
        }

        return RKHeadquarter.getInstance();
    }
}
