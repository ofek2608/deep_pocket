package com.ofek2608.deep_pocket.api.struct.client;

import com.ofek2608.deep_pocket.api.struct.PocketBase;
import com.ofek2608.deep_pocket.api.struct.PocketInfo;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public final class ClientPocket extends PocketBase {
	@Nullable private ClientPocketData data = null;
	
	public ClientPocket(UUID pocketId, UUID owner, PocketInfo info) {
		super(pocketId, owner, info);
	}
	
	public boolean isDataEmpty() {
		return data == null;
	}
	
	public boolean isDataPresent() {
		return data != null;
	}
	
	public ClientPocketData getData() throws NoSuchElementException {
		if (data == null)
			throw new NoSuchElementException("No value present");
		return data;
	}
	
	public Optional<ClientPocketData> getDataOptional() {
		return Optional.ofNullable(data);
	}
	
	public ClientPocketData createData() {
		return data = new ClientPocketData();
	}
	
	public ClientPocketData getOrCreateData() {
		if (isDataEmpty()) {
			createData();
		}
		return data;
	}
	
	public void clearData() {
		data = null;
	}
}
