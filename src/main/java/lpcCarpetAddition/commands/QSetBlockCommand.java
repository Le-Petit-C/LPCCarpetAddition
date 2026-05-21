package lpcCarpetAddition.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import lpcCarpetAddition.utils.CommandUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class QSetBlockCommand implements CommandRegistrationCallback {
    public static QSetBlockCommand getInstance(){return instance;}
    @Override public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, @NonNull CommandBuildContext commandRegistryAccess, Commands.@NonNull CommandSelection registrationEnvironment) {
		//noinspection SpellCheckingInspection
		var setBlockNode = commandDispatcher.findNode(List.of("setblock"));
        commandDispatcher.register(buildEnchantmentCommand(setBlockNode));
    }
    private static final QSetBlockCommand instance = new QSetBlockCommand();
    private static @NotNull LiteralArgumentBuilder<CommandSourceStack> buildEnchantmentCommand(CommandNode<CommandSourceStack> setBlockNode) {
        //noinspection SpellCheckingInspection
        LiteralArgumentBuilder<CommandSourceStack> result = Commands.literal("qsetblock");
        result.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
        result.redirect(setBlockNode, context->{
            CommandUtils.suppressNextFeedBack();
            return context.getSource();
        });
        return result;
    }
}
