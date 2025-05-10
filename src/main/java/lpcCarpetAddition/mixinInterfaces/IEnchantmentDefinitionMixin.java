package lpcCarpetAddition.mixinInterfaces;

import net.minecraft.enchantment.Enchantment;

public interface IEnchantmentDefinitionMixin {
    void lPCCarpetAddition$setMaxLevel(int level);
    static void setMaxLevel(Enchantment enchantment, int maxLevel){
        ((IEnchantmentDefinitionMixin)(Object)enchantment.definition()).lPCCarpetAddition$setMaxLevel(maxLevel);
    }
}
