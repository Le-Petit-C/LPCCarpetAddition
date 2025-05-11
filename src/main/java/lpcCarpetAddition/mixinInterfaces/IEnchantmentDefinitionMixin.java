package lpcCarpetAddition.mixinInterfaces;

import net.minecraft.enchantment.Enchantment;

public interface IEnchantmentDefinitionMixin {
    void lPCCarpetAddition$setMutableMaxLevel(int level);
    int lPCCarpetAddition$getMutableMaxLevel();
    static int getMutableMaxLevel(Enchantment enchantment){
        return ((IEnchantmentDefinitionMixin)(Object)enchantment.definition()).lPCCarpetAddition$getMutableMaxLevel();
    }
    static void setMutableMaxLevel(Enchantment enchantment, int maxLevel){
        ((IEnchantmentDefinitionMixin)(Object)enchantment.definition()).lPCCarpetAddition$setMutableMaxLevel(maxLevel);
    }
}
