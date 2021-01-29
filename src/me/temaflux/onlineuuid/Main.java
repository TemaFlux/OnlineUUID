package me.temaflux.onlineuuid;

import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.profile.PlayerProfile;

public class Main
extends JavaPlugin
implements Listener {
	public static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(23);
	private MojangAPI mojangapi = null;
	
	@Override
	public void onEnable() {
		this.mojangapi = new MojangAPI(this);
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Listener) this);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent e) {
		PlayerProfile pp = e.getPlayerProfile();
		UUID online = getUUID(pp.getName());
		if (online != null && !online.equals(pp.getId())) {
			pp.setId(online);
			e.setPlayerProfile(pp);
//			this.getLogger().info("Change from Offline to Online UUID for player: " + pp.getName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLoginEvent(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		UUID online = getUUID(p.getName());
		if (online != null && !p.getUniqueId().equals(online)) {
			setUUID(p, online);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLoginEvent(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		UUID online = getUUID(p.getName());
		if (online != null && !p.getUniqueId().equals(online)) {
			setUUID(p, online);
		}
	}
	
	public UUID getUUID(String name) {
		UUID uuid = null;
		try {
			uuid = mojangapi.getUUID(name, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uuid;
	}
	
	public void setUUID(Player p, UUID uuid) {
		try {
			Object EP = getCraftPlayer(p);
			Method setUUID = EP.getClass().getMethod("setUUID", UUID.class);
			setUUID.invoke(EP, uuid);
//			this.getLogger().info("Change from Offline to Online UUID for player #2: " + p.getName());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static Object getCraftPlayer(Player player) {
	    try {
	        return Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".entity.CraftPlayer")
	                .getMethod("getHandle")
	                .invoke(player);
	    } catch (Exception e) {
	        throw new Error(e);
	    }
	}
}
