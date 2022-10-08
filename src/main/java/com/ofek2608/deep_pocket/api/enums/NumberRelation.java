package com.ofek2608.deep_pocket.api.enums;

public enum NumberRelation {
	//NEVER
	BT(0, 0, 1),
	EQ(0, 1, 0),
	BE(0, 1, 1),
	LT(1, 0, 0),
	NE(1, 0, 1),
	LE(1, 1, 0),
	//ALWAYS
	;

	public final boolean l, e, b;

	NumberRelation(int l, int e, int b) {
		this.l = l != 0;
		this.e = e != 0;
		this.b = b != 0;
	}

	public boolean check(double d0, double d1) {
		return d0 < d1 ? l : d0 == d1 ? e : b;
	}
}
