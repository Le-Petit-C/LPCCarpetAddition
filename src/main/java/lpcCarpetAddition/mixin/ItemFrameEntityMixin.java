package lpcCarpetAddition.mixin;

import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin {
    @Unique ItemFrameEntity getThis(){return (ItemFrameEntity)(Object)this; }
    @Inject(method = "getComparatorPower", at = @At("RETURN"), cancellable = true)
    void mixinGetComparatorPower(CallbackInfoReturnable<Integer> cir){
        if(!LPCCarpetSettings.comparatorGetsRealTime) return;
        if(getThis().getHeldItemStack().getItem() != Items.CLOCK) return;
        long sec = System.currentTimeMillis() / 1000;
        BlockState thisBlock = getThis().getWorld().getBlockState(getThis().getAttachedBlockPos().offset(getThis().getFacing().getOpposite()));
        boolean isHigh32 = thisBlock.getBlock() == LPCCarpetSettings.comparatorGetsRealTimeHigh32Block;
        int ret = (int) (((isHigh32 ? sec >> 32 : sec) >> (getThis().getRotation() << 2)) & 15);
        cir.setReturnValue(ret);
    }
}
