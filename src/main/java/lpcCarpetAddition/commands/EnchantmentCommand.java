package lpcCarpetAddition.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lpcCarpetAddition.utils.DataUtils.EnchantmentRecord;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static lpcCarpetAddition.utils.CommandUtils.*;

@Deprecated
public class EnchantmentCommand implements CommandRegistrationCallback {
    @Deprecated
    public static EnchantmentCommand getInstance(){return instance;}
    @Override public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, @NonNull CommandBuildContext commandRegistryAccess, Commands.@NonNull CommandSelection registrationEnvironment) {
        commandDispatcher.register(enchantmentCommandBuilder);
    }
    private static final EnchantmentCommand instance = new EnchantmentCommand();
    private static final String argumentEnchantmentId = "enchantmentId";
    private static final @NotNull LiteralArgumentBuilder<CommandSourceStack> enchantmentCommandBuilder = buildEnchantmentCommand();
    private static @NotNull LiteralArgumentBuilder<CommandSourceStack> buildEnchantmentCommand(){
        LiteralArgumentBuilder<CommandSourceStack> result = Commands.literal("enchantment");
        result.then(buildEnchantmentLimitCommand());
        return result;
    }
    private static @NotNull LiteralArgumentBuilder<CommandSourceStack> buildEnchantmentLimitCommand(){
        LiteralArgumentBuilder<CommandSourceStack> result = Commands.literal("limit");
        result.then(
            Commands.argument(argumentEnchantmentId, StringArgumentType.greedyString())
                .suggests(enchantmentSuggester)
                .executes(context -> {
                    EnchantmentRecord enchantment = getEnchantment(context, argumentEnchantmentId);
                    for(ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers())
                        player.sendSystemMessage(enchantment.enchantment().description());
                    return 1;
                })
        );
        return result;
    }
}
