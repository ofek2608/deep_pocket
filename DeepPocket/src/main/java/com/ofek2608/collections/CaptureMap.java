package com.ofek2608.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"SuspiciousToArrayCall", "SuspiciousMethodCalls", "unchecked"})
public class CaptureMap<K,V> implements Map<K,V> {
	private final HashMap<K,V> internal;
	private Snapshot lastSnapshot = new Snapshot();
	private KeySet keySet;
	private Values values;
	private EntrySet entrySet;

	public CaptureMap(@Range(from = 0, to = Integer.MAX_VALUE) int initialCapacity, float loadFactor) {
		this.internal = new HashMap<>(initialCapacity, loadFactor);
	}

	public CaptureMap(@Range(from = 0, to = Integer.MAX_VALUE) int initialCapacity) {
		this.internal = new HashMap<>(initialCapacity);
	}

	public CaptureMap() {
		this.internal = new HashMap<>();
	}

	public CaptureMap(Map<? extends K, ? extends V> m) {
		this.internal = new HashMap<>(m);
	}


	@Override
	public int size() {
		return internal.size();
	}

	@Override
	public boolean isEmpty() {
		return internal.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return internal.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internal.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return internal.getOrDefault(key, defaultValue(key));
	}

	@Nullable
	@Override
	public V put(K key, V value) {
		validateKey(key);
		value = validateValue(value);
		if (Objects.equals(defaultValue(key), value))
			return remove(key);
		V oldValue = internal.put(key, value);
		if (!Objects.equals(oldValue, value))
			lastSnapshot.added.add(key);
		return oldValue;
	}

	public boolean putIfCan(K key, V value) {
		if (internal.containsKey(key))
			return false;
		try {
			validateKey(key);
			value = validateValue(value);
		} catch (Exception exception) {
			return false;
		}
		internal.put(key, value);
		return true;
	}

	@Override
	public V remove(Object key) {
		if (!internal.containsKey(key))
			return null;
		lastSnapshot.removed.add((K)key);
		lastSnapshot.added.remove(key);
		return internal.remove(key);
	}

	public boolean removeIfCan(Object key) {
		if (!internal.containsKey(key))
			return false;
		lastSnapshot.removed.add((K)key);
		lastSnapshot.added.remove(key);
		internal.remove(key);
		return true;
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		m.forEach(this::put);
	}

	@Override
	public void clear() {
		lastSnapshot.added.clear();
		lastSnapshot.removed.addAll(internal.keySet());
		internal.clear();
	}

	@NotNull
	@Override
	public Set<K> keySet() {
		KeySet keySet = this.keySet;
		return keySet == null ? this.keySet = new KeySet() : keySet;
	}

