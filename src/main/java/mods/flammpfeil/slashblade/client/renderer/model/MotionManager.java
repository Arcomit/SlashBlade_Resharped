package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdVmdMotionMc;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.util.concurrent.Executors;

import static mods.flammpfeil.slashblade.init.DefaultResources.ExMotionLocation;

/**
 * Created by Furia on 2016/02/06.
 */
public class MotionManager {

    private static final class SingletonHolder {
        private static final MotionManager instance = new MotionManager();
    }

    public static MotionManager getInstance() {
        return SingletonHolder.instance;
    }

    MmdVmdMotionMc defaultMotion;

    LoadingCache<ResourceLocation, MmdVmdMotionMc> cache;

    private MotionManager() {
        try {
            defaultMotion = new MmdVmdMotionMc(ExMotionLocation);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MmdException e) {
            e.printStackTrace();
        }

        cache = CacheBuilder.newBuilder()
                .build(CacheLoader.asyncReloading(new CacheLoader<ResourceLocation, MmdVmdMotionMc>() {
                    @Override
                    public MmdVmdMotionMc load(ResourceLocation key) throws Exception {
                        try {
                            return new MmdVmdMotionMc(key);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return defaultMotion;
                        }
                    }

                }, Executors.newCachedThreadPool()));
    }

    @SubscribeEvent
    public void reload(TextureStitchEvent.Post event) {
        cache.invalidateAll();

        try {
            defaultMotion = new MmdVmdMotionMc(ExMotionLocation);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MmdException e) {
            e.printStackTrace();
        }
    }

    public MmdVmdMotionMc getMotion(ResourceLocation loc) {
        if (loc != null) {
            try {
                return cache.get(loc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultMotion;
    }

}
