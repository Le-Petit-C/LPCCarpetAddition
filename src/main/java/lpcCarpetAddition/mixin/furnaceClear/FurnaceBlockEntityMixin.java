package lpcCarpetAddition.mixin.furnaceClear;

import lpcCarpetAddition.LPCCarpetSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceBlockEntityMixin extends BaseContainerBlockEntity {
	protected FurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {super(blockEntityType, blockPos, blockState);}
	@Shadow @Final private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;
	@Shadow protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
	@Shadow int litTimeRemaining;
	@Shadow int litTotalTime;
	@Shadow int cookingTimer;
	@Shadow int cookingTotalTime;
	@Shadow protected abstract boolean isLit();
	@Shadow protected abstract int getBurnDuration(FuelValues fuelRegistry, ItemStack stack);
	@Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
	private static void injectTickHead(ServerLevel world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
		if(!LPCCarpetSettings.furnaceClear) return;
		FurnaceBlockEntityMixin be = (FurnaceBlockEntityMixin)(Object)blockEntity;
		assert be != null;
		ItemStack inputStack = be.items.getFirst();
		if(inputStack.isEmpty()) return;
		if(be.quickCheck.getRecipeFor(new SingleRecipeInput(inputStack), world).isPresent()) return;
		ItemStack outputStack = be.items.get(2);
		var clearMethod = LPCCarpetSettings.furnaceClearMode;
		if(!clearMethod.canClear(inputStack, outputStack)) return;
		if(clearMethod.needSmelt) {
			boolean oldIsBurning = be.isLit();
			if(!be.isLit()) {
				ItemStack fuelStack = be.items.get(1);
				be.litTimeRemaining = be.getBurnDuration(world.fuelValues(), fuelStack);
				be.litTotalTime = be.litTimeRemaining;
				if (be.isLit()) {
					Item item = fuelStack.getItem();
					fuelStack.shrink(1);
					if (fuelStack.isEmpty())
						be.items.set(1, item.getCraftingRemainder());
					be.setChanged();
				}
			}
			if (be.isLit()) {
				++be.cookingTimer;
				--be.litTimeRemaining;
				if (be.cookingTimer == be.cookingTotalTime) {
					be.cookingTimer = 0;
					be.cookingTotalTime = getTotalCookTime(world, blockEntity);
					if(clearMethod.hasResult) be.items.set(2, new ItemStack(inputStack.getItem(), outputStack.getCount() + 1));
					inputStack.shrink(1);
					be.setChanged();
				}
			}
			if(be.isLit() != oldIsBurning) {
				state = state.setValue(AbstractFurnaceBlock.LIT, be.isLit());
				world.setBlock(pos, state, Block.UPDATE_ALL);
			}
			ci.cancel();
		}
		else {
			if(clearMethod.hasResult) {
				int moved = Math.min(inputStack.getCount(), outputStack.getMaxStackSize() - outputStack.getCount());
				if(moved == 0) return;
				be.items.set(2, new ItemStack(inputStack.getItem(), outputStack.getCount() + moved));
				inputStack.shrink(moved);
			}
			else inputStack.setCount(0);
			be.setChanged();
		}
	}
	
	@Shadow private static int getTotalCookTime(ServerLevel world, AbstractFurnaceBlockEntity blockEntity) {
		return 0;
	}
}
