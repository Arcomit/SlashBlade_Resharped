package mods.flammpfeil.slashblade.init;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ObjectHolder;

public class SBItems {
    // TODO: 需要改为DeferredRegister形式的注册

    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul")
    public static Item proudsoul;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_ingot")
    public static Item proudsoul_ingot;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_tiny")
    public static Item proudsoul_tiny;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_sphere")
    public static Item proudsoul_sphere;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_crystal")
    public static Item proudsoul_crystal;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":proudsoul_trapezohedron")
    public static Item proudsoul_trapezohedron;

    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade_wood")
    public static Item slashblade_wood;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade_bamboo")
    public static Item slashblade_bamboo;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade_silverbamboo")
    public static Item slashblade_silverbamboo;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade_white")
    public static Item slashblade_white;

    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":slashblade")
    public static Item slashblade;

    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_1")
    public static Item bladestand_1;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_2")
    public static Item bladestand_2;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_v")
    public static Item bladestand_v;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_s")
    public static Item bladestand_s;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_1w")
    public static Item bladestand_1w;
    @ObjectHolder(registryName = "minecraft:item", value = SlashBlade.MODID + ":bladestand_2w")
    public static Item bladestand_2w;
}
