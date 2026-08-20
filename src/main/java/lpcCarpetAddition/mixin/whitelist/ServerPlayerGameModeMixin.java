package lpcCarpetAddition.mixin.whitelist;

import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpcCarpetAddition.LPCCarpetSettings.*;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Final @Shadow protected ServerPlayer player;

	@Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
	void onHandleBlockBreakAction(CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerAttackBlock, player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			ci.cancel();
		}
	}
	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	void onHandleBlockInteract(ServerPlayer player, Level level, ItemStack itemStack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
		if(!WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerInteractBlock, player)) return;
		if(!player.isShiftKeyDown() && WhitelistMethods.shouldAllowBlockInteraction(level, hitResult)) return;
		if (WhitelistMethods.notWhiteListed(player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			WhitelistMethods.sendBlockUpdatePackets(player, hitResult);
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
	@Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
	void onHandleBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerAttackBlock, player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			cir.setReturnValue(false);
		}
	}
}
