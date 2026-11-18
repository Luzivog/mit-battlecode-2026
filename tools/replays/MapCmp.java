import battlecode.world.*;
import java.io.*;
import java.nio.file.*;
public class MapCmp {
    public static void main(String[] a) throws Exception {
        LiveMap x = GameMapIO.Serial.deserialize(Files.readAllBytes(Paths.get(a[0])), false);
        LiveMap y = GameMapIO.Serial.deserialize(Files.readAllBytes(Paths.get(a[1])), false);
        System.out.println(x.getMapName()+" size " + (x.getWidth()==y.getWidth()&&x.getHeight()==y.getHeight())
          + " walls " + java.util.Arrays.equals(x.getWallArray(), y.getWallArray())
          + " dirt " + java.util.Arrays.equals(x.getDirtArray(), y.getDirtArray())
          + " mines " + java.util.Arrays.equals(x.getCheeseMineArray(), y.getCheeseMineArray())
          + " cheese " + java.util.Arrays.equals(x.getCheeseArray(), y.getCheeseArray())
          + " seed " + (x.getSeed()==y.getSeed()) + " sym " + (x.getSymmetry()==y.getSymmetry())
          + " bodies " + java.util.Arrays.toString(x.getInitialBodies()).equals(java.util.Arrays.toString(y.getInitialBodies())));
    }
}
