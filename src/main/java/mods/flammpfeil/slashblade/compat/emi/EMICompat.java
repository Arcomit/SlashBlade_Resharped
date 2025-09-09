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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
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
        HashSet<ResourceLocation> vanillaSmithing = new HashSet<>();
        List<SlashBladeSmithingRecipe> smithingRecipes = findRecipesByType(RecipeType.SMITHING).stream()
                .filter(r -> r instanceof SlashBladeSmithingRecipe).map(r -> (SlashBladeSmithingRecipe) r).toList();
        for (SlashBladeSmithingRecipe recipe : smithingRecipes) {
            registry.addRecipe(new SlashBladeSmithingEmiRecipe(recipe));
            vanillaSmithing.add(recipe.getId());
        }
        registry.removeRecipes(emiRecipe -> {
            return vanillaSmithing.contains(emiRecipe.getId()) && !(emiRecipe instanceof SlashBladeSmithingEmiRecipe);
        });

        // 注册SlashBlade锻造配方
        HashSet<ResourceLocation> vanillaCrafting = new HashSet<>();
        List<SlashBladeShapedRecipe> craftingRecipes = findRecipesByType(RecipeType.CRAFTING).stream()
                .filter(r -> r instanceof SlashBladeShapedRecipe).map(r -> (SlashBladeShapedRecipe) r).toList();;
        for (SlashBladeShapedRecipe recipe : craftingRecipes) {
            registry.addRecipe(new SlashBladeCraftingEmiRecipe(recipe));
            vanillaCrafting.add(recipe.getId());
        }
        registry.removeRecipes(emiRecipe -> {
            return vanillaCrafting.contains(emiRecipe.getId()) && !(emiRecipe instanceof SlashBladeCraftingEmiRecipe);
        });

        // 添加工作站
        registry.addWorkstation(SLASHBLADE_SMITHING_CATEGORY, EmiStack.of(Blocks.SMITHING_TABLE));
        registry.addWorkstation(SLASHBLADE_SHAPED_CATEGORY, EmiStack.of(Blocks.CRAFTING_TABLE));

        registry.removeEmiStacks(emiStack -> {
            ItemStack stack = emiStack.getItemStack();
            if (!(stack.getItem() instanceof ItemSlashBlade)) return false;
            var optional = stack.getCapability(ItemSlashBlade.BLADESTATE);
            if(!optional.isPresent()) return false;
            var state = optional.orElseThrow(NullPointerException::new);
            return !(state instanceof SimpleSlashBladeState) && state.isEmpty();
        });

        BladeModelManager.getClientSlashBladeRegistry()
                .forEach(def -> {
                    var stack = def.getBlade();
                    System.out.println("EMIDEBUG: " + stack.getDisplayName());
                    registry.addEmiStack(EMISlashBladeStack.of(stack));
                });

        registry.removeRecipes(ResourceLocation.parse("emi:/crafting/repairing/slashblade/slashblade"));
    }

    private static <C extends Container, T extends Recipe<C>> List<T> findRecipesByType(RecipeType<T> type) {
        return Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(type);
    }
}