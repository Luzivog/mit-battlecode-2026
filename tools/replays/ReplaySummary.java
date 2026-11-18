import battlecode.schema.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.zip.GZIPInputStream;

public class ReplaySummary {
    public static void main(String[] args) throws Exception {
        for (String path : args) {
            byte[] raw;
            try (InputStream in = new GZIPInputStream(new FileInputStream(path))) { raw = in.readAllBytes(); }
            ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            GameWrapper gw = GameWrapper.getRootAsGameWrapper(bb);
            String[] teams = new String[3];
            String mapName = null; int w=0,h=0;
            int[] lastCheese = new int[3], lastCatDmg = new int[3], lastKings = new int[3], lastBabies = new int[3], maxBabies = new int[3], maxKings = new int[3];
            int[] r500Babies = new int[3], r500Cheese = new int[3];
            System.out.println("### " + Paths.get(path).getFileName());
            for (int i = 0; i < gw.eventsLength(); i++) {
                EventWrapper ew = gw.events(i);
                byte t = ew.eType();
                if (t == Event.GameHeader) {
                    GameHeader gh = (GameHeader) ew.e(new GameHeader());
                    for (int k = 0; k < gh.teamsLength(); k++) { TeamData td = gh.teams(k); teams[td.teamId()] = td.name() + "(" + td.packageName() + ")"; }
                    System.out.println("teams: 1=" + teams[1] + " 2=" + teams[2]);
                } else if (t == Event.MatchHeader) {
                    MatchHeader mh = (MatchHeader) ew.e(new MatchHeader());
                    mapName = mh.map().name(); w = mh.map().size().x(); h = mh.map().size().y();
                    java.util.Arrays.fill(maxBabies,0); java.util.Arrays.fill(maxKings,0);
                } else if (t == Event.Round) {
                    Round r = (Round) ew.e(new Round());
                    for (int k = 0; k < r.teamIdsLength(); k++) {
                        int id = r.teamIds(k);
                        lastCheese[id] = r.teamCheeseTransferred(k); lastCatDmg[id] = r.teamCatDamage(k);
                        lastKings[id] = r.teamAliveRatKings(k); lastBabies[id] = r.teamAliveBabyRats(k);
                        maxBabies[id] = Math.max(maxBabies[id], lastBabies[id]); maxKings[id] = Math.max(maxKings[id], lastKings[id]);
                        if (r.roundId() == 500) { r500Babies[id] = lastBabies[id]; r500Cheese[id] = lastCheese[id]; }
                    }
                } else if (t == Event.MatchFooter) {
                    MatchFooter mf = (MatchFooter) ew.e(new MatchFooter());
                    System.out.printf("  map=%s %dx%d winner=%d(%s) by=%s rounds=%d | end: cheeseXfer %d/%d catDmg %d/%d kings %d/%d babies %d/%d | maxBabies %d/%d maxKings %d/%d%n",
                        mapName, w, h, mf.winner(), teams[mf.winner()] == null ? "?" : teams[mf.winner()].split("\\(")[0], WinType.name(mf.winType()), mf.totalRounds(),
                        lastCheese[1], lastCheese[2], lastCatDmg[1], lastCatDmg[2], lastKings[1], lastKings[2], lastBabies[1], lastBabies[2], maxBabies[1], maxBabies[2], maxKings[1], maxKings[2]);
                }
            }
        }
    }
}
