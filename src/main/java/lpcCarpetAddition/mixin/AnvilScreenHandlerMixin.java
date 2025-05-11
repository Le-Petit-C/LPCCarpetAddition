package lpcCarpetAddition.mixin;

import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.mixinInterfaces.IEnchantmentDefinitionMixin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;intValue()I"))
    int erasePunishment(Integer instance){
        if(LPCCarpetSettings.disableAnvilPunishment) return 0;
        else return instance;
    }
    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I"))
    int redirectGetMaxLevel(Enchantment instance){
        return IEnchantmentDefinitionMixin.getMutableMaxLevel(instance);
    }
}
