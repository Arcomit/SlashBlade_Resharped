package mods.flammpfeil.slashblade.compat.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.registry.EmiTags;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;

    public class EMISlashBladeStack extends ItemEmiStack {
    private CompoundTag capNBT;
    public EMISlashBladeStack(ItemStack stack) {
        super(stack);
        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
            this.capNBT = s.serializeNBT();
        });
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = super.getItemStack();
        if (this.capNBT != null) {
            stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.deserializeNBT(this.capNBT);
            });
        }
        return stack;
    }

    public static EmiStack of(ItemStack stack) {
        if (stack.isEmpty()) {
            return EmiStack.EMPTY;
        }
        return new EMISlashBladeStack(stack);
    }

    public static EmiIngredient of(Ingredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) {
            return EmiStack.EMPTY;
        }
        ItemStack[] stacks = ingredient.getItems();
        int amount = 1;
        if (stacks.length != 0) {
            amount = stacks[0].getCount();
            for (int i = 1; i < stacks.length; i++) {
                if (stacks[i].getCount() != amount) {
                    amount = 1;
                    break;
                }
            }
        }
        return of(ingredient, amount);
    }

    public static EmiIngredient of(Ingredient ingredient, long amount) {
        if (ingredient == null || ingredient.isEmpty()) {
            return EmiStack.EMPTY;
        }
        return EmiTags.getIngredient(Item.class, Arrays.stream(ingredient.getItems()).map(EMISlashBladeStack::of).toList(), amount);
    }
}
