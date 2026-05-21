package lpcCarpetAddition.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlockMixin {
    @Inject(method = "getInputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;getAnalogOutput()I", shift = At.Shift.AFTER))
    void scheduleMoreWhenTestingItemFrame(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> cir, @Local(name = "itemFrame") ItemFrame itemFrame){
        if(!LPCCarpetSettings.comparatorGetsRealTime) return;
        if(itemFrame.getItem().getItem() != Items.CLOCK) return;
        if(level instanceof ServerLevel serverWorld)
            serverWorld.scheduleTick(pos, state.getBlock(), 1);
    }
}
