package saga.tf_reengaged.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class TFConfig {
    public static final ForgeConfigSpec COMMON_SPEC;

    // --- EntityTF44 Settings ---
    public static final ForgeConfigSpec.DoubleValue NEFTHYS_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue NEFTHYS_MOVE_SPEED;
    public static final ForgeConfigSpec.DoubleValue NEFTHYS_ATTACK_DAMAGE;

    // --- Animation Settings ---
    public static final ForgeConfigSpec.DoubleValue NEFTHYS_WING_SPEED_FLY;
    public static final ForgeConfigSpec.DoubleValue NEFTHYS_WING_SPEED_IDLE;

    static {
        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        BUILDER.comment("Nefthys (TF44) Settings").push("nefthys");

        NEFTHYS_MAX_HEALTH = BUILDER
                .comment("Max Health of Nefthys")
                .defineInRange("maxHealth", 300.0D, 1.0D, 10000.0D);

        NEFTHYS_MOVE_SPEED = BUILDER
                .comment("Movement speed of Nefthys")
                .defineInRange("moveSpeed", 0.35D, 0.0D, 2.0D);

        NEFTHYS_ATTACK_DAMAGE = BUILDER
                .comment("Attack damage of Nefthys")
                .defineInRange("attackDamage", 15.0D, 0.0D, 1000.0D);

        BUILDER.pop();

        BUILDER.comment("Animation Settings").push("animations");

        NEFTHYS_WING_SPEED_FLY = BUILDER
                .comment("Wing flap speed during movement")
                .defineInRange("wingSpeedFly", 0.25D, 0.0D, 2.0D);

        NEFTHYS_WING_SPEED_IDLE = BUILDER
                .comment("Wing flap speed during idle")
                .defineInRange("wingSpeedIdle", 0.04D, 0.0D, 1.0D);

        BUILDER.pop();

        COMMON_SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }
}
