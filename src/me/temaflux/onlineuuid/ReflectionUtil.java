package me.temaflux.onlineuuid;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;

public final class ReflectionUtil {
    private static final String SERVER_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(23);
    
	public static boolean useMethod(Object object, String name, Class<?> type, Object value) {
		try {
			Method method = object.getClass().getMethod(name, type);
			method.invoke(object, value);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
    public static Class<?> getNMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + SERVER_VERSION + "." + className);
    }

    public static Class<?> getCraftBukkitClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + "." + className);
    }
}

