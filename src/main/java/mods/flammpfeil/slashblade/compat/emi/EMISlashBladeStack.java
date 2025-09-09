package mods.flammpfeil.slashblade.compat.emi;

import dev.emi.emi.api.stack.Comparison;
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
    public static final Comparison COMPARE_SLASHBLADE = Comparison.of((a, b) -> {
        ItemStack aStack = a.getItemStack();
        ItemStack bStack = b.getItemStack();
        if (aStack.getItem() != bStack.getItem()) return false;
        String keyA = a.getNbt().getCompound("bladeState").getString("translationKey");
        String keyB = b.getNbt().getCompound("bladeState").getString("translationKey");

        System.out.println(a.getClass());
        System.out.println(a.getNbt());
        System.out.println(b.getClass());
        System.out.println(b.getNbt());

        return keyA.equals(keyB);
    });

    private CompoundTag capNBT;

    public EMISlashBladeStack(ItemStack stack) {
        super(stack);
        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
            this.capNBT = s.serializeNBT();
        });
        this.comparison = COMPARE_SLASHBLADE;
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
