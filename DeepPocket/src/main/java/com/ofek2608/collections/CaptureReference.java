package com.ofek2608.collections;

import java.util.Objects;

public class CaptureReference<T> {
	private T value;
	private Snapshot lastSnapshot;

	public CaptureReference() {}

	public CaptureReference(T initialValue) {
		this.value = initialValue;
	}

	public boolean set(T value) {
		if (Objects.equals(this.value, value))
			return false;
		this.value = value;
		Snapshot lastSnapshot = this.lastSnapshot;
		if (lastSnapshot != null) {
			lastSnapshot.changed = true;
			this.lastSnapshot = null;
		}
		return true;
	}

	public T get() {
		return value;
	}

	public Snapshot createSnapshot() {
		Snapshot lastSnapshot = this.lastSnapshot;
		return lastSnapshot == null ? this.lastSnapshot = new Snapshot() : lastSnapshot;
	}


	public class Snapshot {
		private boolean changed;

		public boolean isChanged() {
			return changed;
		}

		public T getValue() {
			return value;
		}
	}
}
