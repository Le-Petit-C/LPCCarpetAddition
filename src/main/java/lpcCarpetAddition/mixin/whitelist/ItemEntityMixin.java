package lpcCarpetAddition.mixin.whitelist;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
	@ModifyExpressionValue(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
	boolean modifyItemPlayerTouch(boolean original, @Local(argsOnly = true) Player player) {
		if(original) return true;
		if (!LPCCarpetSettings.rejectNonWhitelistedPlayerDropOrPickItem) return false;
		if (!(player instanceof ServerPlayer serverPlayer)) return false;
		if (WhitelistMethods.notWhiteListed(serverPlayer)) {
			WhitelistMethods.sendNotWhitelistedMessage(serverPlayer);
			return true;
		}
		return false;
	}
}
