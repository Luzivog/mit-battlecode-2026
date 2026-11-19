package bcx.subst;

import java.io.File;

/** Patch of battlecode.server.GameMaker: an empty save path writes nothing (the native engine without a replay). */
public final class GameMaker_p {
    public void writeGame(File saveFile) {
        if (saveFile == null) throw new RuntimeException("Null file provided to writeGame");
        if (saveFile.getPath().isEmpty()) return;
        bcx.NativeIO.writeFile(saveFile.getPath(), toBytes());
    }

    @Orig
    public byte[] toBytes() { return null; }
}
