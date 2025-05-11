package lpcCarpetAddition.mixin;

import lpcCarpetAddition.mixinInterfaces.IEnchantmentDefinitionMixin;
import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.Definition.class)
public class EnchantmentDefinitionMixin implements IEnchantmentDefinitionMixin {
    @Final @Shadow
    private int maxLevel;
    @Unique int mutableMaxLevel;
    @Override public void lPCCarpetAddition$setMutableMaxLevel(int level) {
        mutableMaxLevel = level;
    }
    @Override public int lPCCarpetAddition$getMutableMaxLevel() {
        return mutableMaxLevel;
    }
    @Inject(method = "<init>", at = @At("RETURN"))
    void initMixin(CallbackInfo ci){
        mutableMaxLevel = maxLevel;
    }
}
