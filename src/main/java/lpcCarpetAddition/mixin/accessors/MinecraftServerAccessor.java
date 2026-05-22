package lpcCarpetAddition.mixin.accessors;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
	@Accessor FuelValues getFuelValues();
}
