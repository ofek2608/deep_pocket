package com.ofek2608.deep_pocket.api.utils;

public record Rect(int x0, int x1, int y0, int y1) {
	public static final Rect ZERO = new Rect(0, 0, 0, 0);
	
	public Rect {
		if (x1 < x0) {
			int temp = x0;
			x0 = x1;
			x1 = temp;
		}
		if (y1 < y0) {
			int temp = y0;
			y0 = y1;
			y1 = temp;
		}
	}
	
	public int x() {
		return x0;
	}
	
	public int y() {
		return y0;
	}
	
	public int w() {
		return x1 - x0;
	}
	
	public int h() {
		return y1 - y0;
	}
	
	public int midX() {
		return (x0 + x1) >> 1;
	}
	
	public int midY() {
		return (y0 + y1) >> 1;
	}
}
