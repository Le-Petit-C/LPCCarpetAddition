package lpcCarpetAddition.validators;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import lpcCarpetAddition.mixin.accessors.CachedUserNameToIdResolverAccessor;
import lpcCarpetAddition.mixin.accessors.ProfileResolverCacheAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.jetbrains.annotations.Nullable;

public class PlayerListRulesValidator extends Validator<Boolean> {
	@Override public Boolean validate(@Nullable CommandSourceStack source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
		if (source != null) {
			var server = source.getServer();
			server.schedule(server.wrapRunnable(()->clearResolutionCaches(server)));
		}
		return newValue;
	}

	/** 切换匹配规则后：清空所有联网解析缓存并重载白名单，使新规则立即生效（无需重启）。 */
	private static void clearResolutionCaches(MinecraftServer server) {
		ProfileResolverCacheAccessor profileAccessor = (ProfileResolverCacheAccessor)server.services().profileResolver();
		profileAccessor.getProfileCacheByName().invalidateAll();
		profileAccessor.getProfileCacheById().invalidateAll();
		if (server.services().nameToIdCache() instanceof CachedUserNameToIdResolver cached) {
			CachedUserNameToIdResolverAccessor accessor = (CachedUserNameToIdResolverAccessor)cached;
			accessor.getProfilesByName().clear();
			accessor.getProfilesByUUID().clear();
		}
		server.getPlayerList().reloadWhiteList();
	}
}
