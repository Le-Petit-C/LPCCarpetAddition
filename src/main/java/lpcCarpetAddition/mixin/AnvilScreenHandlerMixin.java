package lpcCarpetAddition.mixin;

import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.mixinInterfaces.IEnchantmentDefinitionMixin;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    Object erasePunishment(ItemStack instance, ComponentType<?> componentType, Object o){
        if(LPCCarpetSettings.disableAnvilPunishment && componentType == DataComponentTypes.REPAIR_COST) return 0;
        else return instance.getOrDefault(componentType, o);
    }
    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I"))
    int redirectGetMaxLevel(Enchantment instance){
        return IEnchantmentDefinitionMixin.getMutableMaxLevel(instance);
    }
    @ModifyConstant(method = "updateResult", constant = {@Constant(intValue = 40), @Constant(intValue = 39)})
    int modifyAnvilLimit(int constant){
        if(LPCCarpetSettings.survivalAnvilLimit < 0) return Integer.MAX_VALUE - constant + 40;
        else return LPCCarpetSettings.survivalAnvilLimit - constant + 40;
    }
}
