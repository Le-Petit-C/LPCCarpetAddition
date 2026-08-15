package lpcCarpetAddition.mixin.phantom;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.features.phantomSpawn.PhantomSpawnMonsterCapped;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;players()Ljava/util/List;"))
	List<ServerPlayer> modifyPlayerList(List<ServerPlayer> original) {
		if(!LPCCarpetSettings.phantomSpawnMonsterCapped) return original;
		if(PhantomSpawnMonsterCapped.isValidGlobally()) return original;
		else return List.of();
	}
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"))
	boolean warpIsSpectator(ServerPlayer instance, Operation<Boolean> original) {
		boolean isSpectator = original.call(instance);
		if(isSpectator) return true;
		if(!LPCCarpetSettings.phantomSpawnMonsterCapped) return false;
		return !PhantomSpawnMonsterCapped.isValidLocally(instance);
	}
	@Inject(method = "tick", at = @At("RETURN"))
	void injectTickReturn(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
		PhantomSpawnMonsterCapped.capturedSpawnState = null;
	}
}
