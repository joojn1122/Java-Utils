package com.joojn.utils;

import com.google.common.collect.Iterables;
import net.joojn.minecraft.EventListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

public class Env {


    public static Object callStatic(String className, String methodName, Object... params)
    {
        ClassLoader cl = EventListener.classLoader;
        Stream<Object> stream = Arrays.stream(params);

        try
        {
            Class<?> clazz = cl.loadClass(className);
            Method method = clazz.getMethod(methodName,
                    stream.map(Object::getClass).map(Env::getPrimitiveClass).toArray(Class[]::new));

            return method.invoke(null, params);

        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class<?> getPrimitiveClass(Class<?> clazz)
    {
        HashMap<Class<?>, Class<?>> hash = new HashMap<>();

        hash.put(Integer.class, int.class);
        hash.put(Double.class, double.class);
        hash.put(Float.class, float.class);
        hash.put(Character.class, char.class);
        hash.put(Long.class, long.class);
        hash.put(Short.class, short.class);

        return hash.getOrDefault(clazz, clazz);
    }

    public static Object callSimpleStatic(String className, String methodName)
    {
        ClassLoader cl = EventListener.classLoader;

        try
        {
            Class<?> clazz = cl.loadClass(className);
            Method method = clazz.getMethod(methodName,
                    (Class<?>[]) null
            );

            return method.invoke(null, (Object[]) null);

        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static Method getMethodByName(String className, String methodName, boolean declared)
    {
        try
        {
            Class<?> clazz = EventListener.classLoader.loadClass(className);

            for(Method method : declared ? clazz.getDeclaredMethods() : clazz.getMethods())
            {
                if(method.getName().equals(methodName)) return method;
            }
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static Object invoke(Method method, Object instance, Object... args)
    {
        try
        {
            return method.invoke(instance, args);
        }
        catch (InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static Object invokeSimple(Method method, Object instance)
    {
        return invoke(method, instance, (Object[]) null);
    }

    public static Object invokeStatic(Method method, Object... args)
    {
        return invoke(method, null, args);
    }

    public static Object invokeStatic(Method method)
    {
        return invokeSimple(method, null);
    }

    public static <T> T getField(Field field, Object instance)
    {
        try
        {
            return (T) field.get(instance);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String fieldsToString(Object keybind) {

        StringBuilder stringBuilder = new StringBuilder().append("[");

        for(Field field : keybind.getClass().getFields())
        {
            if(Modifier.isStatic(field.getModifiers())) continue;

            if(!field.isAccessible()) field.setAccessible(true);

            Object value = getField(field, keybind);

            stringBuilder.append(field.getName()).append(" = ").append(value).append(",");
        }

        return stringBuilder.append("]").toString();
    }
}
