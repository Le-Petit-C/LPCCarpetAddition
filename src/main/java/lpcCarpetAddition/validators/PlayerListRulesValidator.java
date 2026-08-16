package lpcCarpetAddition.validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import lpcCarpetAddition.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

public class PlayerListRulesValidator extends Validator<Boolean> {
	@Override public Boolean validate(@Nullable CommandSourceStack source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
		if (source != null) source.sendSystemMessage(CommandUtils.fixTranslatedText("carpet.lpc.validator.playerListRulesWarning"));
		return newValue;
	}
}
