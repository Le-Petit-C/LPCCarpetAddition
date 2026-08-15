package lpcCarpetAddition.validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import lpcCarpetAddition.mixin.accessors.EntityTypeAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.Nullable;

public class PhantomsCountAsMonsterValidator extends Validator<Boolean> {
	@Override public Boolean validate(@Nullable CommandSourceStack source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
		((EntityTypeAccessor)EntityType.PHANTOM).setCategory(newValue ? MobCategory.MONSTER : MobCategory.MISC);
		return newValue;
	}
}
