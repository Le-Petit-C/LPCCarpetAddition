package lpcCarpetAddition.mixinUtils;

import com.mojang.brigadier.builder.ArgumentBuilder;
import lpcCarpetAddition.mixin.accessors.ArgumentBuilderAccessor;

@SuppressWarnings("unchecked")
public class AccessorUtils {
	public static <S, T extends ArgumentBuilder<S, T>> ArgumentBuilderAccessor<S, T> asAccessor(ArgumentBuilder<S, T> o) { return (ArgumentBuilderAccessor<S, T>)o; }
}
