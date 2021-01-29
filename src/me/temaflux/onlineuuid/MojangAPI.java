package me.temaflux.onlineuuid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

public class MojangAPI {
	public Plugin plugin = null;
	public Logger logger = null;
	public HashMap<String, Player> temp = new HashMap<String, Player>();
    private static final String UUID_URL = "https://api.minetools.eu/uuid/%name%";
    private static final String UUID_URL_MOJANG = "https://api.mojang.com/users/profiles/minecraft/%name%";
    private static final String UUID_URL_BACKUP = "https://api.ashcon.app/mojang/v2/user/%name%";
    
    public MojangAPI(Plugin plugin) {
    	this.plugin = plugin;
    	this.logger = this.plugin.getLogger();
    }

    public UUID getUUID(String name, boolean tryNext) throws Exception {
    	Player p = temp.getOrDefault(name, new Player());
    	boolean end = (p.timestamp_end-System.currentTimeMillis() <= 0);
    	if (end && p.uuid != null)
    		temp.remove(name);
    	else if (p.uuid != null)
    		return p.uuid;
        String output;

        try {
            output = readURL(UUID_URL.replace("%name%", name));

            JsonElement element = new JsonParser().parse(output);
            JsonObject obj = element.getAsJsonObject();

            if (obj.has("status") && obj.get("status").getAsString().equalsIgnoreCase("ERR")) {
            	UUID uuid = getUUIDMojang(name);
            	temp.put(name, new Player(uuid, System.currentTimeMillis() + (1800 * 1000L)));
                return uuid;
            }

            if (obj.get("id") == null)
                throw new Exception("Premium player with that name does not exist.");

            UUID uuid = this.Str2UUID(obj.get("id").getAsString());
            temp.put(name, new Player(uuid, System.currentTimeMillis() + (1800 * 1000L)));
            return uuid;
        } catch (IOException e) {
            if (tryNext) {
            	UUID uuid = getUUIDMojang(name);
                temp.put(name, new Player(uuid, System.currentTimeMillis() + (1800 * 1000L)));
                return uuid;
            }
        }

        return null;
    }

    public UUID getUUIDMojang(String name) throws Exception {
        return getUUIDMojang(name, true);
    }

    public UUID getUUIDMojang(String name, boolean tryNext) throws Exception {
        logger.log(Level.INFO, "Trying Mojang API to get UUID for player " + name + ".");

        String output;
        try {
            output = readURL(UUID_URL_MOJANG.replace("%name%", name));

            if (output.isEmpty())
            	throw new Exception("Premium player with that name does not exist.");

            JsonElement element = new JsonParser().parse(output);
            JsonObject obj = element.getAsJsonObject();

            if (obj.has("error")) {
                if (tryNext)
                    return getUUIDBackup(name);
                return null;
            }

            return this.Str2UUID(obj.get("id").getAsString());

        } catch (IOException e) {
            if (tryNext)
                return getUUIDBackup(name);
        }

        return null;
    }

    public UUID getUUIDBackup(String name) throws Exception {
        logger.log(Level.INFO, "Trying backup API to get UUID for player " + name + ".");

        try {
            String output = readURL(UUID_URL_BACKUP.replace("%name%", name), 10000);

            JsonElement element = new JsonParser().parse(output);
            JsonObject obj = element.getAsJsonObject();

            if (obj.has("code")) {
                if (obj.get("error").getAsString().equalsIgnoreCase("Not Found"))
                	throw new Exception("Premium player with that name does not exist.");

                throw new Exception("Skin Data API is overloaded, please try again later!");
            }

            return this.Str2UUID(obj.get("uuid").getAsString().replace("-", ""));
        } catch (IOException e) {
        	throw new Exception("Premium player with that name does not exist.");
        }
    }

    private String readURL(String url) throws IOException {
        return readURL(url, 5000);
    }

    private String readURL(String url, int timeout) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "OnlineUUID");
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        con.setDoOutput(true);

        String line;
        StringBuilder output = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        while ((line = in.readLine()) != null)
            output.append(line);

        in.close();
        return output.toString();
    }
    
    public UUID Str2UUID(String s) {
    	s = s.replace("-", "");
    	UUID uuid = null;
    	try {
	    	uuid = new UUID(
		    	new BigInteger(s.substring(0, 16), 16).longValue(),
		    	new BigInteger(s.substring(16), 16).longValue()
	    	);
    	}
    	catch (Exception e) {}
    	return uuid;
    }
}