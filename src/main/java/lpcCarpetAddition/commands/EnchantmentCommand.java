package lpcCarpetAddition.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import static lpcCarpetAddition.Utils.CommandUtils.*;
import static lpcCarpetAddition.Utils.DataUtils.*;

public class EnchantmentCommand implements CommandRegistrationCallback {
    public static EnchantmentCommand getInstance(){return instance;}
    @Override public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(enchantmentCommandBuilder);
    }
    private static final EnchantmentCommand instance = new EnchantmentCommand();
    private static final String argumentEnchantmentId = "enchantmentId";
    private static final @NotNull LiteralArgumentBuilder<ServerCommandSource> enchantmentCommandBuilder = buildEnchantmentCommand();
    private static @NotNull LiteralArgumentBuilder<ServerCommandSource> buildEnchantmentCommand(){
        LiteralArgumentBuilder<ServerCommandSource> result = CommandManager.literal("enchantment");
        result.then(buildEnchantmentLimitCommand());
        return result;
    }
    private static @NotNull LiteralArgumentBuilder<ServerCommandSource> buildEnchantmentLimitCommand(){
        LiteralArgumentBuilder<ServerCommandSource> result = CommandManager.literal("limit");
        result.then(
                CommandManager.argument(argumentEnchantmentId, StringArgumentType.greedyString())
                        .suggests(enchantmentSuggester)
                        .executes(context -> {
                            EnchantmentRecord enchantment = getEnchantment(context, argumentEnchantmentId);
                            for(ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList())
                                player.sendMessage(enchantment.enchantment().description());
                            return 1;
                        })
        );
        return result;
    }
}
