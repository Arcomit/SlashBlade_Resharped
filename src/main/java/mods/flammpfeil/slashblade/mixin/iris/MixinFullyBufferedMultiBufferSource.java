package mods.flammpfeil.slashblade.mixin.iris;

import mods.flammpfeil.slashblade.IMixinFullyBufferedMultiBufferSource;
import net.irisshaders.batchedentityrendering.impl.BufferSegment;
import net.irisshaders.batchedentityrendering.impl.BufferSegmentRenderer;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(FullyBufferedMultiBufferSource.class)
public class MixinFullyBufferedMultiBufferSource implements IMixinFullyBufferedMultiBufferSource {

    @Shadow
    private boolean isReady;

    @Shadow
    private Map<RenderType, List<BufferSegment>> typeToSegment;

    @Shadow
    private BufferSegmentRenderer segmentRenderer;

    @Shadow
    private int renderTypes;

    @Shadow
    private int drawCalls;

    // 添加一个新方法而不修改原有方法
    @Override
    public void onEndBatchCustom(RenderType specificType) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        if (!this.isReady) {
            ((FullyBufferedMultiBufferSource) (Object) this).readyUp();
        }

        profiler.push("draw specific buffer");
        List<BufferSegment> segments = this.typeToSegment.getOrDefault(specificType, Collections.emptyList());
        if (!segments.isEmpty()) {
            specificType.setupRenderState();
            ++this.renderTypes;

            for (BufferSegment segment : segments) {
                this.segmentRenderer.drawInner(segment);
                ++this.drawCalls;
            }

            this.typeToSegment.remove(specificType);
            specificType.clearRenderState();
        }

        profiler.pop();
    }
}
