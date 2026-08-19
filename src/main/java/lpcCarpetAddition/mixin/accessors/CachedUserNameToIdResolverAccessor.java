package lpcCarpetAddition.mixin.accessors;

import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

/**
 * 暴露 {@code CachedUserNameToIdResolver} 的名字/UUID 内存缓存，用于切换匹配规则后清空。
 */
@Mixin(CachedUserNameToIdResolver.class)
public interface CachedUserNameToIdResolverAccessor {
	@Accessor Map<String, ?> getProfilesByName();
	@Accessor Map<UUID, ?> getProfilesByUUID();
}
