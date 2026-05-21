package lpcCarpetAddition.utils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import static lpcCarpetAddition.utils.DataUtils.*;

public class CommandUtils {
    private static int iSuppressFeedBack = 0;
    
    public static void suppressFeedBack() { ++iSuppressFeedBack; }
    public static void unsuppressFeedBack() { --iSuppressFeedBack; }
    public static boolean isFeedBackSuppressed() { return iSuppressFeedBack != 0; }
    
    public static final EnchantmentSuggester enchantmentSuggester
            = new EnchantmentSuggester();
    public static class EnchantmentSuggester implements SuggestionProvider<CommandSourceStack>{
        private EnchantmentSuggester(){}
        @Override public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            boolean suggested = false;
            String input = context.getInput();
            String id = input.substring(input.lastIndexOf(' ') + 1);
            for(ResourceKey<Enchantment> enchantment : getEnchantments(context).registryKeySet()){
                if(!enchantment.identifier().toString().contains(id)) continue;
                suggested = true;
                builder.suggest(enchantment.identifier().toString());
            }
            if(suggested) return builder.buildFuture();
            else throw createEnchantmentSyntaxException(id);
        }
    }
    public static @NotNull CommandSyntaxException createEnchantmentSyntaxException(String invalidId){
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Invalid enchantment id:" + invalidId);
    }
    public static Registry<Enchantment> getEnchantments(CommandContext<CommandSourceStack> context){
        return DataUtils.getEnchantments(context.getSource().getServer());
    }
    public static @NotNull EnchantmentRecord getEnchantment(CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, argumentName);
        return getEnchantmentOrThrow(context.getSource().getServer(), id);
    }
}
