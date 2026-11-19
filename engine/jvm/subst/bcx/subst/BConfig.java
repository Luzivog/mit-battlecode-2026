package bcx.subst;

import java.util.HashMap;

/** battlecode.server.Config for the native engine (renamed): string properties, set by NativeMain. */
public class BConfig {
    private static BConfig globalConfig = new BConfig(new String[0]);
    private final HashMap<String, String> properties = new HashMap<>();

    public BConfig(String[] args) {}

    public static void setGlobalConfig(BConfig config) { globalConfig = config; }
    public static BConfig getGlobalConfig() { return globalConfig; }
    public String get(String key) { return properties.get(key); }
    public void set(String key, String value) { properties.put(key, value); }
    public int getInt(String key) { return Integer.parseInt(get(key)); }
    public boolean getBoolean(String key) { return Boolean.parseBoolean(get(key)); }
    public void setBoolean(String key, boolean value) { set(key, String.valueOf(value)); }
    public String[] getKeys() { return properties.keySet().toArray(new String[0]); }
}
