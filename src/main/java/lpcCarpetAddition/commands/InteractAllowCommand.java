package lpcCarpetAddition.commands;

import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import lpcCarpetAddition.LPCCarpetAddition;
import lpcCarpetAddition.utils.CommandUtils;
import lpcCarpetAddition.utils.ServerExtraData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class InteractAllowCommand implements CommandRegistrationCallback, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.EndDataPackReload {
    @Override public void onServerStarted(@NonNull MinecraftServer server) { loadInteractAllow(server); }
    @Override public void endDataPackReload(@NonNull MinecraftServer server, @NonNull CloseableResourceManager resourceManager, boolean success) { loadInteractAllow(server); }

    public static class InteractAllowData {
        final Set<Block> blocks = new LinkedHashSet<>();
        final Set<TagKey<Block>> tags = new LinkedHashSet<>();
        JsonElement getAsJsonElement() {
            JsonObject res = new JsonObject();
            JsonArray blocksArray = new JsonArray();
            for(Block key : blocks) blocksArray.add(BuiltInRegistries.BLOCK.getKey(key).toString());
            res.add("blocks", blocksArray);
            JsonArray tagsArray = new JsonArray();
            for(TagKey<Block> tag : tags) tagsArray.add(tag.location().toString());
            res.add("tags", tagsArray);
            return res;
        }
        void setValueFromJsonElement(JsonElement element) {
            blocks.clear();
            tags.clear();
            if(element instanceof JsonObject root) {
                if(root.get("blocks") instanceof JsonArray blocksArray)
                    for(JsonElement e : blocksArray) {
                        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(e.getAsString()));
                        if(block.isPresent()) blocks.add(block.get());
                        else LPCCarpetAddition.LOGGER.warn("Unknown block in interactAllow list, ignored: {}", e.getAsString());
                    }
                if(root.get("tags") instanceof JsonArray tagsArray) {
                    var blockTags = new HashMap<Identifier, TagKey<Block>>();
                    BuiltInRegistries.BLOCK.listTagIds().forEach(
                        tag -> blockTags.put(tag.location(), tag)
                    );
                    for(JsonElement e : tagsArray) {
                        if(e instanceof JsonPrimitive primitive) {
                            TagKey<Block> tag = blockTags.get(Identifier.parse(primitive.getAsString()));
                            if(tag != null) tags.add(tag);
                            else LPCCarpetAddition.LOGGER.warn("Unknown tag in interactAllow list, ignored: {}", primitive.getAsString());
                        }
                        else LPCCarpetAddition.LOGGER.warn("Invalid tag entry in interactAllow list, ignored: {}", e);
                    }
                }
            }
        }
    }

    /** 非白名单玩家在非潜行状态下交互该方块时是否放行。 */
    public static boolean shouldAllow(Level level, BlockPos pos) {
        InteractAllowData data = extraData.getExtraData(level.getServer());
        if(data.blocks.isEmpty() && data.tags.isEmpty()) return false;
        BlockState state = level.getBlockState(pos);
        if(data.blocks.contains(state.getBlock())) return true;
        for(TagKey<Block> tag : data.tags) if(state.is(tag)) return true;
        return false;
    }

    @Override public void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                   @NonNull CommandBuildContext context,
                                   Commands.@NonNull CommandSelection selection) {
        dispatcher.register(Commands.literal("interactallow")
            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("add")
                .then(Commands.argument(blockArg, ResourceOrTagKeyArgument.resourceOrTagKey(Registries.BLOCK))
                    .executes(InteractAllowCommand::add)
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument(blockArg, ResourceOrTagKeyArgument.resourceOrTagKey(Registries.BLOCK))
                    .suggests(InteractAllowCommand::suggestRemove)
                    .executes(InteractAllowCommand::remove)
                )
                .then(Commands.literal("all")
                    .executes(InteractAllowCommand::removeAll)
                )
            )
            .then(Commands.literal("list")
                .executes(InteractAllowCommand::list)
            )
            .then(Commands.literal("reload")
                .executes(InteractAllowCommand::reload)
            )
            .then(Commands.literal("help")
                .executes(InteractAllowCommand::help)
            )
        );
    }

    public static InteractAllowCommand getInstance() { return instance; }

    private static final ServerExtraData.ExtraDataRegistry<InteractAllowData> extraData
        = ServerExtraData.register(InteractAllowData.class, InteractAllowData::new);

    private static final InteractAllowCommand instance = new InteractAllowCommand();
    private static final String blockArg = "block";
    private static final DynamicCommandExceptionType ERROR_NOT_FOUND = new DynamicCommandExceptionType(
        name -> CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.notFound", name));
    private static final DynamicCommandExceptionType ERROR_NOT_IN_LIST = new DynamicCommandExceptionType(
        name -> CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.notInList", name));
    private record ArgParseResult(String type, String printable, Either<Block, TagKey<Block>> result) {
        static Optional<ArgParseResult> parseBlock(ResourceKey<Block> blockKey, String printable) {
            Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(blockKey);
            return block.map(value -> new ArgParseResult("block", printable, Either.left(value)));
        }
        static Optional<ArgParseResult> parseTag(TagKey<Block> blockKey, String printable) {
            Optional<TagKey<Block>> block = BuiltInRegistries.BLOCK.listTagIds().filter(k -> k.equals(blockKey)).findFirst();
            return block.map(value -> new ArgParseResult("tag", printable, Either.right(value)));
        }
        static ArgParseResult parseArg(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            ResourceOrTagKeyArgument.Result<Block> result = ResourceOrTagKeyArgument.getResourceOrTagKey(context, blockArg, Registries.BLOCK, ERROR_NOT_FOUND);
            String printable = result.asPrintable();
            return result.unwrap().map(
                block -> parseBlock(block, printable),
                tag -> parseTag(tag, printable)
            ).orElseThrow(() -> ERROR_NOT_FOUND.create(printable));
        }
    }


    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ArgParseResult result = ArgParseResult.parseArg(context);
        var data = extraData.getExtraData(context);
        boolean added = result.result.map(data.blocks::add, data.tags::add);
        String translationKey = "carpet.lpc.command.interactallow." + (added ? "added" : "alreadyInList");
		context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(translationKey, result.printable,
			CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.kind." + result.type)), added);
		if(added) saveInteractAllow(context);
	    return 1;
	}

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ArgParseResult result = ArgParseResult.parseArg(context);
        InteractAllowData data = extraData.getExtraData(context);
        boolean removed = result.result.map(data.blocks::remove, data.tags::remove);
        if(!removed) throw ERROR_NOT_IN_LIST.create(result.printable);
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.removed", result.printable), true);
        saveInteractAllow(context);
        return 1;
    }

    private static int removeAll(CommandContext<CommandSourceStack> context) {
        InteractAllowData data = extraData.getExtraData(context);
        data.blocks.clear();
        data.tags.clear();
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.removedAll"), true);
        saveInteractAllow(context);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        InteractAllowData data = extraData.getExtraData(context);
        MutableComponent component = Component.empty();
        component.append(CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.listHeadline").withColor(TextColor.YELLOW)).append(":");
        if(data.blocks.isEmpty() && data.tags.isEmpty()) {
            component.append(" ").append(CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.emptyList"));
        } else {
            if(!data.blocks.isEmpty()) {
                component.append("\n    ").append(CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.kind.block").withColor(TextColor.GREEN)).append(":");
                for(Block block : data.blocks)
                    component.append("\n        ").append(Component.literal(BuiltInRegistries.BLOCK.getKey(block).toString()).withColor(TextColor.AQUA));
            }
            if(!data.tags.isEmpty()) {
                component.append("\n    ").append(CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.kind.tag").withColor(TextColor.GREEN)).append(":");
                for(TagKey<Block> tag : data.tags)
                    component.append("\n        ").append(Component.literal(tag.location().toString()).withColor(TextColor.AQUA));
            }
        }
        context.getSource().sendSystemMessage(component);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        loadInteractAllow(context.getSource().getServer());
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.interactallow.reloaded"), true);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal(CommandUtils.loadHelpText("interactallow")));
        return 1;
    }

    private static Path jsonPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.DATA).resolve("lpcCarpetAddition").resolve("interactAllow.json");
    }

    private static void loadInteractAllow(MinecraftServer server) {
        Path path = jsonPath(server);
        try {
            if(Files.exists(path)) extraData.getExtraData(server).setValueFromJsonElement(JsonParser.parseReader(Files.newBufferedReader(path)));
        } catch (IOException ioException) {
            LPCCarpetAddition.LOGGER.warn("Failed to load interactAllow", ioException);
        }
    }

    private static void saveInteractAllow(CommandContext<CommandSourceStack> context) {
        saveInteractAllow(context.getSource().getServer());
    }

    private static void saveInteractAllow(MinecraftServer server) {
        Path path = jsonPath(server);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                new GsonBuilder().setPrettyPrinting().create()
                    .toJson(extraData.getExtraData(server).getAsJsonElement(), writer);
            }
        } catch (IOException ioException) {
            LPCCarpetAddition.LOGGER.warn("Failed to save interactAllow", ioException);
        }
    }

    private static CompletableFuture<Suggestions> suggestRemove(CommandContext<CommandSourceStack> c, SuggestionsBuilder p) {
        var data = extraData.getExtraData(c);
        return SharedSuggestionProvider.suggest(Stream.concat(
            data.blocks.stream().map(block -> BuiltInRegistries.BLOCK.getKey(block).toString()),
            data.tags.stream().map(TagKey::toString)
        ), p);
    }
}
