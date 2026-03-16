package lpcCarpetAddition.mixin;

import lpcCarpetAddition.Utils.TextEx;
import lpcCarpetAddition.loggers.EnderPearlLogger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin{
	@Unique EnderPearlEntity getThis(){return (EnderPearlEntity)(Object)this; }
	@Inject(method = "onRemove", at = @At("RETURN"))
	void onPearlRemove(Entity.RemovalReason reason, CallbackInfo ci){
		if(!EnderPearlLogger.isEnabled) return;
		if(getThis().getEntityWorld().isClient()) return;
		Entity owner = getThis().getOwner();
		MutableText dataText = Text.literal("");
		if(owner != null) dataText.append("Pearl by ").append(TextEx.hoverEntity(Text.literal(owner.getName().getString()), owner)).append(" : ");
		Text[] texts = new Text[]{
			TextEx.setColor(Text.literal("tick : ").append(Text.of(String.valueOf(getThis().getEntityWorld().getTime()))), 0xFFAA00),
			TextEx.appendPos(dataText, getThis().getEntityPos())
		};
		EnderPearlLogger.getInstance().log((playerOption, player)->{
			if(!player.equals(getThis().getOwner())) return null;
			else return texts;
		});
	}
}