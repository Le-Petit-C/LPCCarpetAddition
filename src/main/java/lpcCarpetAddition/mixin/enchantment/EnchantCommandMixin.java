package lpcCarpetAddition.mixin.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import lpcCarpetAddition.commands.EnchantmentCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {
    @WrapOperation(method = "enchant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    private static int wrapGetMaxLevel(Enchantment instance, Operation<Integer> original, @Local(argsOnly = true) CommandSourceStack source) {
        int old = original.call(instance);
        var directMap = EnchantmentCommand.LimitType.ENCHANT_COMMAND.getLimitMap(source);
        if(directMap.containsKey(instance)) return directMap.getInt(instance);
        int max = old;
        for(var type : EnchantmentCommand.LimitType.values()) {
            if(type == EnchantmentCommand.LimitType.ENCHANT_COMMAND) continue;
            var map = type.getLimitMap(source);
            if(map.containsKey(instance)) {
                int lvl = map.getInt(instance);
                if(lvl > max) max = lvl;
            }
        }
        return max;
    }
}
