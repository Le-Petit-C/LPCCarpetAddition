package lpcCarpetAddition.mixin.client;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends HandledScreen<AnvilScreenHandler> {
	public AnvilScreenMixin(AnvilScreenHandler handler, PlayerInventory inventory, Text title) {super(handler, inventory, title);}
	@ModifyConstant(method = "drawForeground", constant = @Constant(intValue = 40))
	private int modifyTooExpensiveConstant(int constant) {
		if(handler.getSlot(0).hasStack() && handler.getSlot(1).hasStack() && !handler.getSlot(2).hasStack()) return Integer.MIN_VALUE;
		else return Integer.MAX_VALUE;
	}
}