package bcx.subst;

import battlecode.world.LiveMap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/** Patch of battlecode.world.GameMapIO: maps are read natively (the jar's bundled maps from a directory). */
public final class GameMapIO_p {
    public static LiveMap loadMap(String mapName, File mapDir, boolean teamsReversed) throws IOException {
        final LiveMap result;
        String path = mapDir.getPath() + "/" + mapName + ".map26";
        if (bcx.NativeIO.exists(path)) {
            result = loadMap(new ByteArrayInputStream(bcx.NativeIO.readFile(path)), teamsReversed);
        } else {
            String bundled = bcx.NativeIO.bundledMaps() + "/" + mapName + ".map26";
            if (!bcx.NativeIO.exists(bundled)) {
                throw new IOException("Can't load map: " + mapName + " from dir " + mapDir + " or default maps.");
            }
            result = loadMap(new ByteArrayInputStream(bcx.NativeIO.readFile(bundled)), teamsReversed);
        }
        if (!result.getMapName().equals(mapName)) {
            throw new IOException("Invalid map: name (" + result.getMapName()
                    + ") does not match filename (" + mapName + ".map26)");
        }
        return result;
    }

    @Orig
    public static LiveMap loadMap(InputStream stream, boolean teamsReversed) throws IOException { return null; }
}
