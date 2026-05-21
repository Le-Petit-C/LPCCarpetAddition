package lpcCarpetAddition.mixin.client;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {
	public AnvilScreenMixin(AnvilMenu menu, Inventory inventory, Component title, Identifier menuResource) {
		super(menu, inventory, title, menuResource);
	}
	@ModifyConstant(method = "extractLabels", constant = @Constant(intValue = 40))
	private int modifyTooExpensiveConstant(int constant) {
		if(menu.getSlot(0).hasItem() && menu.getSlot(1).hasItem() && !menu.getSlot(2).hasItem()) return Integer.MIN_VALUE;
		else return Integer.MAX_VALUE;
	}
}