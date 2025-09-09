package mods.flammpfeil.slashblade.compat.emi;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.runtime.EmiLog;
import mods.flammpfeil.slashblade.recipe.SlashBladeShapedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.Iterator;
import java.util.List;

public class SlashBladeCraftingEmiRecipe extends EmiCraftingRecipe {

    public SlashBladeCraftingEmiRecipe(SlashBladeShapedRecipe recipe) {
        super(
                SlashBladeCraftingEmiRecipe.padIngredients(recipe),
                Minecraft.getInstance().level == null ?
                        EmiStack.EMPTY :
                        SlashBladeCraftingEmiRecipe.getRecipeOutput(recipe, Minecraft.getInstance().level.registryAccess()),
                EmiPort.getId(recipe),
                false
        );
        setRemainders(this.input, recipe);
    }

    private static EmiStack getRecipeOutput(SlashBladeShapedRecipe recipe, RegistryAccess access) {
        CraftingContainer inputs = EmiUtil.getCraftingInventory();
        List<EmiIngredient> ingredients = SlashBladeCraftingEmiRecipe.padIngredients(recipe);
        for (int i = 0; i < ingredients.size(); i++) {
            inputs.setItem(i, ingredients.get(i).getEmiStacks().get(0).getItemStack().copy());
        }
        return EMISlashBladeStack.of(recipe.assemble(inputs, access));
    }

    public static void setRemainders(List<EmiIngredient> input, CraftingRecipe recipe) {
        try {
            TransientCraftingContainer inv = EmiUtil.getCraftingInventory();
            for (int i = 0; i < input.size(); ++i) {
                if (!input.get(i).isEmpty()) {
                    for (int j = 0; j < input.size(); ++j) {
                        if (j != i && !(input.get(j)).isEmpty()) {
                            inv.setItem(
                                    j,
                                    input.get(j).getEmiStacks().get(0).getItemStack().copy()
                            );
                        }
                    }

                    List<EmiStack> stacks = input.get(i).getEmiStacks();
                    Iterator var5 = stacks.iterator();

                    while (var5.hasNext()) {
                        EmiStack stack = (ItemEmiStack) var5.next();
                        inv.setItem(i, stack.getItemStack().copy());
                        ItemStack remainder = recipe.getRemainingItems(inv).get(i);
                        if (!remainder.isEmpty()) {
                            stack.setRemainder(EMISlashBladeStack.of(remainder));
                        }
                    }

                    inv.clearContent();
                }
            }
        } catch (Exception var8) {
            Exception e = var8;
            EmiLog.error("Exception thrown setting remainders for " + String.valueOf(EmiPort.getId(recipe)), e);
        }

    }

    private static List<EmiIngredient> padIngredients(ShapedRecipe recipe) {
        List<EmiIngredient> list = Lists.newArrayList();
        int i = 0;

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                if (x < recipe.getWidth() && y < recipe.getHeight() && i < recipe.getIngredients().size()) {
                    list.add(
                            EMISlashBladeStack.of(recipe.getIngredients().get(i++))
                    );
                } else {
                    list.add(EmiStack.EMPTY);
                }
            }
        }

        return list;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMICompat.SLASHBLADE_SHAPED_CATEGORY;
    }
}
