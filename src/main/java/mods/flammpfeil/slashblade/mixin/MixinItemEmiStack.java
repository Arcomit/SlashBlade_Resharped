package mods.flammpfeil.slashblade.mixin;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemEmiStack.class, remap = false)
public abstract class MixinItemEmiStack extends EmiStack {

    private static Comparison SLASHBLADE_COMPARSION = Comparison.of((self, other) -> {
        ItemStack aStack = self.getItemStack();
        ItemStack bStack = other.getItemStack();
        if (aStack.getItem() != bStack.getItem()) return false;
        String keyA = self.getNbt().getCompound("bladeState").getString("translationKey");
        String keyB = other.getNbt().getCompound("bladeState").getString("translationKey");

        return keyB.equals(keyA);
    });

    @Unique
    public ItemStack slashBlade_Resharped$origionalStack;

    @Inject(method = "<init>(Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void onConstructor(ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() instanceof ItemSlashBlade) {
            this.slashBlade_Resharped$origionalStack = stack.copy();
            this.comparison = SLASHBLADE_COMPARSION;
        }
    }

    @Inject(method = "getItemStack()Lnet/minecraft/world/item/ItemStack;", at = @At("TAIL"), cancellable = true)
    public void getItemStack(CallbackInfoReturnable<ItemStack> cir) {
        if (this.slashBlade_Resharped$origionalStack != null)
            cir.setReturnValue(this.slashBlade_Resharped$origionalStack.copy());

    }

    @Inject(method = "copy()Ldev/emi/emi/api/stack/EmiStack;", at = @At("TAIL"), cancellable = true)
    public void copy(CallbackInfoReturnable<EmiStack> cir) {
        if (this.slashBlade_Resharped$origionalStack != null) {
            EmiStack origin = cir.getReturnValue();
            EmiStack stack = new ItemEmiStack(this.slashBlade_Resharped$origionalStack);
            stack.setChance(origin.getChance())
                    .setRemainder(origin.getRemainder().copy())
                    .comparison(SLASHBLADE_COMPARSION);

            cir.setReturnValue(stack);
        }
    }

}