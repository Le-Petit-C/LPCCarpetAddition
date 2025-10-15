package lpcCarpetAddition.mixin;

import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.class) public class ModifyUnbreakingFunctionMixin {
	@Unique Enchantment getThis(){return (Enchantment)(Object)this;}
	@Inject(method = "modifyItemDamage", at = @At("HEAD"), cancellable = true)
	void modifyItemDamageHead(ServerWorld world, int level, ItemStack stack, MutableFloat itemDamage, CallbackInfo ci){
		if(LPCCarpetSettings.modifyUnbreakingFunction
			&& getThis() == world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).get(Enchantments.UNBREAKING)
			&& !stack.isIn(ItemTags.ARMOR_ENCHANTABLE)){
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
