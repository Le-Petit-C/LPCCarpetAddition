package lpcCarpetAddition.mixin;

import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.mixinInterfaces.IEnchantmentDefinitionMixin;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    Object erasePunishment(ItemStack instance, DataComponentType<?> componentType, Object o){
        if(LPCCarpetSettings.disableAnvilPunishment && componentType == DataComponents.REPAIR_COST) return 0;
        else return instance.getOrDefault(componentType, o);
    }
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    int redirectGetMaxLevel(Enchantment instance){
        return IEnchantmentDefinitionMixin.getMutableMaxLevel(instance);
    }
    @ModifyConstant(method = "createResult", constant = {@Constant(intValue = 40), @Constant(intValue = 39)})
    int modifyAnvilLimit(int constant){
        if(LPCCarpetSettings.survivalAnvilLimit < 0) return Integer.MAX_VALUE - constant + 40;
        else return LPCCarpetSettings.survivalAnvilLimit - constant + 40;
    }
}
