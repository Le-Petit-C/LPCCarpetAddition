package lpcCarpetAddition.mixin.accessors;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootTable.class)
public interface LootTableAccessor {
	@Mixin(LootTable.Builder.class)
	interface BuilderAccessor {
		@Accessor ImmutableList.Builder<LootPool> getPools();
	}
}
