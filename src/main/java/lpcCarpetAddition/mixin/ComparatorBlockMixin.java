package lpcCarpetAddition.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlockMixin {
    @Inject(method = "getPower", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;getComparatorPower()I", shift = At.Shift.AFTER))
    void scheduleMoreWhenTestingItemFrame(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> cir, @Local ItemFrameEntity itemFrameEntity){
        if(!LPCCarpetSettings.comparatorGetsRealTime) return;
        if(itemFrameEntity.getHeldItemStack().getItem() != Items.CLOCK) return;
        if(world instanceof ServerWorld serverWorld)
            serverWorld.scheduleBlockTick(pos, state.getBlock(), 1);
    }
}
