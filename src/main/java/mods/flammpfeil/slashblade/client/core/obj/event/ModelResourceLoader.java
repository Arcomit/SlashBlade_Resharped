package mods.flammpfeil.slashblade.client.core.obj.event;

import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.core.obj.WavefrontObject;
import mods.flammpfeil.slashblade.init.DefaultResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

// 模型资源预加载（防止启动游戏就直接爆玩家显存，只给了原版用）
public class ModelResourceLoader implements PreparableReloadListener {
    private static final ResourceLocation MODEL_DIR = SlashBlade.prefix("model");
    private static final String FILE_TYPES = ".obj";

    private void loadResources(ResourceManager manager) {
        ModelManager instance = ModelManager.getInstance();
        LoadingCache<ResourceLocation, WavefrontObject> cache = instance.cache;
        cache.invalidateAll();
        instance.defaultModel = new WavefrontObject(DefaultResources.resourceDefaultModel);

        String targetNamespace = MODEL_DIR.getNamespace();
        Map<ResourceLocation, Resource> resources = manager.listResources(
                MODEL_DIR.getPath(),
                resLoc -> resLoc.getNamespace().equals(targetNamespace)
                        && resLoc.getPath().endsWith(FILE_TYPES)
        );

        resources.keySet().forEach(resourceLocation -> {
            instance.getModel(resourceLocation);
            System.out.println("是的，我们他妈加载了这个 " + resourceLocation);
        });
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier stage,
                                          ResourceManager resourceManager,
                                          ProfilerFiller preparationsProfiler,
                                          ProfilerFiller reloadProfiler,
                                          Executor backgroundExecutor,
                                          Executor gameExecutor) {
        return CompletableFuture.runAsync(() -> {
            loadResources(resourceManager);
        }, backgroundExecutor).thenCompose(stage::wait).thenApplyAsync(v -> {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> {
                    ModelManager.getInstance().cache.asMap().forEach((key, value) -> {
                        value.initAll();
                        System.out.println("是的，我们他妈初始化了这个 " + key);
                    });
                });
            }else {
                ModelManager.getInstance().cache.asMap().forEach((key, value) -> {
                    value.initAll();
                    System.out.println("是的，我们他妈初始化了这个 " + key);
                });
            }
            return v;
        }, backgroundExecutor);
    }
}