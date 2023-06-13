package com.ofek2608.deep_pocket.api.utils;

public final class LNUtils {
	private LNUtils() {}
	
	public static long normalize(long num) {
		return num < 0 ? -1 : num;
	}
	
	public static int closestInt(long num) {
		return 0 <= num && num <= Integer.MAX_VALUE ? (int)num : Integer.MAX_VALUE;
	}
}
