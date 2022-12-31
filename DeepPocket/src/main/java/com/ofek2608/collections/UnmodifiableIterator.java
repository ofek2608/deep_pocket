package com.ofek2608.collections;

import java.util.Iterator;
import java.util.function.Consumer;

public final class UnmodifiableIterator<E> implements Iterator<E> {
	private final Iterator<E> iter;
	
	public UnmodifiableIterator(Iterator<E> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}
	
	@Override
	public E next() {
		return iter.next();
	}
	
	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		iter.forEachRemaining(action);
	}
}
