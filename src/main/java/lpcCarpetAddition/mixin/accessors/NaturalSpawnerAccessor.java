package lpcCarpetAddition.mixin.accessors;

import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.world.level.NaturalSpawner.class)
public interface NaturalSpawnerAccessor {
	@Mixin(net.minecraft.world.level.NaturalSpawner.SpawnState.class)
	interface SpawnStateAccessor {
		@Accessor LocalMobCapCalculator getLocalMobCapCalculator();
	}
}
