package lpcCarpetAddition.features.furnaceClear;

import net.minecraft.world.item.ItemStack;

public enum FurnaceClearMode {
	DIRECT_DESTROY(false, false),	// 立即销毁
	DIRECT_PASS(true, false),		// 立即移动
	SMELT_DESTROY(false, true),		// 熔炼后销毁
	SMELT_PASS(true, true);			// 熔炼后移动
	
	public final boolean hasResult;   // true=移动至输出槽，false=销毁
	public final boolean needSmelt;   // true=需完整熔炼，false=立即执行
	
	FurnaceClearMode(boolean hasResult, boolean needSmelt) {
		this.hasResult = hasResult;
		this.needSmelt = needSmelt;
	}
	
	public boolean canClear(ItemStack inputStack, ItemStack outputStack) {
		if(!hasResult) return true;
		else if(outputStack.getCount() >= outputStack.getMaxStackSize()) return false;
		else return ItemStack.isSameItemSameComponents(inputStack, outputStack) || outputStack.isEmpty();
	}
}
