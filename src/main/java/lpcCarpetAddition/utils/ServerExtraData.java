package lpcCarpetAddition.utils;

import com.google.common.collect.MutableClassToInstanceMap;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Supplier;

public class ServerExtraData {
	public interface ExtraDataRegistry<T> {
		T getExtraData(MinecraftServer server);
		default T getExtraData(CommandSourceStack commandSource) {
			return getExtraData(commandSource.getServer());
		}
		default T getExtraData(CommandContext<? extends CommandSourceStack> commandContext) {
			return getExtraData(commandContext.getSource());
		}
	}
	public static <T> ExtraDataRegistry<T> register(Class<T> clazz, Function<MinecraftServer, T> resourceAllocator) {
		synchronized (ServerExtraData.class) {
			if(registeredClasses.contains(clazz))
				throw new IllegalStateException("Class " + clazz + " already registered!");
			registeredClasses.add(clazz);
		}
		return new ExtraData<>(clazz, resourceAllocator);
	}
	public static <T> ExtraDataRegistry<T> register(Class<T> clazz, Supplier<T> resourceAllocator) {
		return register(clazz, _->resourceAllocator.get());
	}
	
	public static class ExtraData<T> implements ExtraDataRegistry<T> {
		public final Function<MinecraftServer, T> resourceAllocator;
		public final Class<T> resourceClass;
		private ExtraData(Class<T> resourceClass, Function<MinecraftServer, T> resourceAllocator) {
			this.resourceAllocator = resourceAllocator;
			this.resourceClass = resourceClass;
		}
		@Override public T getExtraData(MinecraftServer server) {
			MutableClassToInstanceMap<Object> map = ((ExtraDataSupplier)server).lpctools$getClassToInstanceMapMap();
			synchronized(map) {
				if(!map.containsKey(resourceClass))
					map.put(resourceClass, resourceAllocator.apply(server));
				return map.getInstance(resourceClass);
			}
		}
	}
	
	public interface ExtraDataSupplier {
		MutableClassToInstanceMap<Object> lpctools$getClassToInstanceMapMap();
	}
	
	private static final HashSet<Class<?>> registeredClasses = new HashSet<>();
}
