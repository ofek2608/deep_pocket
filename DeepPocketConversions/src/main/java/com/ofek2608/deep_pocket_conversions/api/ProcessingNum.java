package com.ofek2608.deep_pocket_conversions.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

public final class ProcessingNum implements Comparable<ProcessingNum> {
	private static final BigInteger BIG_INT_N2 = BigInteger.valueOf(-1L);
	private static final BigInteger BIG_INT_N1 = BigInteger.valueOf(-1L);
	private static final BigInteger BIG_INT_0 = BigInteger.valueOf(0L);
	private static final BigInteger BIG_INT_1 = BigInteger.valueOf(1L);
	public static final ProcessingNum INFINITE = new ProcessingNum(BIG_INT_N2);
	public static final ProcessingNum UNDEFINED = new ProcessingNum(BIG_INT_N1);
	public static final ProcessingNum ZERO = new ProcessingNum(BIG_INT_0);
	public static final ProcessingNum ONE = new ProcessingNum(BIG_INT_1);

	public static ProcessingNum valueOf(long value) {
		return valueOf(BigInteger.valueOf(value));
	}

	public static ProcessingNum valueOf(@Nullable BigInteger value) {
		if (value == null)
			return UNDEFINED;
		if (value.signum() < 0)
			return INFINITE;
		if (value.signum() == 0)
			return ZERO;
		if (value.equals(BIG_INT_1))
			return ONE;
		return new ProcessingNum(value);
	}

	private final BigInteger value;

	private ProcessingNum(BigInteger value) {
		this.value = value;
	}

	public boolean isFinite() { return this != INFINITE && this != UNDEFINED; }
	public boolean isNotFinite() { return this == INFINITE || this == UNDEFINED; }
	public boolean isInfinite() { return this == INFINITE; }
	public boolean isDefined() { return this != UNDEFINED; }
	public boolean isUndefined() { return this == UNDEFINED; }

	public @Nullable BigInteger asBigInt() {
		if (this == UNDEFINED)
			return null;
		if (this == INFINITE)
			return BIG_INT_N1;
		return value;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ProcessingNum that && this.value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		if (this == UNDEFINED)
			return "Undefined";
		if (this == INFINITE)
			return "Infinite";
		return this.value.toString();
	}

	@Override
	public int compareTo(@NotNull ProcessingNum that) {
		boolean thisFinite = this.isFinite();
		boolean thatFinite = that.isFinite();
		if (thisFinite == thatFinite)
			return this.value.compareTo(that.value);
		return thisFinite ? -1 : 1;
	}

	public ProcessingNum add(ProcessingNum that) {
		if (this == UNDEFINED || that == UNDEFINED)
			return UNDEFINED;
		if (this == INFINITE || that == INFINITE)
			return INFINITE;
		if (this == ZERO)
			return that;
		if (that == ZERO)
			return this;
		return valueOf(this.value.add(that.value));
	}

	public ProcessingNum multiply(ProcessingNum that) {
		if (this == UNDEFINED || that == UNDEFINED)
			return UNDEFINED;
		if (this == INFINITE || that == INFINITE)
			return INFINITE;
		if (this == ZERO || that == ZERO)
			return ZERO;
		if (this == ONE)
			return that;
		if (that == ONE)
			return this;
		return valueOf(this.value.multiply(that.value));
	}

	/**
	 * Note:
	 *  inf/inf == 0/0 == inf
	 */
	public ProcessingNum divide(ProcessingNum that) {
		if (this == UNDEFINED || that == UNDEFINED)
			return UNDEFINED;
		if (this == INFINITE || that == ZERO)
			return INFINITE;
		if (this == ZERO || that == INFINITE)
			return ZERO;
		if (that == ONE)
			return this;
		return valueOf(this.value.divide(that.value));
	}
}
