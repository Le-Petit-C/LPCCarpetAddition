package lpcCarpetAddition.mixin;

import lpcCarpetAddition.LPCCarpetSettings;
import lpcCarpetAddition.LPCCarpetSettingsData;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public class ItemFrameEntityMixin {
    @Unique ItemFrame getThis(){return (ItemFrame)(Object)this; }
    @Inject(method = "getAnalogOutput", at = @At("RETURN"), cancellable = true)
    void mixinGetComparatorPower(CallbackInfoReturnable<Integer> cir){
        if(!LPCCarpetSettings.comparatorGetsRealTime) return;
        if(getThis().getItem().getItem() != Items.CLOCK) return;
        long sec = System.currentTimeMillis() / 1000;
        BlockState thisBlock = getThis().level().getBlockState(getThis().getPos().relative(getThis().getNearestViewDirection().getOpposite()));
        boolean isHigh32 = thisBlock.getBlock() == LPCCarpetSettingsData.comparatorGetsRealTimeHigh32Block;
        int ret = (int) (((isHigh32 ? sec >> 32 : sec) >> (getThis().getRotation() << 2)) & 15);
        cir.setReturnValue(ret);
    }
}
