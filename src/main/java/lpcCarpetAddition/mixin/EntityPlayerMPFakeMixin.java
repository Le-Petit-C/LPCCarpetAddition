package lpcCarpetAddition.mixin;

import carpet.patches.EntityPlayerMPFake;
import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeMixin {
    @Unique private EntityPlayerMPFake getThis(){return (EntityPlayerMPFake)(Object)this;}
    @Inject(method = "onDeath", at = @At("RETURN"))
    void resetExperience(DamageSource cause, CallbackInfo ci){
        if(!LPCCarpetSettings.fakePlayerExperienceDuplicationFix) return;
        getThis().setExperienceLevel(0);
        getThis().setExperiencePoints(0);
    }
}
