package lpcCarpetAddition.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import lpcCarpetAddition.commands.QSetBlockCommand;
import lpcCarpetAddition.utils.CommandUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(SetBlockCommand.class)
public class SetBlockCommandMixin {
	@WrapMethod(method = "setBlock")
	private static int injectSetBlockReturn(CommandSourceStack source, BlockPos pos, BlockInput block, SetBlockCommand.Mode mode, @Nullable Predicate<BlockInWorld> predicate, boolean strict, Operation<Integer> original) {
		try {
			return original.call(source, pos, block, mode, predicate, strict);
		} finally {
			var src = QSetBlockCommand.dataRegistry.getExtraData(source);
			if(src.iQSetBlock != 0) {
				--src.iQSetBlock;
				CommandUtils.unsuppressFeedBack();
			}
		}
	}
}
