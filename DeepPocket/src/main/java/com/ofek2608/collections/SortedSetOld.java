//package com.ofek2608.collections;
//
//import net.minecraft.MethodsReturnNonnullByDefault;
//
//import javax.annotation.Nullable;
//import javax.annotation.ParametersAreNonnullByDefault;
//import javax.annotation.concurrent.Immutable;
//import java.util.function.Predicate;
//
///**
// * An immutable set of (T element, int priority) which you insert and extract using {@code insert} and {@code extract} functions.
// * You can also go over the set in the order of the priority with {@code searchElem}.
// * <p>Internally it uses a black red tree.</p>
// *
// * @see SortedSetOld#empty
// * @see SortedSetOld#insert
// * @see SortedSetOld#extract
// * @see SortedSetOld#searchElem
// */
//@ParametersAreNonnullByDefault
//@MethodsReturnNonnullByDefault
//@Immutable
//public final class SortedSetOld<T extends Comparable<T>> {
//	private final T element;
//	private final SortedSetOld<T> less;
//	private final SortedSetOld<T> more;
//	private final boolean red;
//
//	/**
//	 * @param element  the element of this node
//	 * @param less     a tree containing elements that satisfy {@code lessElem.compareTo(element) < 0 }
//	 * @param more     a tree containing elements that satisfy {@code lessElem.compareTo(element) > 0 }
//	 * @param red      if the node is red according to black red tree rules
//	 */
//	private SortedSetOld(T element, @Nullable SortedSetOld<T> less, @Nullable SortedSetOld<T> more, boolean red) {
//		this.element = element;
//		this.less = less;
//		this.more = more;
//		this.red = red;
//	}
//
//	/**
//	 * Complexity: {@code O(log(1))}
//	 * This method always returns null.
//	 * This method can be used if you want to explicitly say that you want an empty set.
//	 * For example {@code PrioritySet<T> newSet = empty();}.
//	 * @return an empty tree, same as just null.
//	 */
//	public static <T extends Comparable<T>> @Nullable SortedSetOld<T> empty() {
//		return null;
//	}
//
//	/**
//	 * Complexity: {@code O(log(n))}
//	 * @param set      an existing set
//	 * @param elem     the new element to be inserted
//	 * @param priority the priority of that element
//	 * @return a new tree which contains {@code elem} as well as all the elements of {@code set}.
//	 *         if ({@code elem},{@code priority}) is inside the set already, it returns {@code set}.
//	 *         it means {@code insert(set, existingElem, existingElemPriority) == set}.
//	 */
//	public static <T extends Comparable<T>> SortedSetOld<T> insert(@Nullable SortedSetOld<T> set, T elem, int priority) {
//		// Inserting to an empty set
//		if (set == null)
//			return new SortedSetOld<>(elem, null, null, false);
//		// Inserting to a tree when the root is the element
//		int side = elem.compareTo(set.element);
//		if (side == 0)
//			return set;
//		//TODO implement
//		//If side < 0 should add to less. if side > 0 should add to more.
//		//Probably should use some other recursive function that has parent and maybe also grandparent/uncle.
//	}
//
//	/**
//	 * Complexity: {@code O(log(n))}
//	 * @param set      an existing set
//	 * @param elem     the element to be removed
//	 * @param priority the priority of that element
//	 * @return a new tree which contains all the elements of {@code set} except {@code elem}.
//	 *         if ({@code elem},{@code priority}) is <i>not</i> inside the set already, it returns {@code set}.
//	 *         it means {@code extract(set, missingElem, missingElemPriority) == set}.
//	 */
//	public static <T extends Comparable<T>> @Nullable SortedSetOld<T> extract(@Nullable SortedSetOld<T> set, T elem, int priority) {
//		// Extracting from an empty set
//		if (set == null)
//			return null;
//		//TODO implement
//	}
//
//	/**
//	 * Complexity: {@code O(n)}
//	 * @param set       the set to search at
//	 * @param predicate the predicate of the element
//	 * @return the first element which matches {@code predicate}
//	 */
//	public static <T extends Comparable<T>> @Nullable T searchElem(@Nullable SortedSetOld<T> set, Predicate<T> predicate) {
//		if (set == null)
//			return null;
//		T result = searchElem(set.less, predicate);
//		if (result != null)
//			return result;
//		if (predicate.test(set.element))
//			return set.element;
//		return searchElem(set.more, predicate);
//	}
//}
