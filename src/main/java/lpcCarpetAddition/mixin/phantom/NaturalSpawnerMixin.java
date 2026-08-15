package lpcCarpetAddition.mixin.phantom;

import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.features.phantomSpawn.PhantomSpawnMonsterCapped;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	@Inject(method = "createState", at = @At("RETURN"))
	private static void captureCreatedState(CallbackInfoReturnable<NaturalSpawner.SpawnState> cir) {
		if(LPCCarpetSettings.phantomSpawnMonsterCapped)
			PhantomSpawnMonsterCapped.capturedSpawnState = cir.getReturnValue();
	}
}
