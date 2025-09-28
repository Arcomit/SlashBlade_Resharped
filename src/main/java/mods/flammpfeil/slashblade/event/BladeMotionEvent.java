package mods.flammpfeil.slashblade.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class BladeMotionEvent extends Event {
    private final LivingEntity entity;
    private final ResourceLocation combo;

    public BladeMotionEvent(LivingEntity entity, ResourceLocation combo) {
        this.entity = entity;
        this.combo = combo;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ResourceLocation getCombo() {
        return this.combo;
    }
}
