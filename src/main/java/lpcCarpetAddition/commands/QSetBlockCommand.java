package lpcCarpetAddition.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import lpcCarpetAddition.utils.CommandUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class QSetBlockCommand implements CommandRegistrationCallback {
    public static QSetBlockCommand getInstance(){return instance;}
    @Override public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, @NonNull CommandRegistryAccess commandRegistryAccess, CommandManager.@NonNull RegistrationEnvironment registrationEnvironment) {
		//noinspection SpellCheckingInspection
		var setBlockNode = commandDispatcher.findNode(List.of("setblock"));
        commandDispatcher.register(buildEnchantmentCommand(setBlockNode));
    }
    private static final QSetBlockCommand instance = new QSetBlockCommand();
    private static @NotNull LiteralArgumentBuilder<ServerCommandSource> buildEnchantmentCommand(CommandNode<ServerCommandSource> setBlockNode) {
        //noinspection SpellCheckingInspection
        LiteralArgumentBuilder<ServerCommandSource> result = CommandManager.literal("qsetblock");
        result.requires(source -> source.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS));
        result.redirect(setBlockNode, context->{
            CommandUtils.suppressNextFeedBack();
            return context.getSource();
        });
        return result;
    }
}
