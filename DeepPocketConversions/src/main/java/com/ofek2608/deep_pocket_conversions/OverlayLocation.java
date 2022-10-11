package com.ofek2608.deep_pocket_conversions;

public enum OverlayLocation {
	TOP_LEFT(0, 0),TOP_CENTER(1, 0),TOP_RIGHT(2, 0),
	MIDDLE_LEFT(0, 1), MIDDLE_RIGHT(2, 1),
	BOTTOM_LEFT(0, 2), BOTTOM_RIGHT(2, 2),
	HIDDEN(-1,0);

	public final int x;
	public final int y;

	OverlayLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
