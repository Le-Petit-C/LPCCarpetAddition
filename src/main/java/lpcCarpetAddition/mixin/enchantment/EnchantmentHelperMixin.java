package lpcCarpetAddition.mixin.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lpcCarpetAddition.commands.EnchantmentCommand;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
	@WrapOperation(
		method = "lambda$getAvailableEnchantmentResults$1",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I")
	)
	private static int useCustomEnchantingChecks(Enchantment instance, Operation<Integer> original) {
		int old = original.call(instance);
		return EnchantmentCommand.LimitType.MULTI_RANDOM_ENCHANTMENTS
			.getLimitMap(EnchantmentCommand.getEnchantmentServer(instance))
			.getOrDefault(instance, old);
	}
}
