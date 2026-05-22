package lpcCarpetAddition.validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import lpcCarpetAddition.LPCCarpetAddition;
import lpcCarpetAddition.mixinInterfaces.IEnchantmentDefinitionMixin;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

public class RelimitedEnchantmentsValidator extends Validator<String> {
    private static void applyEnchantmentSettings(MinecraftServer server, String setting){
        Registry<Enchantment> registry = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        String[] splits = setting.split(";");
        for(String str : splits){
            String[] split = str.split(",");
            Enchantment enchantment;
            try {
                enchantment = registry.getValue(Identifier.parse(split[0]));
                if(enchantment == null) throw new Exception();
            }catch (Exception e){
                LPCCarpetAddition.LOGGER.warn("Invalid enchantment key:{}", split[0]);
                continue;
            }
            switch (split.length) {
                case 1 -> IEnchantmentDefinitionMixin.setMutableMaxLevel(enchantment, Integer.MAX_VALUE);
                case 2 -> {
                    try {
                        IEnchantmentDefinitionMixin.setMutableMaxLevel(enchantment, Integer.parseInt(split[1]));
                    } catch (NumberFormatException exception) {
                        LPCCarpetAddition.LOGGER.warn("Invalid value string:{}", split[1]);
                    }
                }
                default -> LPCCarpetAddition.LOGGER.warn("Invalid enchantment limit string:{}", str);
            }
        }
    }
    @Override public String validate(@Nullable CommandSourceStack source, CarpetRule<String> changingRule, String newValue, String userInput) {
        if (source != null) applyEnchantmentSettings(source.getServer(), newValue);
        return newValue;
    }
}
