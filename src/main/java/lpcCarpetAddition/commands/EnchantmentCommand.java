package lpcCarpetAddition.commands;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpcCarpetAddition.LPCCarpetAddition;
import lpcCarpetAddition.utils.CommandUtils;
import lpcCarpetAddition.utils.ServerExtraData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.WeakHashMap;

public class EnchantmentCommand implements CommandRegistrationCallback, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.EndDataPackReload {
    @Override public void onServerStarted(@NonNull MinecraftServer server) { updateEnchantmentServerCache(server); loadEnchantmentLimits(server); }
    @Override public void endDataPackReload(@NonNull MinecraftServer server, @NonNull CloseableResourceManager resourceManager, boolean success) { updateEnchantmentServerCache(server); }

    public enum LimitType {
        ANVIL,
        SINGLE_RANDOM_ENCHANTMENT,
        MULTI_RANDOM_ENCHANTMENTS,
        ENCHANT_COMMAND;
        public Enchantment2LevelMap getLimitMap(MinecraftServer server) {
            return extraData.getExtraData(server).limitMap.get(this);
        }
        public Enchantment2LevelMap getLimitMap(CommandSourceStack sourceStack) {
            return extraData.getExtraData(sourceStack).limitMap.get(this);
        }
        public Enchantment2LevelMap getLimitMap(CommandContext<? extends CommandSourceStack> context) {
            return extraData.getExtraData(context).limitMap.get(this);
        }
        public Enchantment2LevelMap getLimitMap(ServerLevel level) {
            return getLimitMap(level.getServer());
        }
        public Enchantment2LevelMap getLimitMap(ServerPlayer player) {
            return getLimitMap(player.level());
        }
        public Enchantment2LevelMap getLimitMap(LootContext context) {
            return getLimitMap(context.getLevel());
        }
        public String key() { return name().toLowerCase(); }
        public MutableComponent description() { return CommandUtils.fixTranslatedText("carpet.lpc.command.enchantment.limit.type." + key()); }
    }

