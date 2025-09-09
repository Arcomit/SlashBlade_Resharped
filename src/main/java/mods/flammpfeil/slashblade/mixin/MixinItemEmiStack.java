package mods.flammpfeil.slashblade.mixin;

import dev.emi.emi.api.stack.ItemEmiStack;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemEmiStack.class, remap = false)
public class MixinItemEmiStack {
    @Final
    @Shadow
    private Item item;

    @Final
    @Shadow
    private CompoundTag nbt;

    @Unique
    private ItemStack slashBlade_Resharped$origionalStack;

    @Inject(method = "<init>(Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void onConstructor(ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() instanceof ItemSlashBlade)
            this.slashBlade_Resharped$origionalStack = stack.copy();
    }

    @Inject(method = "getItemStack", at = @At("TAIL"), cancellable = true)
    public void getItemStack(CallbackInfoReturnable<ItemStack> cir) {
        if (this.slashBlade_Resharped$origionalStack != null)
            cir.setReturnValue(this.slashBlade_Resharped$origionalStack.copy());

    }
}