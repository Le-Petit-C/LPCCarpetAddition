package lpcCarpetAddition.mixin;

import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.class) public class ModifyUnbreakingFunctionMixin {
	@Unique Enchantment getThis(){return (Enchantment)(Object)this;}
	@Inject(method = "modifyDurabilityChange", at = @At("HEAD"), cancellable = true)
	void modifyItemDamageHead(ServerLevel world, int level, ItemStack stack, MutableFloat itemDamage, CallbackInfo ci){
		if(LPCCarpetSettings.modifyUnbreakingFunction
			&& getThis() == world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getValue(Enchantments.UNBREAKING)
			&& !stack.is(ItemTags.ARMOR_ENCHANTABLE)){
			float v = itemDamage.floatValue();
			var random = world.getRandom();
			int i = 0;
			//noinspection IntegerDivisionInFloatingPointContext
			float chance = 1.0f / (level + 1 + level * (level - 1) * (level - 2) * (level - 3) / 24);
			for(int k = 0; k < v; ++k){
				if(random.nextFloat() > chance)
					++i;
			}
			itemDamage.setValue(v - i);
			ci.cancel();
		}
	}
}
