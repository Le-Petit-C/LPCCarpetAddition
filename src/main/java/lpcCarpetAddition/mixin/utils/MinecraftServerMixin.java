package lpcCarpetAddition.mixin.utils;

import com.google.common.collect.MutableClassToInstanceMap;
import lpcCarpetAddition.utils.ServerExtraData;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements ServerExtraData.ExtraDataSupplier {
	private final @Unique MutableClassToInstanceMap<Object> serverExtraData = MutableClassToInstanceMap.create();
	@Override public MutableClassToInstanceMap<Object> lpctools$getClassToInstanceMapMap() { return serverExtraData; }
}
