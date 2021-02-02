package me.temaflux.onlineuuid;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.authlib.GameProfile;

public class Main
extends JavaPlugin
implements Listener {
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
		try {
			com.destroystokyo.paper.profile.PlayerProfile pp = e.getPlayerProfile();
			UUID online = getUUID(pp.getName());
			if (online != null && !online.equals(pp.getId())) {
				pp.setId(online);
				e.setPlayerProfile(pp);
//				this.getLogger().info("Change from Offline to Online UUID for player: " + pp.getName());
			}
		}
		catch (Exception | Error ex) {}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent e) {
		setUUID(e);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		setUUID(e);
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
	
	public void setUUID(Event e) {
		Player p = null;
		if (e instanceof PlayerLoginEvent) p = ((PlayerLoginEvent) e).getPlayer();
		else if (e instanceof PlayerQuitEvent) p = ((PlayerQuitEvent) e).getPlayer();
		if (p == null) return;
		String name = p.getName();
		UUID online = getUUID(name);
		if (online != null) {
			//
			Object ep = getCraftPlayer(p);
			GameProfile gp = null;
			try {
				gp = (GameProfile) ep.getClass().getMethod("getProfile").invoke(ep);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			//
			if (gp != null && !gp.getId().equals(online)) {
				// Create new profile
				GameProfile new_gp = new GameProfile(online, name);
				new_gp.getProperties().putAll(gp.getProperties());
				// Find gameprofile
				String gp_f = null;
				for (Field f : Arrays.asList(ep.getClass().getSuperclass().getDeclaredFields()))
					if (f.getType() == GameProfile.class)
						gp_f = f.getName();
				// Change gameprofile
				try {
		            Field field = ep.getClass().getSuperclass().getDeclaredField(gp_f);
		            field.setAccessible(true);
		            field.set(ep, new_gp);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (!p.getUniqueId().equals(online))
				setUUID(p, online);
		}
	}
	
	public void setUUID(Player p, UUID uuid) {
		ReflectionUtil.useMethod(getCraftPlayer(p), "setUUID", UUID.class, uuid);
//		this.getLogger().info("Change from Offline to Online UUID for player #2: " + p.getName());
	}
	
	public static Object getCraftPlayer(Player player) {
	    try {
	        return ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer")
	                .getMethod("getHandle")
	                .invoke(player);
	    } catch (Exception e) {
	        throw new Error(e);
	    }
	}
}
