package lpcCarpetAddition.commands;

import carpet.utils.Translations;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import lpcCarpetAddition.LPCCarpetSettings;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class HeadCommand implements CommandRegistrationCallback {
    private static Text fixTranslatedText(String translationKey) { return Text.literal(Translations.tr(translationKey)); }
    private static final DynamicCommandExceptionType FAILED_NOT_PLAYER_HEAD = new DynamicCommandExceptionType(ignored -> fixTranslatedText("carpet.lpc.commandHead.fail.notPlayerHead"));
    private static final DynamicCommandExceptionType FAILED_NOT_BLANK_OR_YOUR = new DynamicCommandExceptionType(ignored -> fixTranslatedText("carpet.lpc.commandHead.fail.notBlankOrYourHead"));
    public static HeadCommand getInstance(){return instance;}
    @Override public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(enchantmentCommandBuilder);
    }
    private static final HeadCommand instance = new HeadCommand();
    private static final @NotNull LiteralArgumentBuilder<ServerCommandSource> enchantmentCommandBuilder = buildEnchantmentCommand();
    private static @NotNull LiteralArgumentBuilder<ServerCommandSource> buildEnchantmentCommand(){
        LiteralArgumentBuilder<ServerCommandSource> result = CommandManager.literal("head");
        result.requires(source -> source.hasPermissionLevel(2) || LPCCarpetSettings.commandHead);
        result.executes(context -> giveHead(context.getSource()));
        return result;
    }
    private static int giveHead(ServerCommandSource source) throws CommandSyntaxException {
        if(source.getEntity() instanceof ServerPlayerEntity player){
            var mainHandStack = player.getInventory().getSelectedStack();
            if(mainHandStack.getItem() != Items.PLAYER_HEAD) throw FAILED_NOT_PLAYER_HEAD.create(null);
            if(mainHandStack.getComponents().isEmpty()){
                ItemStack stack = new ItemStack(Items.PLAYER_HEAD, mainHandStack.getCount());
                stack.applyUnvalidatedChanges(ComponentChanges.builder()
                    .add(DataComponentTypes.PROFILE, ProfileComponent.ofDynamic(player.getGameProfile().name()))
                    .build());
                player.getInventory().setSelectedStack(stack);
            }
            else {
                boolean proceeded = false;
                for(var component : mainHandStack.getComponents()){
                    if(component.type().equals(DataComponentTypes.PROFILE) && component.value() instanceof ProfileComponent profileComponent){
                        var profile = profileComponent.getGameProfile();
                        var playerProfile = player.getGameProfile();
                        if(profile.name().equals(playerProfile.name()) || profile.id().equals(playerProfile.id())){
                            proceeded = true;
                            player.getInventory().setSelectedStack(new ItemStack(Items.PLAYER_HEAD, mainHandStack.getCount()));
                            break;
                        }
                        else throw FAILED_NOT_BLANK_OR_YOUR.create(null);
                    }
                }
                if(!proceeded) {
                    ItemStack stack = new ItemStack(Items.PLAYER_HEAD, mainHandStack.getCount());
                    stack.applyUnvalidatedChanges(ComponentChanges.builder()
                        .add(DataComponentTypes.PROFILE, ProfileComponent.ofDynamic(player.getGameProfile().name()))
                        .build());
                    player.getInventory().setSelectedStack(stack);
                }
            }
            player.currentScreenHandler.sendContentUpdates();
            player.playerScreenHandler.onContentChanged(player.getInventory());
            return 1;
        }
        else return 0;
    }
}
