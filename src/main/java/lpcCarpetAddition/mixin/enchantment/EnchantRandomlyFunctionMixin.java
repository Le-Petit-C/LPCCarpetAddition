package lpcCarpetAddition.mixin.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import lpcCarpetAddition.commands.EnchantmentCommand;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantRandomlyFunction.class)
public class EnchantRandomlyFunctionMixin {
	@WrapOperation(method = "enchantItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
	int wrapGetMaxLevel(Enchantment instance, Operation<Integer> original, @Local(argsOnly = true) LootContext context) {
		int old = original.call(instance);
		return EnchantmentCommand.LimitType.SINGLE_RANDOM_ENCHANTMENT.getLimitMap(context).getOrDefault(instance, old);
	}
}
