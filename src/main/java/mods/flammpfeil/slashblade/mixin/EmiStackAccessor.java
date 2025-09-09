package mods.flammpfeil.slashblade.mixin;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EmiStack.class, remap = false)
public interface EmiStackAccessor {
    @Accessor("comparison")
    Comparison getComparison();

    @Accessor("comparison")
    void setComparison(Comparison comparison);
}