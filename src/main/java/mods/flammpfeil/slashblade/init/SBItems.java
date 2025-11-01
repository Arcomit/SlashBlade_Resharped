package mods.flammpfeil.slashblade.init;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("NotNullFieldNotInitialized")
public class SBItems {
    // TODO: 需要改为DeferredRegister形式的注册

    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul")
    @NotNull
    public static Item proudsoul;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_ingot")
    @NotNull
    public static Item proudsoul_ingot;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_tiny")
    @NotNull
    public static Item proudsoul_tiny;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_sphere")
    @NotNull
    public static Item proudsoul_sphere;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_crystal")
    @NotNull
    public static Item proudsoul_crystal;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_trapezohedron")
    @NotNull
    public static Item proudsoul_trapezohedron;

    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade_wood")
    @NotNull
    public static Item slashblade_wood;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade_bamboo")
    @NotNull
    public static Item slashblade_bamboo;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade_silverbamboo")
    @NotNull
    public static Item slashblade_silverbamboo;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade_white")
    @NotNull
    public static Item slashblade_white;

    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade")
    @NotNull
    public static Item slashblade;

    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_1")
    @NotNull
    public static Item bladestand_1;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_2")
    @NotNull
    public static Item bladestand_2;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_v")
    @NotNull
    public static Item bladestand_v;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_s")
    @NotNull
    public static Item bladestand_s;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_1w")
    @NotNull
    public static Item bladestand_1w;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_2w")
    @NotNull
    public static Item bladestand_2w;
}
