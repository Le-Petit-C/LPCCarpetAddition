package lpcCarpetAddition.mixin.furnaceClear;

import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceBlockEntityMixin extends LockableContainerBlockEntity {
	protected FurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {super(blockEntityType, blockPos, blockState);}
	@Shadow @Final private ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;
	@Shadow protected DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
	@Shadow int litTimeRemaining;
	@Shadow int litTotalTime;
	@Shadow int cookingTimeSpent;
	@Shadow int cookingTotalTime;
	@Shadow protected abstract boolean isBurning();
	@Shadow protected abstract int getFuelTime(FuelRegistry fuelRegistry, ItemStack stack);
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private static void injectTickHead(ServerWorld world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
		if(!LPCCarpetSettings.furnaceClear) return;
		FurnaceBlockEntityMixin be = (FurnaceBlockEntityMixin)(Object)blockEntity;
		assert be != null;
		ItemStack inputStack = be.inventory.getFirst();
		if(inputStack.isEmpty()) return;
		if(be.matchGetter.getFirstMatch(new SingleStackRecipeInput(inputStack), world).isPresent()) return;
		ItemStack outputStack = be.inventory.get(2);
		var clearMethod = LPCCarpetSettings.furnaceClearMode;
		if(!clearMethod.canClear(inputStack, outputStack)) return;
		if(clearMethod.needSmelt) {
			boolean oldIsBurning = be.isBurning();
			if(!be.isBurning()) {
				ItemStack fuelStack = be.inventory.get(1);
				be.litTimeRemaining = be.getFuelTime(world.getFuelRegistry(), fuelStack);
				be.litTotalTime = be.litTimeRemaining;
				if (be.isBurning()) {
					Item item = fuelStack.getItem();
					fuelStack.decrement(1);
					if (fuelStack.isEmpty())
						be.inventory.set(1, item.getRecipeRemainder());
					be.markDirty();
				}
			}
			if (be.isBurning()) {
				++be.cookingTimeSpent;
				--be.litTimeRemaining;
				if (be.cookingTimeSpent == be.cookingTotalTime) {
					be.cookingTimeSpent = 0;
					be.cookingTotalTime = getCookTime(world, blockEntity);
					if(clearMethod.hasResult) be.inventory.set(2, new ItemStack(inputStack.getItem(), outputStack.getCount() + 1));
					inputStack.decrement(1);
					be.markDirty();
				}
			}
			if(be.isBurning() != oldIsBurning) {
				state = state.with(AbstractFurnaceBlock.LIT, be.isBurning());
				world.setBlockState(pos, state, Block.NOTIFY_ALL);
			}
			ci.cancel();
		}
		else {
			if(clearMethod.hasResult) {
				int moved = Math.min(inputStack.getCount(), outputStack.getMaxCount() - outputStack.getCount());
				if(moved == 0) return;
				be.inventory.set(2, new ItemStack(inputStack.getItem(), outputStack.getCount() + moved));
				inputStack.decrement(moved);
			}
			else inputStack.setCount(0);
			be.markDirty();
		}
	}
	
	@Shadow private static int getCookTime(ServerWorld world, AbstractFurnaceBlockEntity blockEntity) {
		return 0;
	}
}
