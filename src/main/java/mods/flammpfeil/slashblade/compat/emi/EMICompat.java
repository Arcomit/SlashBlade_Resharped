package mods.flammpfeil.slashblade.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.recipe.RecipeSerializerRegistry;
import mods.flammpfeil.slashblade.recipe.SlashBladeSmithingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

@EmiEntrypoint
public class EMICompat implements EmiPlugin {

    public static final EmiRecipeCategory SLASHBLADE_SMITHING_CATEGORY = new EmiRecipeCategory(
            SlashBlade.prefix("slashblade_smithing"),
            EmiStack.of(Blocks.SMITHING_TABLE)
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(SLASHBLADE_SMITHING_CATEGORY);

        // 注册SlashBlade锻造配方
        List<SlashBladeSmithingRecipe> smithingRecipes = findRecipesByType(RecipeSerializerRegistry.SLASHBLADE_SMITHING_TYPE.get());
        for (SlashBladeSmithingRecipe recipe : smithingRecipes) {
            registry.addRecipe(new SlashBladeSmithingEmiRecipe(recipe));
            System.out.println("EMI_DEBUG: " + recipe.getId());
        }

        // 添加工作站
        registry.addWorkstation(SLASHBLADE_SMITHING_CATEGORY, EmiStack.of(Blocks.SMITHING_TABLE));
    }

    private static <C extends Container, T extends Recipe<C>> List<T> findRecipesByType(RecipeType<T> type) {
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(type);
    }
}