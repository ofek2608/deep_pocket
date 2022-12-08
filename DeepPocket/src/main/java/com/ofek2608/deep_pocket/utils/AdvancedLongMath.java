package com.ofek2608.deep_pocket.utils;

/**
 * Advanced longs are longs where negative numbers represent infinity.
 */
public final class AdvancedLongMath {
	private AdvancedLongMath() {}
	
	/**
	 * @param a an advanced long
	 * @param b an advanced long
	 * @return a + b or -1 if overflow
	 */
	public static long advancedSum(long a, long b) {
		if (a < 0 || b < 0) return -1;
		long result = a + b;
		return result < 0 ? -1 : result;
	}
	
	/**
	 * @param a an advanced long
	 * @param b an advanced long
	 * @return a - b or 0 if underflow
	 * @apiNote inf - inf := inf
	 */
	public static long advancedSub(long a, long b) {
		if (a < 0) return -1;
		if (b < 0 || a <= b) return 0;
		return a - b;
	}
	
	/**
	 * @param a an advanced long
	 * @param b an advanced long
	 * @return a * b or -1 if overflow
	 * @apiNote 0 * inf := 0
	 */
	public static long advancedMul(long a, long b) {
		if (a == 0 || b == 0) return 0;
		if (a < 0 || b < 0) return -1;
		try {
			return Math.multiplyExact(a, b);
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * @param a an advanced long
	 * @param b an advanced long
	 * @return a / b
	 * @apiNote 0 / inf := 0
	 * @apiNote 0 / 0 := 0
	 * @apiNote inf / inf := inf
	 */
	public static long advancedDiv(long a, long b) {
		if (a == 0) return 0;
		if (a < 0 || b == 0) return -1;
		if (b < 0) return 0;
		return a / b;
	}
	
	/**
	 * @param a an advanced long
	 * @param b an advanced long
	 * @return the minimum between the numbers
	 */
	public static long advancedMin(long a, long b) {
		if (a < 0) return b < 0 ? -1 : 0;
		if (b < 0) return a;
		return Math.min(a, b);
	}
	
	/**
	 * @param a an advanced long
	 * @param b an advanced long
	 * @return the maximum between the numbers
	 */
	public static long advancedMax(long a, long b) {
		if (a < 0 || b < 0) return -1;
		return Math.max(a, b);
	}
	
	private static final String[] ADVANCED_NUMBER_SUFFIXES = {"K","M","B","T","Q","P"};
	/**
	 * @param num      an advanced number
	 * @param limitLen the maximum string length, must be >= 5 if it's >= 19 then the result won't contain a "."
	 * @return a string with length shorter than {@code limitLen}
	 */
	public static String advancedToString(long num, int limitLen) {
		if (limitLen < 5)
			throw new IllegalArgumentException("limitLen");
		if (num < 0)
			return "Inf";
		String asString = Long.toString(num);
		int numLen = asString.length();
		if (numLen <= limitLen)
			return asString;
		int dotIndex = ((numLen - 1) % 3) + 1;
		return asString.substring(0, dotIndex) +
				(limitLen - 2 > dotIndex ? '.' + asString.substring(dotIndex, limitLen - 2) : "") +
				ADVANCED_NUMBER_SUFFIXES[(numLen - 4) / 3];
	}
}
