package mods.flammpfeil.slashblade.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.SimpleSlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.recipe.RecipeSerializerRegistry;
import mods.flammpfeil.slashblade.recipe.SlashBladeShapedRecipe;
import mods.flammpfeil.slashblade.recipe.SlashBladeSmithingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
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
    public static final EmiRecipeCategory SLASHBLADE_SHAPED_CATEGORY = new EmiRecipeCategory(
            SlashBlade.prefix("shaped_blade"),
            EmiStack.of(Blocks.CRAFTING_TABLE)
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(SLASHBLADE_SMITHING_CATEGORY);
        registry.addCategory(SLASHBLADE_SHAPED_CATEGORY);

        // 注册SlashBlade锻造配方
        List<SlashBladeSmithingRecipe> smithingRecipes = findRecipesByType(RecipeSerializerRegistry.SLASHBLADE_SMITHING_TYPE.get());
        for (SlashBladeSmithingRecipe recipe : smithingRecipes) {
            registry.addRecipe(new SlashBladeSmithingEmiRecipe(recipe));
        }

        // 注册SlashBlade锻造配方
        List<SlashBladeShapedRecipe> craftingRecipes = findRecipesByType(RecipeSerializerRegistry.SLASHBLADE_SHAPED_TYPE.get());
        for (SlashBladeShapedRecipe recipe : craftingRecipes) {
            System.out.println("SB_EMI_DEBUG: " + recipe.getId());
            registry.addRecipe(new SlashBladeCraftingEmiRecipe(recipe));
        }

        // 添加工作站
        registry.addWorkstation(SLASHBLADE_SMITHING_CATEGORY, EmiStack.of(Blocks.SMITHING_TABLE));
        registry.addWorkstation(SLASHBLADE_SHAPED_CATEGORY, EmiStack.of(Blocks.CRAFTING_TABLE));

    	registry.removeEmiStacks(s->{
    		if(!s.getItemStack().getCapability(ItemSlashBlade.BLADESTATE).isPresent())
    			return false;
    		var state = s.getItemStack().getCapability(ItemSlashBlade.BLADESTATE)
    				.orElseThrow(NullPointerException::new);
    		return (state instanceof SimpleSlashBladeState) || state.isEmpty();
    	});
        
        BladeModelManager.getClientSlashBladeRegistry()
                .forEach(defi -> {
                	var stack = defi.getBlade();
                    registry.addEmiStack(EMISlashBladeStack.of(stack));
                });

        registry.removeRecipes(new ResourceLocation("emi", "/crafting/repairing/slashblade/slashblade"));
    }

    private static <C extends Container, T extends Recipe<C>> List<T> findRecipesByType(RecipeType<T> type) {
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(type);
    }
}