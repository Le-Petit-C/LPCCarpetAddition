package lpcCarpetAddition.mixin;

import lpcCarpetAddition.utils.TextEx;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import lpcCarpetAddition.loggers.EnderPearlLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlEntityMixin{
	@Unique ThrownEnderpearl getThis(){return (ThrownEnderpearl)(Object)this; }
	@Inject(method = "onRemoval", at = @At("RETURN"))
	void onPearlRemove(Entity.RemovalReason reason, CallbackInfo ci){
		if(!EnderPearlLogger.isEnabled) return;
		if(getThis().level().isClientSide()) return;
		Entity owner = getThis().getOwner();
		MutableComponent dataText = Component.literal("");
		if(owner != null) dataText.append("Pearl by ").append(TextEx.hoverEntity(Component.literal(owner.getName().getString()), owner)).append(" : ");
		Component[] texts = new Component[]{
			TextEx.setColor(Component.literal("tick : ").append(Component.nullToEmpty(String.valueOf(getThis().level().getGameTime()))), 0xFFAA00),
			TextEx.appendPos(dataText, getThis().position())
		};
		EnderPearlLogger.getInstance().log((_, player)->{
			if(!player.equals(getThis().getOwner())) return null;
			else return texts;
		});
	}
}