	@NotNull
	@Override
	public Collection<V> values() {
		Values values = this.values;
		return values == null ? this.values = new Values() : values;
	}

	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		EntrySet entrySet = this.entrySet;
		return entrySet == null ? this.entrySet = new EntrySet() : entrySet;
	}

	public Snapshot createSnapshot() {
		return lastSnapshot = lastSnapshot.next = new Snapshot();
	}



	//For use in an extending class
	public void validateKey(K key) throws IllegalArgumentException { }
	public V validateValue(V val) throws IllegalArgumentException { return val; }
	public V defaultValue(Object key) { return null; }



	public class Snapshot {
		private Snapshot next;
		protected final Set<K> added = new HashSet<>();
		protected final Set<K> removed = new HashSet<>();

		protected Snapshot() {}

		public void simplify() {
			if (next == null || next.next == null)
				return;
			next.simplify();
			this.removed.removeAll(next.added);
			this.added.removeAll(next.removed);
			this.removed.addAll(next.removed);
			this.added.addAll(next.added);
			next = next.next;
		}

		public @UnmodifiableView Set<K> getAddedKeys() {
			return Collections.unmodifiableSet(added);
		}

		public @UnmodifiableView List<V> getAddedValues() {
			return added.stream().map(CaptureMap.this::get).toList();
		}

		public @UnmodifiableView Set<K> getRemovedKeys() {
			return Collections.unmodifiableSet(removed);
		}

		public @UnmodifiableView Map<K,V> getAddedAsMap() {
			HashMap<K,V> result = new HashMap<>();
			added.forEach(key->result.put(key, internal.get(key)));
			return result;
		}

		public @UnmodifiableView Map<K,V> getChangedAsMap() {
			HashMap<K,V> result = new HashMap<>();
			added.forEach(key->result.put(key, internal.get(key)));
			removed.forEach(key->result.put(key, defaultValue(key)));
			return result;
		}
	}











	final class KeySet extends AbstractSet<K> {
		private final Set<K> internalKeySet = internal.keySet();
		@Override public int size() { return CaptureMap.this.size(); }
		@Override public void clear() { CaptureMap.this.clear(); }
		@Override public @NotNull Iterator<K> iterator() { return new KeyIterator(); }
		@Override public boolean contains(Object o) { return CaptureMap.this.containsKey(o); }
		@Override public boolean remove(Object o) { return CaptureMap.this.removeIfCan(o); }
		@Override public @NotNull Spliterator<K> spliterator() { return internalKeySet.spliterator(); }
		@Override public Object @NotNull [] toArray() { return internalKeySet.toArray(); }
		@Override public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) { return internalKeySet.toArray(a); }
		@Override public void forEach(Consumer<? super K> action) { internalKeySet.forEach(action); }
	}

	final class Values extends AbstractCollection<V> {
		private final Collection<V> internalValues = internal.values();
		public int size()                 { return CaptureMap.this.size(); }
		public void clear()               { CaptureMap.this.clear(); }
		public @NotNull Iterator<V> iterator()     { return new ValueIterator(); }
		public boolean contains(Object o) { return containsValue(o); }
		public @NotNull Spliterator<V> spliterator() {
			return internalValues.spliterator();
		}
		public Object @NotNull [] toArray() { return internalValues.toArray(); }
		public <T> T @NotNull [] toArray(T @NotNull [] a) { return internalValues.toArray(a); }
		public void forEach(Consumer<? super V> action) { internalValues.forEach(action); }
	}

	final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
		private final Set<Map.Entry<K,V>> internalEntrySet = internal.entrySet();
		public int size()                 { return CaptureMap.this.size(); }
		public void clear()               { CaptureMap.this.clear(); }
		public @NotNull Iterator<Map.Entry<K,V>> iterator() { return new EntryIterator(); }
		public boolean contains(Object o) { return internalEntrySet.contains(o); }
		public boolean remove(Object o) { return contains(o) && o instanceof Map.Entry<?, ?> e && CaptureMap.this.removeIfCan(e.getKey()); }
		public @NotNull Spliterator<Map.Entry<K,V>> spliterator() { return internalEntrySet.spliterator(); }
		public void forEach(Consumer<? super Map.Entry<K,V>> action) { internalEntrySet.forEach(action); }
	}



	/* ------------------------------------------------------------ */
	// iterators

	abstract class HashIterator {
		private final Iterator<Entry<K,V>> internalIterator = internal.entrySet().iterator();
		private Entry<K,V> current;

		public final boolean hasNext() {
			return internalIterator.hasNext();
		}

		final Entry<K,V> nextNode() {
			return current = internalIterator.next();
		}

		public final void remove() {
			Entry<K,V> p = current;
			if (p == null)
				throw new IllegalStateException();
			K key = p.getKey();
			internalIterator.remove();
			lastSnapshot.removed.add(key);
			lastSnapshot.added.remove(key);
			current = null;
		}
	}

	final class KeyIterator extends HashIterator implements Iterator<K> {
		public K next() { return nextNode().getKey(); }
	}

	final class ValueIterator extends HashIterator implements Iterator<V> {
		public V next() { return nextNode().getValue(); }
	}

	final class EntryIterator extends HashIterator implements Iterator<Map.Entry<K,V>> {
		public Map.Entry<K,V> next() { return nextNode(); }
	}
}
