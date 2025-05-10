package lpcCarpetAddition.mixin;

import lpcCarpetAddition.LPCCarpetAddition;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.mixinInterfaces.IEnchantmentDefinitionMixin;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Unique private static String lastString = "";
    @Unique private static final HashMap<String, Integer> relimitedEnchantmentMap = new HashMap<>();

    @Inject(method = "getEnchantments", at = @At("RETURN"))
    private static void setMaxLevelAfterGetEnchantments(ItemStack stack, CallbackInfoReturnable<ItemEnchantmentsComponent> cir){
        if(!lastString.equals(LPCCarpetSettings.relimitedEnchantments)){
            lastString = LPCCarpetSettings.relimitedEnchantments;
            String[] splits = lastString.split(";");
            for(String str : splits){
                String[] split = str.split(",");
                switch (split.length) {
                    case 1 -> relimitedEnchantmentMap.put(split[0], Integer.MAX_VALUE);
                    case 2 -> {
                        try {
                            relimitedEnchantmentMap.put(split[0], Integer.valueOf(split[1]));
                        } catch (NumberFormatException exception) {
                            LPCCarpetAddition.LOGGER.warn("Invalid value string:{}", split[1]);
                        }
                    }
                    default -> LPCCarpetAddition.LOGGER.warn("Invalid enchantment limit string:{}", str);
                }
            }
        }
        for(RegistryEntry<Enchantment> enchantmentEntry : cir.getReturnValue().getEnchantments()){
            String id = enchantmentEntry.getIdAsString();
            Integer maxLevel = relimitedEnchantmentMap.get(id);
            if(maxLevel != null) {
                IEnchantmentDefinitionMixin.setMaxLevel(enchantmentEntry.value(), maxLevel);
                continue;
            }
            Integer subMaxLevel = relimitedEnchantmentMap.get(id.substring(id.lastIndexOf(':') + 1));
            if(subMaxLevel != null) IEnchantmentDefinitionMixin.setMaxLevel(enchantmentEntry.value(), subMaxLevel);
        }
    }
}
