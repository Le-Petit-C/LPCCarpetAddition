package lpcCarpetAddition.commands;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lpcCarpetAddition.LPCCarpetAddition;
import lpcCarpetAddition.utils.CommandUtils;
import lpcCarpetAddition.utils.ServerExtraData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 管理「额外允许执行 /whitelist 指令的玩家」名单（非 OP 玩家也能执行 /whitelist）。
 */
public class WhitelistPermitCommand implements CommandRegistrationCallback, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.EndDataPackReload {
    @Override public void onServerStarted(@NonNull MinecraftServer server) { loadWhitelistPermit(server); }
    @Override public void endDataPackReload(@NonNull MinecraftServer server, @NonNull CloseableResourceManager resourceManager, boolean success) { loadWhitelistPermit(server); }

    public static class WhitelistPermitData {
        final Set<NameAndId> players = new LinkedHashSet<>();
        JsonElement getAsJsonElement() {
            JsonArray res = new JsonArray();
            for(NameAndId player : players) {
                JsonObject playerObject = new JsonObject();
                player.appendTo(playerObject);
                res.add(playerObject);
            }
            return res;
        }
        void setValueFromJsonElement(JsonElement element) {
            players.clear();
            if(element instanceof JsonArray playersArray) {
                for(JsonElement e : playersArray) {
                    if(e instanceof JsonObject o) {
                        NameAndId player = NameAndId.fromJson(o);
                        if(player != null) players.add(player);
                        else LPCCarpetAddition.LOGGER.warn("Invalid entry in whitelistPermit, ignored: {}", o);
                    }
                }
            }
        }
    }

    /** 该命令源是否在允许执行 /whitelist 的额外名单中（非 OP 玩家）。 */
    public static boolean isPermitted(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if(player == null) return false;
        return extraData.getExtraData(source.getServer()).players.contains(player.nameAndId());
    }

    @Override public void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                   @NonNull CommandBuildContext context,
                                   Commands.@NonNull CommandSelection selection) {
        dispatcher.register(Commands.literal("whitelistpermit")
            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("add")
                .then(Commands.argument(playerArg, GameProfileArgument.gameProfile())
                    .executes(WhitelistPermitCommand::add)
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument(playerArg, GameProfileArgument.gameProfile())
                    .suggests(WhitelistPermitCommand::suggestRemove)
                    .executes(WhitelistPermitCommand::remove)
                )
                .then(Commands.literal("all")
                    .executes(WhitelistPermitCommand::removeAll)
                )
            )
            .then(Commands.literal("list")
                .executes(WhitelistPermitCommand::list)
            )
            .then(Commands.literal("reload")
                .executes(WhitelistPermitCommand::reload)
            )
            .then(Commands.literal("help")
                .executes(WhitelistPermitCommand::help)
            )
        );
    }

    public static WhitelistPermitCommand getInstance() { return instance; }

    private static final ServerExtraData.ExtraDataRegistry<WhitelistPermitData> extraData
        = ServerExtraData.register(WhitelistPermitData.class, WhitelistPermitData::new);

    private static final WhitelistPermitCommand instance = new WhitelistPermitCommand();
    private static final String playerArg = "player";
    private static final DynamicCommandExceptionType ERROR_NOT_IN_LIST = new DynamicCommandExceptionType(
        name -> CommandUtils.fixTranslatedText("carpet.lpc.command.whitelistpermit.notInList", name));

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<NameAndId> players = GameProfileArgument.getGameProfiles(context, playerArg);
        WhitelistPermitData data = extraData.getExtraData(context);
        boolean changed = false;
        for(NameAndId player : players) {
            if(data.players.add(player)) {
                context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
                    "carpet.lpc.command.whitelistpermit.added", player.name()), true);
                changed = true;
            } else {
                context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
                    "carpet.lpc.command.whitelistpermit.alreadyInList", player.name()), false);
            }
        }
        if(changed) {
            saveWhitelistPermit(context);
            refreshCommandTree(context.getSource().getServer(), players);
        }
        return players.size();
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<NameAndId> players = GameProfileArgument.getGameProfiles(context, playerArg);
        WhitelistPermitData data = extraData.getExtraData(context);
        for(NameAndId player : players)
            if(!data.players.contains(player)) throw ERROR_NOT_IN_LIST.create(player.name());
        for(NameAndId player : players) {
            data.players.remove(player);
            context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
                "carpet.lpc.command.whitelistpermit.removed", player.name()), true);
        }
        saveWhitelistPermit(context);
        refreshCommandTree(context.getSource().getServer(), players);
        return players.size();
    }

    private static int removeAll(CommandContext<CommandSourceStack> context) {
        WhitelistPermitData data = extraData.getExtraData(context);
        Collection<NameAndId> affected = new ArrayList<>(data.players);
        data.players.clear();
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.whitelistpermit.removedAll"), true);
        saveWhitelistPermit(context);
        refreshCommandTree(context.getSource().getServer(), affected);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        WhitelistPermitData data = extraData.getExtraData(context);
        MutableComponent component = Component.empty();
        component.append(CommandUtils.fixTranslatedText("carpet.lpc.command.whitelistpermit.listHeadline").withColor(TextColor.YELLOW)).append(":");
        if(data.players.isEmpty()) {
            component.append(" ").append(CommandUtils.fixTranslatedText("carpet.lpc.command.whitelistpermit.emptyList"));
        } else {
            for(NameAndId player : data.players)
                component.append("\n    ").append(Component.literal(player.name()).withColor(TextColor.AQUA));
        }
        context.getSource().sendSystemMessage(component);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        loadWhitelistPermit(context.getSource().getServer());
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.whitelistpermit.reloaded"), true);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal(CommandUtils.loadHelpText("whitelistpermit")));
        return 1;
    }

    /** 名单变化后，向受影响的在线玩家重新发送命令树，让 /whitelist 的可见性（requires）及时更新。 */
    private static void refreshCommandTree(MinecraftServer server, Collection<NameAndId> players) {
        Commands commands = server.getCommands();
        for(NameAndId playerId : players) {
            ServerPlayer player = server.getPlayerList().getPlayerByName(playerId.name());
            if(player != null) commands.sendCommands(player);
        }
    }

    private static Path jsonPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.DATA).resolve("lpcCarpetAddition").resolve("whitelistPermit.json");
    }

    private static void loadWhitelistPermit(MinecraftServer server) {
        Path path = jsonPath(server);
        try {
            if(Files.exists(path)) extraData.getExtraData(server).setValueFromJsonElement(JsonParser.parseReader(Files.newBufferedReader(path)));
        } catch (IOException ioException) {
            LPCCarpetAddition.LOGGER.warn("Failed to load whitelistPermit", ioException);
        }
    }

    private static void saveWhitelistPermit(CommandContext<CommandSourceStack> context) {
        saveWhitelistPermit(context.getSource().getServer());
    }

    private static void saveWhitelistPermit(MinecraftServer server) {
        Path path = jsonPath(server);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                new GsonBuilder().setPrettyPrinting().create()
                    .toJson(extraData.getExtraData(server).getAsJsonElement(), writer);
            }
        } catch (IOException ioException) {
            LPCCarpetAddition.LOGGER.warn("Failed to save whitelistPermit", ioException);
        }
    }

    private static CompletableFuture<Suggestions> suggestRemove(CommandContext<CommandSourceStack> c, SuggestionsBuilder p) {
        return SharedSuggestionProvider.suggest(extraData.getExtraData(c).players.stream().map(NameAndId::name), p);
    }
}
