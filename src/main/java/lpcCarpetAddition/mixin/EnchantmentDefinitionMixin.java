package lpcCarpetAddition.mixin;

import lpcCarpetAddition.mixinInterfaces.IEnchantmentDefinitionMixin;
import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Enchantment.Definition.class)
public class EnchantmentDefinitionMixin implements IEnchantmentDefinitionMixin {
    @Mutable @Final @Shadow
    private int maxLevel;
    @Override public void lPCCarpetAddition$setMaxLevel(int level) {
        maxLevel = level;
    }
}
