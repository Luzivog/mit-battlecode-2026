package challenger;

import battlecode.common.MapLocation;

import static challenger.util.Util.encode2x2;

// Tune.CM_MINE_BOOK (a book switch, 0 in main): every baby rat starts with the map's mines from the book (Book.CM_MINES,
// every mine of the map) among its known mines, so the explore-only opening ends at once and its trips draw from the
// whole list with main's distance weighting (robot/rat/MinerStrategy.pickSeenMine). Kings keep what they see: their
// mine choice and the formation's mine count stay main's (docs/theory/cm8-mine-book.md).
public class MineBook {
    public static void load() {
        MapLocation[] mines = Book.CM_MINES;
        if (mines == null || Game.isRatKing) return;
        for (int i = mines.length; --i >= 0; ) Game.knownCheeseMines.add(encode2x2(mines[i]));
    }
}
