package lpcCarpetAddition.Utils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static lpcCarpetAddition.Utils.DataUtils.*;

public class CommandUtils {
    public static final EnchantmentSuggester enchantmentSuggester
            = new EnchantmentSuggester();
    public static class EnchantmentSuggester implements SuggestionProvider<ServerCommandSource>{
        private EnchantmentSuggester(){}
        @Override public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            boolean suggested = false;
            String input = context.getInput();
            String id = input.substring(input.lastIndexOf(' ') + 1);
            for(RegistryKey<Enchantment> enchantment : getEnchantments(context).getKeys()){
                if(!enchantment.getValue().toString().contains(id)) continue;
                suggested = true;
                builder.suggest(enchantment.getValue().toString());
            }
            if(suggested) return builder.buildFuture();
            else throw createEnchantmentSyntaxException(id);
        }
    }
    public static @NotNull CommandSyntaxException createEnchantmentSyntaxException(String invalidId){
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Invalid enchantment id:" + invalidId);
    }
    public static Registry<Enchantment> getEnchantments(CommandContext<ServerCommandSource> context){
        return DataUtils.getEnchantments(context.getSource().getServer());
    }
    public static @NotNull EnchantmentRecord getEnchantment(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, argumentName);
        return getEnchantmentOrThrow(context.getSource().getServer(), id);
    }
}
