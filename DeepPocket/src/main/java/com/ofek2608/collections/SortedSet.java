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
// * <p>Internally it uses an AVL tree.</p>
// *
// * @see SortedSet#empty
// * @see SortedSet#insert
// * @see SortedSet#extract
// * @see SortedSet#searchElem
// */
//@ParametersAreNonnullByDefault
//@MethodsReturnNonnullByDefault
//@Immutable
//public final class SortedSet<T extends Comparable<T>> {
//	private final T element;
//	private final SortedSet<T> left;
//	private final SortedSet<T> right;
//	private final int height;
//
//	/**
//	 * @param element The element of this node
//	 * @param left    A tree containing elements that satisfy {@code lessElem.compareTo(element) < 0 }
//	 * @param right    A tree containing elements that satisfy {@code lessElem.compareTo(element) > 0 }
//	 */
//	private SortedSet(T element, @Nullable SortedSet<T> left, @Nullable SortedSet<T> right) {
//		this.element = element;
//		this.left = left;
//		this.right = right;
//		this.height = Math.max(getHeight(left), getHeight(right));
//	}
//
//	/**
//	 * Complexity: {@code O(log(1))}
//	 * This method always returns null.
//	 * This method can be used if you want to explicitly say that you want an empty set.
//	 * For example {@code PrioritySet<T> newSet = empty();}.
//	 * @return an empty tree, same as just null.
//	 */
//	public static <T extends Comparable<T>> @Nullable SortedSet<T> empty() {
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
//	public static <T extends Comparable<T>> SortedSet<T> insert(@Nullable SortedSet<T> set, T elem) {
//		// Inserting to an empty set
//		if (set == null)
//			return new SortedSet<>(elem, null, null);
//		// Inserting to a tree when the root is the element
//		int side = elem.compareTo(set.element);
//		if (side == 0)
//			return set;
//		SortedSet<T> left = side < 0 ? insert(set.left, elem) : set.left;
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
//	public static <T extends Comparable<T>> @Nullable SortedSet<T> extract(@Nullable SortedSet<T> set, T elem, int priority) {
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
//	public static <T extends Comparable<T>> @Nullable T searchElem(@Nullable SortedSet<T> set, Predicate<T> predicate) {
//		if (set == null)
//			return null;
//		T result = searchElem(set.left, predicate);
//		if (result != null)
//			return result;
//		if (predicate.test(set.element))
//			return set.element;
//		return searchElem(set.right, predicate);
//	}
//
//
//
//
//	//Helper methods
//
//
//	private static int getHeight(@Nullable SortedSet<?> set) {
//		return set == null ? -1 : set.height;
//	}
//
//	private static int getBalance(@Nullable SortedSet<?> set) {
//		return set == null ? 0 : getHeight(set.left) - getHeight(set.right);
//	}
//
//	private static <T extends Comparable<T>> SortedSet<T> rotateLeft(SortedSet<T> set) {
//		return new SortedSet<>(
//				set.right.element,
//				new SortedSet<>(set.element, set.left, set.right.left),
//				set.right.right
//		);
//	}
//
//	private static <T extends Comparable<T>> SortedSet<T> rotateRight(SortedSet<T> set) {
//		return new SortedSet<>(
//				set.left.element,
//				set.left.left,
//				new SortedSet<>(set.element, set.right, set.left.right)
//		);
//	}
//}
