package lpcCarpetAddition.validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import lpcCarpetAddition.features.whitelist.NonWhitelistedPlayerJoinMode;
import lpcCarpetAddition.features.whitelist.WhitelistMethods;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

public class NonWhitelistValidator extends Validator<lpcCarpetAddition.features.whitelist.NonWhitelistedPlayerJoinMode> {
	@Override public NonWhitelistedPlayerJoinMode validate(@Nullable CommandSourceStack source, CarpetRule<NonWhitelistedPlayerJoinMode> changingRule, NonWhitelistedPlayerJoinMode newValue, String userInput) {
		if(source != null) WhitelistMethods.scheduleUpdatePlayersGameMode(source.getServer(), false);
		return newValue;
	}
}