    @Override public void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                   @NonNull CommandBuildContext context,
                                   Commands.@NonNull CommandSelection selection) {
        dispatcher.register(Commands.literal("enchantment")
            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("limit")
                .then(Commands.literal("set")
                    .then(Commands.argument(enchantmentIdArg, ResourceArgument.resource(context, Registries.ENCHANTMENT))
                        .then(Commands.argument(limitTypeArg, StringArgumentType.word())
                            .suggests(CommandUtils.suggestInEnum(LimitType.class))
                            .then(Commands.argument(limitLevelArg, IntegerArgumentType.integer())
                                .executes(EnchantmentCommand::setLimit)
                            )
                        )
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument(enchantmentIdArg, ResourceArgument.resource(context, Registries.ENCHANTMENT))
                        .then(Commands.argument(limitTypeArg, StringArgumentType.word())
                            .suggests(CommandUtils.suggestInEnum(LimitType.class))
                            .executes(EnchantmentCommand::removeLimit)
                        )
                        .executes(EnchantmentCommand::removeLimitWithoutType)
                    )
                    .then(Commands.literal("all")
                        .executes(EnchantmentCommand::removeLimitAll)
                    )
                )
                .then(Commands.literal("reload")
                    .executes(EnchantmentCommand::reload)
                )
                .then(Commands.literal("list")
                    .executes(EnchantmentCommand::list)
                )
            )
            .then(Commands.literal("help")
                .executes(EnchantmentCommand::help)
            )
        );
    }

    public static EnchantmentCommand getInstance() { return instance; }

    public static class Enchantment2LevelMap extends Object2IntOpenHashMap<Identifier> {
        private final MinecraftServer server;
        private Enchantment2LevelMap(MinecraftServer server) { this.server = server; }
		public int getOrDefault(Enchantment enchantment, int fallback) {
            return getOrDefault(CommandUtils.getEnchantmentId(server, enchantment), fallback);
        }
        public int getInt(Enchantment enchantment) {
            return getInt(CommandUtils.getEnchantmentId(server, enchantment));
        }
        public boolean containsKey(Enchantment enchantment) {
            return containsKey(CommandUtils.getEnchantmentId(server, enchantment));
        }
    }

    private static class EnchantmentLimitData {
        final EnumMap<LimitType, Enchantment2LevelMap> limitMap = new EnumMap<>(LimitType.class);
        EnchantmentLimitData(MinecraftServer server) { for(var e : LimitType.values()) limitMap.put(e, new Enchantment2LevelMap(server)); }
        JsonElement getAsJsonElement() {
            JsonObject res = new JsonObject();
            for(var type2valuesEntry : limitMap.entrySet()) {
                JsonObject sub = new JsonObject();
                for(var valuesEntry : type2valuesEntry.getValue().object2IntEntrySet())
                    sub.addProperty(valuesEntry.getKey().toString(), valuesEntry.getIntValue());
                res.add(type2valuesEntry.getKey().key(), sub);
            }
            return res;
        }
        void setValueFromJsonElement(JsonElement element) {
            limitMap.values().forEach(Object2IntOpenHashMap::clear);
            if(element instanceof JsonObject root) {
                for(var type2valuesEntry : limitMap.entrySet()) {
                    JsonElement subElement = root.get(type2valuesEntry.getKey().key());
                    if(subElement instanceof JsonObject sub) {
                        for(var entry : sub.entrySet()) {
                            JsonElement value = entry.getValue();
                            if(value instanceof JsonPrimitive primitive) {
                                try {
                                    type2valuesEntry.getValue().put(Identifier.parse(entry.getKey()), primitive.getAsInt());
                                } catch (NumberFormatException _) {}
                            }
                        }
                    }
                }
            }
        }
    }

    public static MinecraftServer getEnchantmentServer(Enchantment enchantment) { return enchantmentServerCache.get(enchantment); }

    private static final ServerExtraData.ExtraDataRegistry<EnchantmentLimitData> extraData
        = ServerExtraData.register(EnchantmentLimitData.class, EnchantmentLimitData::new);

    private static final WeakHashMap<Enchantment, MinecraftServer> enchantmentServerCache = new WeakHashMap<>();

    private static final EnchantmentCommand instance = new EnchantmentCommand();
    private static final String enchantmentIdArg = "enchantmentId";
    private static final String limitTypeArg = "limitType";
    private static final String limitLevelArg = "limitLevel";
    private static final DynamicCommandExceptionType ERROR_INVALID_LIMIT_TYPE = new DynamicCommandExceptionType(
            name -> CommandUtils.fixTranslatedText("carpet.lpc.command.enchantment.limit.invalidType", name));

    private static Enchantment enchantment(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ResourceArgument.getEnchantment(context, enchantmentIdArg).value();
    }

    private static LimitType limitType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, limitTypeArg);
        for(LimitType type : LimitType.values()) if(type.key().equalsIgnoreCase(name)) return type;
        throw ERROR_INVALID_LIMIT_TYPE.create(name);
    }

    private static int limitLevel(CommandContext<CommandSourceStack> context) {
        return IntegerArgumentType.getInteger(context, limitLevelArg);
    }

    private static int setLimit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Enchantment enchantment = enchantment(context);
        LimitType type = limitType(context);
        int level = limitLevel(context);
        type.getLimitMap(context).put(CommandUtils.getEnchantmentId(context, enchantment), level);
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
                "carpet.lpc.command.enchantment.limit.set",
                enchantment.description(), type.description(), level), true);
        saveEnchantmentLimits(context);
        return 1;
    }

    private static int removeLimit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Enchantment enchantment = enchantment(context);
        LimitType type = limitType(context);
        type.getLimitMap(context).removeInt(enchantment);
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
                "carpet.lpc.command.enchantment.limit.remove",
                enchantment.description(), type.description()), true);
        saveEnchantmentLimits(context);
        return 1;
    }

    private static int removeLimitWithoutType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Enchantment enchantment = enchantment(context);
        extraData.getExtraData(context).limitMap.values().forEach(m->m.removeInt(enchantment));
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText(
                "carpet.lpc.command.enchantment.limit.removeAllForEnchantment",
                enchantment.description()), true);
        saveEnchantmentLimits(context);
        return 1;
    }

    private static int removeLimitAll(CommandContext<CommandSourceStack> context) {
        extraData.getExtraData(context).limitMap.values().forEach(Object2IntOpenHashMap::clear);
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.enchantment.limit.removeAll"), true);
        saveEnchantmentLimits(context);
        return 1;
    }

    private static Path jsonPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.DATA).resolve("lpcCarpetAddition").resolve("enchantmentLimit.json");
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        MutableComponent component = Component.empty();
        component.append(CommandUtils.fixTranslatedText("carpet.lpc.command.enchantment.limit.listHeadline").withColor(TextColor.YELLOW)).append(":");
        var registry = context.getSource().getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        boolean allEmpty = true;
        for(var mappingsEntry : extraData.getExtraData(context).limitMap.entrySet()) {
            if(mappingsEntry.getValue().isEmpty()) continue;
            allEmpty = false;
            component.append("\n    ").append(mappingsEntry.getKey().description().withColor(TextColor.GREEN)).append(":");
            for(var entry : mappingsEntry.getValue().object2IntEntrySet()) {
                component.append("\n        ");
                var enchantment = registry.getOptional(entry.getKey());
                if(enchantment.isPresent()) component.append(enchantment.get().description().copy().withColor(TextColor.AQUA));
                else component.append("\"" + entry.getKey().toString() + "\"");
                component.append(": ").append(String.valueOf(entry.getIntValue()));
            }
        }
        if(allEmpty) component.append(" ").append(CommandUtils.fixTranslatedText("carpet.lpc.command.enchantment.limit.emptyLimits"));
        context.getSource().sendSystemMessage(component);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        loadEnchantmentLimits(context.getSource().getServer());
        context.getSource().sendSuccess(() -> CommandUtils.fixTranslatedText("carpet.lpc.command.enchantment.limit.reloaded"), true);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal(CommandUtils.loadHelpText("enchantment")));
        return 1;
    }

    private static void loadEnchantmentLimits(MinecraftServer server) {
        Path path = jsonPath(server);
        try {
            if(Files.exists(path)) extraData.getExtraData(server).setValueFromJsonElement(JsonParser.parseReader(Files.newBufferedReader(path)));
        } catch (IOException ioException) {
            LPCCarpetAddition.LOGGER.warn("Failed to load enchantmentLimits", ioException);
        }
	}

    private static void saveEnchantmentLimits(CommandContext<CommandSourceStack> context) {
        saveEnchantmentLimits(context.getSource().getServer());
    }

    private static void saveEnchantmentLimits(MinecraftServer server) {
        Path path = jsonPath(server);
		try {
			Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                new GsonBuilder().setPrettyPrinting().create()
                    .toJson(extraData.getExtraData(server).getAsJsonElement(), writer);
            }
	    } catch (IOException ioException) {
            LPCCarpetAddition.LOGGER.warn("Failed to save enchantmentLimits", ioException);
		}
    }

    private static void updateEnchantmentServerCache(MinecraftServer server) {
        for(var val : server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT))
            enchantmentServerCache.put(val, server);
    }
}
