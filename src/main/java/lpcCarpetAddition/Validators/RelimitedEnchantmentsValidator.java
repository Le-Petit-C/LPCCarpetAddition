package lpcCarpetAddition.Validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import lpcCarpetAddition.LPCCarpetAddition;
import lpcCarpetAddition.mixinInterfaces.IEnchantmentDefinitionMixin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class RelimitedEnchantmentsValidator extends Validator<String> {
    private static void applyEnchantmentSettings(MinecraftServer server, String setting){
        Registry<Enchantment> registry = server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        String[] splits = setting.split(";");
        for(String str : splits){
            String[] split = str.split(",");
            Enchantment enchantment;
            try {
                enchantment = registry.get(Identifier.of(split[0]));
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
    @Override public String validate(@Nullable ServerCommandSource source, CarpetRule<String> changingRule, String newValue, String userInput) {
        if (source != null) applyEnchantmentSettings(source.getServer(), newValue);
        return newValue;
    }
}
