package lpcCarpetAddition.mixin.accessors.modifiers;

import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FuelValues.class)
public class FuelValuesModifier {
	@Shadow @Final @Mutable private Object2IntSortedMap<Item> values;
}
