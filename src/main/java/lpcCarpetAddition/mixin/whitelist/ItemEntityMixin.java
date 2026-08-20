package lpcCarpetAddition.mixin.whitelist;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
	@ModifyExpressionValue(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
	boolean modifyItemPlayerTouch(boolean original, @Local(argsOnly = true) Player player) {
		if(original) return true;
		MinecraftServer server = WhitelistMethods.notNullIfServerAndShouldReject(LPCCarpetSettings.rejectNonWhitelistedPlayerDropOrPickItem, player);
		if (server == null) return false;
		if (WhitelistMethods.notWhiteListed(server, player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			return true;
		}
		return false;
	}
}
