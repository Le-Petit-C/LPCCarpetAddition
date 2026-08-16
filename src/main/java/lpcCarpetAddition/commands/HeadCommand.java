package lpcCarpetAddition.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.utils.CommandUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static lpcCarpetAddition.utils.CommandUtils.fixTranslatedText;

public class HeadCommand implements CommandRegistrationCallback {
    private static final DynamicCommandExceptionType FAILED_NOT_PLAYER_HEAD = new DynamicCommandExceptionType(ignored -> fixTranslatedText("carpet.lpc.command.head.fail.notPlayerHead"));
    private static final DynamicCommandExceptionType FAILED_NOT_BLANK_OR_YOUR = new DynamicCommandExceptionType(ignored -> fixTranslatedText("carpet.lpc.command.head.fail.notBlankOrYourHead"));
    public static HeadCommand getInstance(){return instance;}
    @Override public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, @NonNull CommandBuildContext commandRegistryAccess, Commands.@NonNull CommandSelection registrationEnvironment) {
        commandDispatcher.register(enchantmentCommandBuilder);
    }
    private static final HeadCommand instance = new HeadCommand();
    private static final @NotNull LiteralArgumentBuilder<CommandSourceStack> enchantmentCommandBuilder = buildHeadCommand();
    private static @NotNull LiteralArgumentBuilder<CommandSourceStack> buildHeadCommand(){
        LiteralArgumentBuilder<CommandSourceStack> result = Commands.literal("head");
        result.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || LPCCarpetSettings.commandHead);
        result.executes(context -> giveHead(context.getSource()));
        result.then(Commands.literal("help").executes(HeadCommand::help));
        return result;
    }
    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal(CommandUtils.loadHelpText("head")));
        return 1;
    }
    private static int giveHead(CommandSourceStack source) throws CommandSyntaxException {
        if(source.getEntity() instanceof ServerPlayer player){
            var mainHandStack = player.getInventory().getSelectedItem();
            if(mainHandStack.getItem() != Items.PLAYER_HEAD) throw FAILED_NOT_PLAYER_HEAD.create(null);
            if(mainHandStack.getComponents().isEmpty()){
                ItemStack stack = new ItemStack(Items.PLAYER_HEAD, mainHandStack.getCount());
                stack.applyComponents(DataComponentPatch.builder()
                    .set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(player.getGameProfile().name()))
                    .build());
                player.getInventory().setSelectedItem(stack);
            }
            else {
                boolean proceeded = false;
                for(var component : mainHandStack.getComponents()){
                    if(component.type().equals(DataComponents.PROFILE) && component.value() instanceof ResolvableProfile profileComponent){
                        var profile = profileComponent.partialProfile();
                        var playerProfile = player.getGameProfile();
                        if(profile.name().equals(playerProfile.name()) || profile.id().equals(playerProfile.id())){
                            proceeded = true;
                            player.getInventory().setSelectedItem(new ItemStack(Items.PLAYER_HEAD, mainHandStack.getCount()));
                            break;
                        }
                        else throw FAILED_NOT_BLANK_OR_YOUR.create(null);
                    }
                }
                if(!proceeded) {
                    ItemStack stack = new ItemStack(Items.PLAYER_HEAD, mainHandStack.getCount());
                    stack.applyComponents(DataComponentPatch.builder()
                        .set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(player.getGameProfile().name()))
                        .build());
                    player.getInventory().setSelectedItem(stack);
                }
            }
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.slotsChanged(player.getInventory());
            return 1;
        }
        else return 0;
    }
}
