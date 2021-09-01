package net.eonzenx.spool_ge.config;

import net.eonzenx.spool_ge.utils.mixin.animals.IHappinessEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;

public class Config
{
    public static final class Wings {
        public static WingSettings Leaf = new WingSettings(0.55f);
        public static WingSettings Feather = new WingSettings(0.7f);
        public static WingSettings Hide = new WingSettings(0.85f);
        public static WingSettings Elytra = new WingSettings(1f);
    }

    public static final class Bed {
        public static boolean AFFECT_RESPAWN_ANCHOR = false;
        public static int MAX_BED_RESPAWNS = 1;
    }

    public static final class Animals {
        public static double EDIBLE_FOLLOW_SPEED = 1.0d;
        public static double BREEDABLE_FOLLOW_SPEED = 1.2d;
        public static float BASE_EAT_TIMER = 600;

        public static final class Happiness {
            public static float BASE = 33f;
            public static float MAX = 100f;
            public static float MIN = 0f;
            public static float HOSTILE_BASE_MULTIPLIER = 0f;

            public static float EDIBLE_HAPPINESS = 1f;
            public static float BREEDABLE_HAPPINESS = 3f;
            public static float HOSTILE_FOOD_MULTIPLIER = 0.5f;

            public static float BASE_LOVE_FRACTION = 0.75f;

            public static float calcBaseHappiness(LivingEntity entity) {
                if (entity instanceof HostileEntity) {
                    return BASE * HOSTILE_BASE_MULTIPLIER;
                }

                if (entity instanceof PassiveEntity) {
                    return BASE;
                }

                return BASE;
            }
            public static float calcHappinessRatio(LivingEntity entity) {
                if (entity instanceof IHappinessEntity happinessEntity) {
                    var happiness = happinessEntity.getHappiness();
                    var difference = MAX - MIN;

                    var rawRatio = happiness / difference;
                    var happinessRatio = rawRatio / BASE_LOVE_FRACTION;
                    return happinessRatio;
                }

                return 1f;
            }

            public static int calcEatTimer(LivingEntity entity) {
                return (int) (BASE_EAT_TIMER / calcHappinessRatio(entity));
            }
        }
    }

    public static final class Nbt {
        public static final String HAPPINESS_KEY = "Happiness";
        public static final String EAT_TIMEOUT_KEY = "EatTimeout";
    }
}
