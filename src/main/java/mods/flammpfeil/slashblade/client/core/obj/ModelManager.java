package mods.flammpfeil.slashblade.client.core.obj;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mods.flammpfeil.slashblade.client.core.obj.model.WavefrontObject;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.concurrent.Executors;

// 模型缓存管理器
@OnlyIn(Dist.CLIENT)
public class ModelManager {

    private static final ModelManager instance = new ModelManager();
    public static ModelManager getInstance() {
        return instance;
    }

    public static Registry<SlashBladeDefinition> getClientSlashBladeRegistry() {
        return Minecraft.getInstance().getConnection().registryAccess()
                .registryOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }

    public WavefrontObject defaultModel;

    public LoadingCache<ResourceLocation, WavefrontObject> cache;

    private ModelManager() {
        defaultModel = new WavefrontObject(DefaultResources.resourceDefaultModel);

        cache = CacheBuilder.newBuilder()
                .build(CacheLoader.asyncReloading(new CacheLoader<ResourceLocation, WavefrontObject>() {
                    @Override
                    public WavefrontObject load(ResourceLocation key) throws Exception {
                        try {
                            return new WavefrontObject(key);
                        } catch (Exception e) {
                            return defaultModel;
                        }
                    }

                }, Executors.newCachedThreadPool()));
    }

    public WavefrontObject getModel(ResourceLocation loc) {
        if (loc != null) {
            try {
                return cache.get(loc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultModel;
    }

}
