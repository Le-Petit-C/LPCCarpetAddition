package lpcCarpetAddition.commands;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lpcCarpetAddition.LPCCarpetAddition;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.utils.CommandUtils;
import lpcCarpetAddition.utils.ServerExtraData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * 管理「rejectNonWhitelistedPlayerExecuteServerCommand 开启时，非白名单玩家仍被允许执行的服务端指令」名单。
 *
 * <p>名单里存的是根命令字面量（如 help、list、seed）。非白名单玩家执行命令时，
 * 若该命令所属的根命令在名单中则放行；否则仍受白名单规则限制。</p>
 */
public class CommandAllowCommand implements CommandRegistrationCallback, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.EndDataPackReload {
	@Override public void onServerStarted(@NonNull MinecraftServer server) { loadCommandAllow(server); }
	@Override public void endDataPackReload(@NonNull MinecraftServer server, @NonNull CloseableResourceManager resourceManager, boolean success) { loadCommandAllow(server); }

	public static class CommandAllowData {
		final Set<String> commands = new LinkedHashSet<>();
		JsonElement getAsJsonElement() {
			JsonArray res = new JsonArray();
			for(String command : commands) res.add(command);
			return res;
		}
		void setValueFromJsonElement(JsonElement element) {
			commands.clear();
			if(element instanceof JsonArray commandsArray) {
				for(JsonElement e : commandsArray)
					if(e instanceof JsonPrimitive primitive)
						commands.add(primitive.getAsString());
					else LPCCarpetAddition.LOGGER.warn("Invalid entry in commandAllow, ignored: {}", e);
			}
		}
	}

	/** 非白名单玩家执行命令时，命令是否在允许名单中（按根命令字面量匹配）。 */
	public static boolean isAllowed(MinecraftServer server, String commandLiteral) {
		return extraData.getExtraData(server).commands.contains(commandLiteral);
	}

