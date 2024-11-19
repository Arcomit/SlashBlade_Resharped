package mods.flammpfeil.slashblade;

import net.minecraft.client.renderer.RenderType;

public interface IMixinFullyBufferedMultiBufferSource {
    public void onEndBatchCustom(RenderType specificType);
}
