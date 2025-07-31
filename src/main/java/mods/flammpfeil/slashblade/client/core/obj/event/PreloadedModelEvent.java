package mods.flammpfeil.slashblade.client.core.obj.event;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 预加载模型
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = SlashBlade.MODID, value = Dist.CLIENT)
public class PreloadedModelEvent {

    @SubscribeEvent
    public static void registerResourceLoaders(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ModelResourceLoader());
    }
}
