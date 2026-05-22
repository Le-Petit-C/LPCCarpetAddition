package lpcCarpetAddition.mixin.loveGhastling;

import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.validators.LoveGhastlingValidator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Shadow @Final private PatchedDataComponentMap components;
	
	@Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
	void injectItemStackGeneralInitReturn(Holder<Item> item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
		if (item.value() == Items.DRIED_GHAST) {
			if (LPCCarpetSettings.loveGhastling < 0) {
				this.components.set(DataComponents.FOOD, LoveGhastlingValidator.getGhastlingFoodProperties());
				this.components.set(DataComponents.CONSUMABLE, LoveGhastlingValidator.getGhastlingConsumable());
			}
		}
	}
}
