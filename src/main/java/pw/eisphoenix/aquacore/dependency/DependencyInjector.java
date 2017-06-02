package pw.eisphoenix.aquacore.dependency;

import pw.eisphoenix.aquacore.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class DependencyInjector {
    private static Map<Class<?>, Object> dependencies = new HashMap<>();

    public static void inject(final Object object) {
        final Class<?> clazz = object.getClass();
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(Inject.class) == null) {
                continue;
            }
            final Class<?> typeClazz = field.getType();
            if (typeClazz.getAnnotation(Injectable.class) == null) {
                throw new IllegalInjectionException("ClassType is not injectable");
            }
            if (!dependencies.containsKey(typeClazz)) {
                loadClass(typeClazz);
            }
            ReflectionUtil.setFinal(field, false);
            try {
                field.set(object, dependencies.get(typeClazz));
            } catch (final IllegalAccessException e) {
                throw new IllegalInjectionException("Illegal Access");
            }
        }

        for (final Class<?> interfaceClazz : clazz.getInterfaces()) {
            if (InjectionHook.class.equals(interfaceClazz)) {
                ((InjectionHook) object).postInjection();
            }
        }
    }

    private static void loadClass(final Class<?> clazz) {
        try {
            final Object object = clazz.getConstructor().newInstance();
            dependencies.put(clazz, object);
            inject(object);
        } catch (final ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
