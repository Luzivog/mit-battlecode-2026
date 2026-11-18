import battlecode.schema.*;
import battlecode.world.*;
import java.io.*;
import java.nio.*;
import java.util.zip.GZIPInputStream;
// usage: java MapExtract <outDir> replay.bc26...  -> writes every match map as <name>.map26
public class MapExtract {
    public static void main(String[] args) throws Exception {
        File out = new File(args[0]); out.mkdirs();
        for (int a = 1; a < args.length; a++) {
            byte[] raw; try (InputStream in = new GZIPInputStream(new FileInputStream(args[a]))) { raw = in.readAllBytes(); }
            GameWrapper gw = GameWrapper.getRootAsGameWrapper(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN));
            for (int i = 0; i < gw.eventsLength(); i++) {
                EventWrapper ew = gw.events(i);
                if (ew.eType() != Event.MatchHeader) continue;
                MatchHeader mh = (MatchHeader) ew.e(new MatchHeader());
                LiveMap lm = GameMapIO.Serial.deserialize(mh.map(), false);
                File f = new File(out, lm.getMapName() + ".map26");
                if (!f.exists()) { GameMapIO.writeMap(lm, out); System.out.println("wrote " + f); }
            }
        }
    }
}
