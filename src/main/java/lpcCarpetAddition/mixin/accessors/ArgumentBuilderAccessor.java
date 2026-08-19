package lpcCarpetAddition.mixin.accessors;

import com.mojang.brigadier.builder.ArgumentBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArgumentBuilder.class)
public interface ArgumentBuilderAccessor<S, T extends ArgumentBuilder<S, T>> {
	@Invoker T invokeGetThis();
}
