package mods.flammpfeil.slashblade.capability.slashblade;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;

public class NamedBladeStateCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    protected LazyOptional<ISlashBladeState> state;
    private ItemStack blade;
    public NamedBladeStateCapabilityProvider(ItemStack blade) {
    	state = LazyOptional.of( () -> new SlashBladeState(blade));
    	this.blade = blade;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ItemSlashBlade.BLADESTATE.orEmpty(cap, state);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.state.orElseGet(() -> new SlashBladeState(blade)).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag inTag) {
        state.ifPresent(instance -> instance.deserializeNBT(inTag));
    }
}
