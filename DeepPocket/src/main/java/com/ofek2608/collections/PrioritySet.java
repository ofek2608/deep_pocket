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
// * @see PrioritySet#empty
// * @see PrioritySet#insert
// * @see PrioritySet#extract
// * @see PrioritySet#searchElem
// */
//@ParametersAreNonnullByDefault
//@MethodsReturnNonnullByDefault
//@Immutable
//public final class PrioritySet<T> {
//	private final T element;
//	private final int priority;
//	private final PrioritySet<T> less;
//	private final PrioritySet<T> more;
//	private final boolean red;
//
//	/**
//	 * @param element  the element of this node
//	 * @param priority the priority of this node
//	 * @param less     a tree containing elements with lower or equal priority
//	 * @param more     a tree containing elements with higher priority
//	 * @param red      if the node is red according to black red tree
//	 */
//	private PrioritySet(T element, int priority, @Nullable PrioritySet<T> less, @Nullable PrioritySet<T> more, boolean red) {
//		this.element = element;
//		this.priority = priority;
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
//	public static <T> @Nullable PrioritySet<T> empty() {
//		return null;
//	}
//
//	private static <T> boolean isNodeEquals(@Nullable PrioritySet<T> set, T elem, int priority) {
//		return set != null && set.priority == priority && elem.equals(set.element);
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
//	public static <T> PrioritySet<T> insert(@Nullable PrioritySet<T> set, T elem, int priority) {
//		// Inserting to an empty set
//		if (set == null)
//			return new PrioritySet<>(elem, priority, null, null, false);
//		// Inserting to a tree when the root is the element
//		if (isNodeEquals(set, elem, priority))
//			return set;
//		//TODO implement
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
//	public static <T> @Nullable PrioritySet<T> extract(@Nullable PrioritySet<T> set, T elem, int priority) {
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
//	public static <T> @Nullable T searchElem(@Nullable PrioritySet<T> set, Predicate<T> predicate) {
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
