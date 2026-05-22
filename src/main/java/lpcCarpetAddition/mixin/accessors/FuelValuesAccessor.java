package lpcCarpetAddition.mixin.accessors;

import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FuelValues.class)
public interface FuelValuesAccessor {
	@Accessor Object2IntSortedMap<Item> getValues();
	@Accessor void setValues(Object2IntSortedMap<Item> values);
}
