package lpcCarpetAddition.validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import lpcCarpetAddition.features.whitelist.WhitelistedPlayerGameMode;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

public class WhitelistValidator extends Validator<WhitelistedPlayerGameMode> {
	@Override public WhitelistedPlayerGameMode validate(@Nullable CommandSourceStack source, CarpetRule<WhitelistedPlayerGameMode> changingRule, WhitelistedPlayerGameMode newValue, String userInput) {
		if(source != null) WhitelistMethods.scheduleUpdatePlayersGameMode(source.getServer(), true);
		return newValue;
	}
}
