package lpcCarpetAddition.mixin.accessors;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(LocalMobCapCalculator.class)
public interface LocalMobCapCalculatorAccessor {
	@Accessor Map<ServerPlayer, LocalMobCapCalculator.MobCounts> getPlayerMobCounts();
	@Mixin(LocalMobCapCalculator.MobCounts.class)
	interface MobCountsAccessor {
		@Accessor Object2IntMap<MobCategory> getCounts();
	}
}
