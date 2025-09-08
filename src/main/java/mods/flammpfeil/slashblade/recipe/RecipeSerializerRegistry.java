package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
            .create(ForgeRegistries.RECIPE_TYPES, SlashBlade.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, SlashBlade.MODID);

    public static final RegistryObject<RecipeSerializer<?>> SLASHBLADE_SHAPED = RECIPE_SERIALIZER
            .register("shaped_blade", () -> SlashBladeShapedRecipe.SERIALIZER);
    
    public static final RegistryObject<RecipeSerializer<?>> PROUDSOUL_RECIPE = RECIPE_SERIALIZER
            .register("proudsoul", () -> ProudsoulShapelessRecipe.SERIALIZER);
    
    public static final RegistryObject<RecipeSerializer<?>> SLASHBLADE_SMITHING = RECIPE_SERIALIZER
            .register("slashblade_smithing", () -> SlashBladeSmithingRecipe.SERIALIZER);

    public static final RegistryObject<RecipeType<SlashBladeShapedRecipe>> SLASHBLADE_SHAPED_TYPE = RECIPE_TYPES
            .register("shaped_blade", () -> recipeType("shaped_blade"));

    public static final RegistryObject<RecipeType<SlashBladeSmithingRecipe>> SLASHBLADE_SMITHING_TYPE = RECIPE_TYPES
            .register("slashblade_smithing", () -> recipeType("slashblade_smithing"));

    private static <T extends Recipe<?>> RecipeType<T> recipeType(String name) {
        return new RecipeType<T>() {
            public String toString() {
                return new ResourceLocation(SlashBlade.MODID, name).toString();
            }
        };
    }
}
