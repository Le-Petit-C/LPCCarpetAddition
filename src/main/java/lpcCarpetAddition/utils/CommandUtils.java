package lpcCarpetAddition.utils;

import carpet.CarpetSettings;
import carpet.utils.Translations;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

public class CommandUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class);
    private static int iSuppressFeedBack = 0;
    
    public static void suppressFeedBack() { ++iSuppressFeedBack; }
    public static void unsuppressFeedBack() { --iSuppressFeedBack; }
    public static boolean isFeedBackSuppressed() { return iSuppressFeedBack != 0; }

    public static <T> SuggestionProvider<T> suggestInEnum(Class<? extends Enum<?>> enumClass) {
        return suggestInArray(enumClass.getEnumConstants(), v->v.name().toLowerCase());
    }

    public static <T, U> SuggestionProvider<T> suggestInArray(U[] iterable, Function<U, String> mapping) {
        var suggestionsBuilder = ImmutableList.<String>builder();
        for(var u : iterable) suggestionsBuilder.add(mapping.apply(u));
        return (_, builder) -> SharedSuggestionProvider.suggest(suggestionsBuilder.build(), builder);
    }

    public static MutableComponent fixTranslatedText(String translationKey, Object... args) {
        return Component.translatableWithFallback(translationKey, Translations.tr(translationKey), args);
    }

    public static Identifier getEnchantmentId(MinecraftServer server, Enchantment enchantment) {
        return server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getKey(enchantment);
    }

    public static Identifier getEnchantmentId(CommandSourceStack sourceStack, Enchantment enchantment) {
        return getEnchantmentId(sourceStack.getServer(), enchantment);
    }

    public static Identifier getEnchantmentId(CommandContext<? extends CommandSourceStack> context, Enchantment enchantment) {
        return getEnchantmentId(context.getSource(), enchantment);
    }

    /**
     * Loads the help text for a sub-command from the mod resource
     * {@code assets/lpccarpetaddition/help/<subCommand>/<lang>.txt}, following
     * the server language ({@link CarpetSettings#language}) with en_us fallback.
     */
    public static String loadHelpText(String subCommand) {
        String lang = CarpetSettings.language;
        String text = readHelpResource(subCommand, lang);
        if (text == null && !"en_us".equals(lang)) text = readHelpResource(subCommand, "en_us");
        return text == null ? "" : text.replace("\r\n", "\n");
    }

    private static String readHelpResource(String subCommand, String lang) {
        try (InputStream in = CommandUtils.class.getClassLoader()
                .getResourceAsStream("assets/lpccarpetaddition/help/" + subCommand + "/" + lang + ".txt")) {
            return in == null ? null : IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Failed to read help text for '{}/{}'", subCommand, lang, e);
            return null;
        }
    }

    /** 名单变化后，向受影响的在线玩家重新发送命令树，让 /whitelist 的可见性（requires）及时更新。 */
    public static void refreshCommandTree(MinecraftServer server, Iterable<NameAndId> players) {
        Commands commands = server.getCommands();
        for(NameAndId playerInfo : players) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerInfo.id());
            if(player != null) commands.sendCommands(player);
        }
    }

    public static void refreshCommandTree(MinecraftServer server, NameAndId ...players) {
        refreshCommandTree(server, List.of(players));
    }
}
