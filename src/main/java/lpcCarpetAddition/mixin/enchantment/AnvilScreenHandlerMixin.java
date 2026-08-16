package lpcCarpetAddition.mixin.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.commands.EnchantmentCommand;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {
    public AnvilScreenHandlerMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition itemInputSlots) {
        super(menuType, containerId, inventory, access, itemInputSlots);
    }

    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    Object erasePunishment(ItemStack instance, DataComponentType<?> componentType, Object o, Operation<Object> original){
        if(LPCCarpetSettings.disableAnvilPunishment && componentType == DataComponents.REPAIR_COST) return 0;
        else return original.call(instance, componentType, o);
    }
    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    int wrapGetMaxLevel(Enchantment instance, Operation<Integer> original) {
        int old = original.call(instance);
        if(!(player instanceof ServerPlayer serverPlayer)) return old;
        else return EnchantmentCommand.LimitType.ANVIL.getLimitMap(serverPlayer).getOrDefault(instance, old);
    }
    @ModifyConstant(method = "createResult", constant = {@Constant(intValue = 40), @Constant(intValue = 39)})
    int modifyAnvilLimit(int constant){
        if(LPCCarpetSettings.survivalAnvilLimit < 0) return Integer.MAX_VALUE - constant + 40;
        else return LPCCarpetSettings.survivalAnvilLimit - constant + 40;
    }
}
