package lpcCarpetAddition.mixin.accessors.modifiers;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityType.class)
public class EntityTypeModifier {
	@Shadow @Final @Mutable private MobCategory category;
}
