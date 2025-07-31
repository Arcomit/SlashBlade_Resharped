package mods.flammpfeil.slashblade.mixin;

import mods.flammpfeil.slashblade.client.core.obj.ModelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderStateShard.class)
public class MixinRenderStateShard {

    @Inject(method = "setupRenderState()V", at = @At("TAIL"))
    private void afterSetupRenderState(CallbackInfo ci) {
        ModelRenderer.setCurrentRenderStateShard((RenderStateShard)(Object)this);
    }
}
