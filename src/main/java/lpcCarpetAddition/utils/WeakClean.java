package lpcCarpetAddition.utils;

import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("unused")
public class WeakClean<T> implements Iterable<T> {
	public void add(T val) {
		WeakReference<T> ref = new WeakReference<>(val);
		synchronized (refs) {
			cleanOnce();
			cleanOnce();
			refs.add(ref);
		}
	}
	
	@Override public @NonNull Iterator<T> iterator() {
		ArrayList<T> list;
		synchronized (refs) {
			list = new ArrayList<>(refs.size());
			int i = 0;
			while (i < refs.size()) {
				var v = refs.get(i).get();
				if (v != null) {
					list.add(v);
					++i;
				}
				else {
					refs.set(i, refs.getLast());
					refs.removeLast();
				}
			}
		}
		list.trimToSize();
		return list.iterator();
	}
	
	private void cleanOnce() {
		if(refs.isEmpty()) return;
		if(++cleanIndex >= refs.size()) cleanIndex = 0;
		var ref = refs.get(cleanIndex);
		if(ref.get() == null) {
			refs.set(cleanIndex, refs.getLast());
			refs.removeLast();
		}
	}
	
	private final ArrayList<WeakReference<T>> refs = new ArrayList<>();
	private int cleanIndex = 0;
}
