package mods.flammpfeil.slashblade.compat.emi;

import dev.emi.emi.api.stack.ItemEmiStack;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

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
}
