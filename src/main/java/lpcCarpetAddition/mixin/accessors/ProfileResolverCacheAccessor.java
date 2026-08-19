package lpcCarpetAddition.mixin.accessors;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.UUID;

/**
 * 暴露 {@code ProfileResolver.Cached} 的 Guava 缓存，用于切换匹配规则后 invalidateAll() 清理。
 * 目标类是 package-private 嵌套类，故用字符串 target。
 */
@Mixin(targets = "net.minecraft.server.players.ProfileResolver$Cached")
public interface ProfileResolverCacheAccessor {
	@Accessor LoadingCache<String, Optional<GameProfile>> getProfileCacheByName();
	@Accessor LoadingCache<UUID, Optional<GameProfile>> getProfileCacheById();
}
