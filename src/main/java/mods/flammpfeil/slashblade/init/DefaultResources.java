package mods.flammpfeil.slashblade.init;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;

public interface DefaultResources {
    ResourceLocation BaseMotionLocation = SlashBlade.prefix("combostate/old_motion.vmd");
    ResourceLocation ExMotionLocation = SlashBlade.prefix("combostate/motion.vmd");
    
    ResourceLocation testLocation = SlashBlade.prefix("combostate/piercing.vmd");
    
    ResourceLocation testPLLocation = SlashBlade.prefix("combostate/piercing_pl.vmd");

    public static final ResourceLocation resourceDefaultModel = new ResourceLocation("slashblade", "model/blade.obj");
    public static final ResourceLocation resourceDefaultTexture = new ResourceLocation("slashblade", "model/blade.png");

    public static final ResourceLocation resourceDurabilityModel = new ResourceLocation("slashblade",
            "model/util/durability.obj");
    public static final ResourceLocation resourceDurabilityTexture = new ResourceLocation("slashblade",
            "model/util/durability.png");
}
