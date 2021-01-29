package me.temaflux.onlineuuid;

import java.util.UUID;

public class Player {
	public UUID uuid          = null;
	public Long timestamp_end = 0L;
	
	public Player() {}
	
	public Player(UUID uuid) {
		this.uuid = uuid;
	}
	
	public Player(UUID uuid, Long timestamp_end) {
		this.uuid          = uuid;
		this.timestamp_end = timestamp_end;
	}
}
