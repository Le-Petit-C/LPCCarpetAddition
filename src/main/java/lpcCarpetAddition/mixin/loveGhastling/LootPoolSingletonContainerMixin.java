package lpcCarpetAddition.mixin.loveGhastling;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.validators.LoveGhastlingValidator;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootPoolSingletonContainer.class)
public class LootPoolSingletonContainerMixin {
	@Mixin(LootPoolSingletonContainer.EntryBase.class)
	public static class EntryBaseMixin {
		@Shadow @Final LootPoolSingletonContainer this$0;
		@ModifyReturnValue(method = "getWeight", at = @At("RETURN"))
		int modifyGetWeightReturnValue(int original) {
			if(this$0 == LoveGhastlingValidator.getGhastlingLootItem() && LPCCarpetSettings.loveGhastling > 0)
				return 0;
			return original;
		}
	}
}
