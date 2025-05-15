package lpcCarpetAddition.Utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.doubles.DoubleIterable;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

import static lpcCarpetAddition.Utils.CommandUtils.*;

@SuppressWarnings("unused")
public class DataUtils {
    public static DoubleIterable iterableFrom(Vec3d vec){
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
        return server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
    }
    public static Iterable<EnchantmentRecord> getEnchantmentRecords(MinecraftServer server){
        return new Iterable<>() {
            @Override public @NotNull Iterator<EnchantmentRecord> iterator() {
                return new Iterator<>() {
                    private final Iterator<Map.Entry<RegistryKey<Enchantment>, Enchantment>>
                            iterator = getEnchantments(server).getEntrySet().iterator();
                    @Override public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    @Override public EnchantmentRecord next() {
                        Map.Entry<RegistryKey<Enchantment>, Enchantment> next = iterator.next();
                        return new EnchantmentRecord(next.getKey(), next.getValue());
                    }
                };
            }
        };
    }
    public static @Nullable DataUtils.EnchantmentRecord getEnchantment(MinecraftServer server, String enchantmentId){
        for(EnchantmentRecord enchantment : getEnchantmentRecords(server)){
            if(enchantment.getIdAsString().equals(enchantmentId)) return enchantment;
            if(enchantment.getIdAsString().equals(enchantmentId)) return enchantment;
        }
        return null;
    }
    public static @NotNull DataUtils.EnchantmentRecord getEnchantmentOrThrow(MinecraftServer server, String enchantmentId) throws CommandSyntaxException {
        EnchantmentRecord result = getEnchantment(server, enchantmentId);
        if(result == null) throw createEnchantmentSyntaxException(enchantmentId);
        return result;
    }
    public record EnchantmentRecord(RegistryKey<Enchantment> key, Enchantment enchantment){
        public static EnchantmentRecord of(Map.Entry<RegistryKey<Enchantment>, Enchantment> entry){
            return new EnchantmentRecord(entry.getKey(), entry.getValue());
        }
        public String getIdAsString(){
            return key.getValue().toString();
        }
    }
}
