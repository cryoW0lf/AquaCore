package pw.eisphoenix.aquacore.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class ReflectionUtil {
    private static Field modifiersField;

    static {
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void setFinal(final Field field, final boolean setFinal) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            modifiersField.setInt(field, field.getModifiers() & (setFinal ? Modifier.FINAL : ~Modifier.FINAL));
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public static String getVersion() {
        final String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static Class<?> getNMSClass(final String className) {
        final String fullName = "net.minecraft.server." + getVersion() + "." + className;
        try {
            return Class.forName(fullName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> getCBClass(final String className) {
        final String fullName = "org.bukkit.craftbukkit." + getVersion() + "." + className;
        try {
            return Class.forName(fullName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
