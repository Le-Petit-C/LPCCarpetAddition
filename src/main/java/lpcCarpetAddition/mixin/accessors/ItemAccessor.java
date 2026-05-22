package lpcCarpetAddition.mixin.accessors;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {
	@Accessor Holder.Reference<Item> getBuiltInRegistryHolder();
}
