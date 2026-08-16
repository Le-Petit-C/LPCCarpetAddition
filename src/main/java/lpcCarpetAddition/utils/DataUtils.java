package lpcCarpetAddition.utils;

import it.unimi.dsi.fastutil.doubles.DoubleIterable;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("unused")
public class DataUtils {
    public static DoubleIterable iterableFrom(Vec3 vec){
        return new DoubleIterable() {
            final double[] array = new double[]{vec.x, vec.y, vec.z};
            @Override public @NotNull DoubleIterator iterator() {
                return new DoubleIterator() {
                    int n = 0;
                    @Override public double nextDouble() {
                        return array[n++];
                    }
                    @Override public boolean hasNext() {
                        return n < array.length;
                    }
                };
            }
        };
    }
    public static @Nullable String getModVersion(String modId){
        for(ModContainer mod : FabricLoader.getInstance().getAllMods()){
            if(modId.equals(mod.getMetadata().getId()))
                return mod.getMetadata().getVersion().getFriendlyString();
        }
        return null;
    }
    public static @NotNull String getModVersionWithDefault(String modId, String def){
        String ret = getModVersion(modId);
        if(ret != null) return ret;
        else return def;
    }
    public static @NotNull String getModVersionWithDefault(String modId){
        String ret = getModVersion(modId);
        if(ret != null) return ret;
        else return "Failed to get mod version of " + modId + " mod";
    }
    public static Registry<Enchantment> getEnchantments(MinecraftServer server){
        return server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    }
    public record EnchantmentRecord(ResourceKey<Enchantment> key, Enchantment enchantment){
        public static EnchantmentRecord of(Map.Entry<ResourceKey<Enchantment>, Enchantment> entry){
            return new EnchantmentRecord(entry.getKey(), entry.getValue());
        }
        public String getIdAsString(){
            return key.identifier().toString();
        }
    }
}
