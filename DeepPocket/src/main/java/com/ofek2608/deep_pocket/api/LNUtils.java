package com.ofek2608.deep_pocket.api;

public final class LNUtils {
	private LNUtils() {}
	
	public static long normalize(long num) {
		return num < 0 ? -1 : num;
	}
}
