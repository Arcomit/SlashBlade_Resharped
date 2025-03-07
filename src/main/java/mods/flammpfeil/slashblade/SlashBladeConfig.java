package mods.flammpfeil.slashblade;

import net.minecraftforge.common.ForgeConfigSpec;

public class SlashBladeConfig {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.BooleanValue HUNGER_CAN_REPAIR;
    public static ForgeConfigSpec.IntValue MAX_PROUD_SOUL_GOT;
    public static ForgeConfigSpec.IntValue SUMMON_SWORD_COST;
    public static ForgeConfigSpec.IntValue SUMMON_SWORD_ART_COST;
    public static ForgeConfigSpec.DoubleValue BEWITCHED_HUNGER_EXHAUSTION;
    public static ForgeConfigSpec.BooleanValue PVP_ENABLE;
    public static ForgeConfigSpec.BooleanValue FRIENDLY_ENABLE;
    public static ForgeConfigSpec.DoubleValue SABIGATANA_SPAWN_CHANCE;
    public static ForgeConfigSpec.DoubleValue BROKEN_SABIGATANA_SPAWN_CHANCE;
    public static ForgeConfigSpec.IntValue REFINE_LEVEL_COST;

    public static ForgeConfigSpec.DoubleValue SLASHBLADE_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue REFINE_DAMAGE_MULTIPLIER;

    
    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push("general");

        SABIGATANA_SPAWN_CHANCE = COMMON_BUILDER.comment("Determining the spawn chance of sabigatana.")
                .defineInRange("sabigatana_spawn_chance", 0.05D, 0.0D, 1.0D);

        BROKEN_SABIGATANA_SPAWN_CHANCE = COMMON_BUILDER.comment("Determining the spawn chance of a broken sabigatana.")
                .defineInRange("broken_sabigatana_spawn_chance", 0.15D, 0.0D, 1.0D);

        HUNGER_CAN_REPAIR = COMMON_BUILDER.comment("Determines whether to make hunger effect repair slashblade.",
                "If enable, if player has hunger effect, your slashblade in hotbar will be repaired, cost player's hunger.")
                .define("hunger_can_repair", true);
        PVP_ENABLE = COMMON_BUILDER.comment("Determines whether to enable slashblade's PVP.",
                "If enable, player can attack player with SlashBlade.").define("pvp_enable", false);
        FRIENDLY_ENABLE = COMMON_BUILDER.comment("Determines whether to enable slashblade's friendly fire.",
                "If enable, player can attack friendly entity with SlashBlade.").define("friendly_enable", false);
        
        REFINE_LEVEL_COST = COMMON_BUILDER.comment("Determining the level cost for refine a slashblade.")
                .defineInRange("refine_level_cost", 1, 1, Integer.MAX_VALUE);
        
        SUMMON_SWORD_COST = COMMON_BUILDER.comment("Determining the proud soul cost for single summon mirage blade.")
                .defineInRange("summon_sword_cost", 2, 1, Integer.MAX_VALUE);
        SUMMON_SWORD_ART_COST = COMMON_BUILDER.comment("Determining the proud soul cost for summon blade arts.")
                .defineInRange("summon_blade_art_cost", 20, 1, Integer.MAX_VALUE);

        MAX_PROUD_SOUL_GOT = COMMON_BUILDER.comment("Determining the max proud soul count for single mobs kill.")
                .defineInRange("max_proud_soul_got", 100, 1, Integer.MAX_VALUE);
        BEWITCHED_HUNGER_EXHAUSTION = COMMON_BUILDER
                .comment("Determining the base exhaustion for slashblade's self-repair.")
                .defineInRange("bewitched_hunger_exhaustion", 0.05D, 0.0001D, Double.MAX_VALUE);

        SLASHBLADE_DAMAGE_MULTIPLIER = COMMON_BUILDER.comment("Blade Damage: Base Damage × Multiplier.[Default: 1.0D]")
                .defineInRange("slashblade_damage_multiplier", 1.0D, 0.0D, 1024.0D);

        REFINE_DAMAGE_MULTIPLIER = COMMON_BUILDER.comment("S-Rank Bonus: Each Refine × Multiplier'value Damage.[Default: 0.785D]")
                .defineInRange("refine_damage_multiplier", 0.785D, 0.0D, 1024.0D);

        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
