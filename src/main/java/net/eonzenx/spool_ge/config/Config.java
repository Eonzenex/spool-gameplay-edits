package net.eonzenx.spool_ge.config;

public class Config
{
    public final static class Wings {
        public static WingSettings Leaf = new WingSettings(0.55f);
        public static WingSettings Feather = new WingSettings(0.7f);
        public static WingSettings Hide = new WingSettings(0.85f);
        public static WingSettings Elytra = new WingSettings(1f);
    }

    public final static class Bed {
        public static boolean AFFECT_RESPAWN_ANCHOR = false;
        public static int MAX_BED_RESPAWNS = 1;
    }
}
