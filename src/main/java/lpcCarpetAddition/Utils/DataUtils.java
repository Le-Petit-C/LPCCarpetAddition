package lpcCarpetAddition.Utils;

import it.unimi.dsi.fastutil.doubles.DoubleIterable;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

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
}