	@Override public void register(CommandDispatcher<CommandSourceStack> dispatcher,
								   @NonNull CommandBuildContext context,
								   Commands.@NonNull CommandSelection selection) {
		dispatcher.register(Commands.literal("commandallow")
			.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
				&& LPCCarpetSettings.rejectNonWhitelistedPlayerExecuteServerCommand)
			.then(Commands.literal("add")
				.then(Commands.argument(commandArg, StringArgumentType.word())
					.suggests(CommandAllowCommand::suggestAdd)
					.executes(CommandAllowCommand::add)
				)
			)
			.then(Commands.literal("remove")
				.then(Commands.argument(commandArg, StringArgumentType.word())
					.suggests(CommandAllowCommand::suggestRemove)
					.executes(CommandAllowCommand::remove)
				)
				.then(Commands.literal("all")
					.executes(CommandAllowCommand::removeAll)
				)
			)
			.then(Commands.literal("list")
				.executes(CommandAllowCommand::list)
			)
			.then(Commands.literal("reload")
				.executes(CommandAllowCommand::reload)
			)
			.then(Commands.literal("help")
				.executes(CommandAllowCommand::help)
			)
		);
	}

	public static CommandAllowCommand getInstance() { return instance; }

	private static final ServerExtraData.ExtraDataRegistry<CommandAllowData> extraData
		= ServerExtraData.register(CommandAllowData.class, CommandAllowData::new);

	private static final CommandAllowCommand instance = new CommandAllowCommand();
	private static final String commandArg = "command";
	private static final DynamicCommandExceptionType ERROR_NOT_FOUND = new DynamicCommandExceptionType(
		name -> CommandUtils.fixTranslatedText("carpet.lpc.command.commandallow.notFound", name));
	private static final DynamicCommandExceptionType ERROR_NOT_IN_LIST = new DynamicCommandExceptionType(
		name -> CommandUtils.fixTranslatedText("carpet.lpc.command.commandallow.notInList", name));

	private static String literalOf(CommandNode<?> node) {
		return node instanceof LiteralCommandNode<?> lit ? lit.getLiteral() : null;
	}

	private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		String command = StringArgumentType.getString(context, commandArg);
		if(!isRootCommand(context.getSource().getServer(), command))
			throw ERROR_NOT_FOUND.create(command);
		CommandAllowData data = extraData.getExtraData(context);
		if(data.commands.add(command)) {
			context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
				"carpet.lpc.command.commandallow.added", command), true);
			saveCommandAllow(context);
			refreshCommandTree(context.getSource().getServer());
		} else {
			context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
				"carpet.lpc.command.commandallow.alreadyInList", command), false);
		}
		return 1;
	}

	private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		String command = StringArgumentType.getString(context, commandArg);
		CommandAllowData data = extraData.getExtraData(context);
		if(!data.commands.remove(command)) throw ERROR_NOT_IN_LIST.create(command);
		context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
			"carpet.lpc.command.commandallow.removed", command), true);
		saveCommandAllow(context);
		refreshCommandTree(context.getSource().getServer());
		return 1;
	}

	private static int removeAll(CommandContext<CommandSourceStack> context) {
		CommandAllowData data = extraData.getExtraData(context);
		data.commands.clear();
		context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.commandallow.removedAll"), true);
		saveCommandAllow(context);
		refreshCommandTree(context.getSource().getServer());
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		CommandAllowData data = extraData.getExtraData(context);
		MutableComponent component = Component.empty();
		component.append(CommandUtils.fixTranslatedText("carpet.lpc.command.commandallow.listHeadline").withColor(TextColor.YELLOW)).append(":");
		if(data.commands.isEmpty()) {
			component.append(" ").append(CommandUtils.fixTranslatedText("carpet.lpc.command.commandallow.emptyList"));
		} else {
			for(String command : data.commands)
				component.append("\n    ").append(Component.literal("/" + command).withColor(TextColor.AQUA));
		}
		context.getSource().sendSystemMessage(component);
		return 1;
	}

	private static int reload(CommandContext<CommandSourceStack> context) {
		loadCommandAllow(context.getSource().getServer());
		context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.commandallow.reloaded"), true);
		return 1;
	}

	private static int help(CommandContext<CommandSourceStack> context) {
		context.getSource().sendSystemMessage(Component.literal(CommandUtils.loadHelpText("commandallow")));
		return 1;
	}

	private static boolean isRootCommand(MinecraftServer server, String name) {
		return server.getCommands().getDispatcher().getRoot().getChildren().stream()
			.anyMatch(node -> name.equals(literalOf(node)));
	}

	/** 名单变化后，向所有在线玩家重新发送命令树，让命令可见性（requires）及时更新。 */
	private static void refreshCommandTree(MinecraftServer server) {
		Commands commands = server.getCommands();
		for(ServerPlayer player : server.getPlayerList().getPlayers())
			commands.sendCommands(player);
	}

	private static Path jsonPath(MinecraftServer server) {
		return server.getWorldPath(LevelResource.DATA).resolve("lpcCarpetAddition").resolve("commandAllow.json");
	}

	private static void loadCommandAllow(MinecraftServer server) {
		Path path = jsonPath(server);
		try {
			if(Files.exists(path)) extraData.getExtraData(server).setValueFromJsonElement(JsonParser.parseReader(Files.newBufferedReader(path)));
		} catch (IOException ioException) {
			LPCCarpetAddition.LOGGER.warn("Failed to load commandAllow", ioException);
		}
	}

	private static void saveCommandAllow(CommandContext<CommandSourceStack> context) {
		saveCommandAllow(context.getSource().getServer());
	}

	private static void saveCommandAllow(MinecraftServer server) {
		Path path = jsonPath(server);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				new GsonBuilder().setPrettyPrinting().create()
					.toJson(extraData.getExtraData(server).getAsJsonElement(), writer);
			}
		} catch (IOException ioException) {
			LPCCarpetAddition.LOGGER.warn("Failed to save commandAllow", ioException);
		}
	}

	private static CompletableFuture<Suggestions> suggestAdd(CommandContext<CommandSourceStack> c, SuggestionsBuilder p) {
		CommandAllowData data = extraData.getExtraData(c);
		Stream<String> registered = c.getSource().getServer().getCommands().getDispatcher().getRoot().getChildren().stream()
			.map(CommandAllowCommand::literalOf).filter(Objects::nonNull);
		return SharedSuggestionProvider.suggest(registered.filter(name -> !data.commands.contains(name)), p);
	}

	private static CompletableFuture<Suggestions> suggestRemove(CommandContext<CommandSourceStack> c, SuggestionsBuilder p) {
		return SharedSuggestionProvider.suggest(extraData.getExtraData(c).commands.stream(), p);
	}
}
