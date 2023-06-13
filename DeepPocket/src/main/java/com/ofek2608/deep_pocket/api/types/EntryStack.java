package com.ofek2608.deep_pocket.api.types;

import com.ofek2608.deep_pocket.api.utils.LNUtils;

public record EntryStack(EntryType type, long count) {
	public EntryStack {
		count = LNUtils.normalize(count);
	}
	
	public EntryStack(EntryType type) {
		this(type, 1);
	}
}
