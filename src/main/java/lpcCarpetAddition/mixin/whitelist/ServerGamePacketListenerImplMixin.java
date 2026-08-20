package lpcCarpetAddition.mixin.whitelist;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpcCarpetAddition.LPCCarpetSettings.*;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

	@Inject(method = "handleContainerSlotStateChanged", cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
	void onContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket packet, CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerMoveContainerItem, player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			player.inventoryMenu.sendAllDataToRemote();
			ci.cancel();
		}
	}

	@WrapOperation(method = "handlePlayerAction",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Z)V"))
	void onDropItem(ServerPlayer instance, boolean all, Operation<Void> original) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerDropOrPickItem, player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			player.inventoryMenu.sendAllDataToRemote();
		}
		else original.call(instance, all);
	}

	@WrapOperation(method = "handlePlayerAction",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;II)V"))
	void onBreakBlock(ServerPlayerGameMode instance, BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxY, int sequence, Operation<Void> original) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerAttackBlock, player))
			WhitelistMethods.sendNotWhitelistedMessage(player);
		else original.call(instance, pos, action, direction, maxY, sequence);
	}

	@Inject(method = "handleUseItemOn", cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
	void onInteractBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerInteractBlock, player)) {
			if(!player.isShiftKeyDown() && WhitelistMethods.shouldAllowBlockInteraction(player.level(), packet.getHitResult())) return;
			WhitelistMethods.sendNotWhitelistedMessage(player);
			((ServerGamePacketListenerImpl)(Object)this).ackBlockChangesUpTo(packet.getSequence());
			WhitelistMethods.sendBlockUpdatePackets(player, packet.getHitResult());
			ci.cancel();
		}
	}

	@Inject(method = "handleUseItem", cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
	void onUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerDropOrPickItem, player) && player.getItemInHand(packet.getHand()).is(ItemTags.BUNDLES)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			player.containerMenu.sendAllDataToRemote();
			ci.cancel();
		}
	}

	@Inject(method = "handleAttack", cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
	void onAttackEntity(ServerboundAttackPacket packet, CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerAttackEntity, player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			ci.cancel();
		}
	}

	@Inject(method = "handleInteract", cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
	void onInteractEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerInteractEntity, player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			ci.cancel();
		}
	}

	@Inject(method = {"handleContainerClick", "handleBundleItemSelectedPacket"}, cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
	void onContainerClick(CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerMoveContainerItem, player) && !(player.containerMenu instanceof InventoryMenu)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			player.containerMenu.sendAllDataToRemote();
			ci.cancel();
		}
	}

	@Inject(method = "handleContainerClick", cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
	void onThrowingItem(ServerboundContainerClickPacket packet, CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerDropOrPickItem, player)
			&& (packet.containerInput() == ContainerInput.THROW || packet.slotNum() == -999)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			player.containerMenu.sendAllDataToRemote();
			ci.cancel();
		}
	}

	@Inject(method = "handleSignUpdate", cancellable = true, at = @At("HEAD"))
	void onSignUpdate(ServerboundSignUpdatePacket packet, CallbackInfo ci) {
		if (WhitelistMethods.shouldReject(rejectNonWhitelistedPlayerEditSign, player)) {
			WhitelistMethods.sendNotWhitelistedMessage(player);
			ci.cancel();
		}
	}
}
