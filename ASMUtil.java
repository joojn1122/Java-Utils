package com.joojn.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ASMUtil {

    public static void printInstructions(InsnList list, boolean fullInfo)
    {
        for(AbstractInsnNode node : list.toArray())
        {
            if(node instanceof LineNumberNode || node instanceof LabelNode) continue;

            System.out.println(fullInfo ? toString(node) : (node + " " + node.getOpcode()));
        }
    }

    public static void injectMethodAtEnd(Class<?> targetClass, String targetMethod, MethodNode method)
    {
        InsnList list = new InsnList();

        // remove RETURN
        for(int i = 0; i < method.instructions.size() - 1 ; i++)
        {
            list.add(method.instructions.get(i));
        }

        injectMethodInstructions(list, targetClass, targetMethod);

        list.add(new InsnNode(Opcodes.RETURN));

        method.instructions = list;
    }

    public static void addPrintln(InsnList list, String value)
    {
        list.add(new FieldInsnNode(jdk.internal.org.objectweb.asm.Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        list.add(new LdcInsnNode(value));
        list.add(new MethodInsnNode(jdk.internal.org.objectweb.asm.Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
    }

    public static void printLdcNodes(MethodNode method)
    {
        System.out.println("Method (" + method.name + ") Ldc nodes: ");

        Arrays.stream(method.instructions.toArray())
                .filter(LdcInsnNode.class::isInstance)
                .map(LdcInsnNode.class::cast)
                .forEach(node -> System.out.println(node.cst));
    }
    /**
     @param offset Offset of where the injection should start -> 0 == start -> -1 == end
     @param argSize Size method arguments, example <p>public void onAttackEntity(Entity entity) -> argSize == 1</p>
     **/
    public static void injectMethodWithArgs(InsnList list, Class<?> targetClass, String targetMethod, int offset, int argSize, boolean override)
    {
        InsnList insList = new InsnList();

        for(int i = 0; i < list.size() ; i++)
        {
            if(i == offset) {
                injectMethodArgs(insList, targetClass, targetMethod, argSize);
                if(override) break;
            }

            insList.add(list.get(i));
        }

        if(offset == -1)
        {
            // remove last (RETURN)
            insList.remove(insList.get(insList.size() - 1));

            injectMethodArgs(insList, targetClass, targetMethod, argSize);
            insList.add(new InsnNode(Opcodes.RETURN));
        }

        list.clear();
        insList.forEach(list::add);
    }

    private static void injectMethodArgs(InsnList list, Class<?> targetClass, String targetMethod, int argSize)
    {
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "java/lang/Object", "getClass", "()Ljava/lang/Class;"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;"));

        list.add(new LdcInsnNode(targetClass.getName()));

        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "java/lang/ClassLoader",
                "loadClass",
                "(Ljava/lang/String;)Ljava/lang/Class;"));


        list.add(new InsnNode(Opcodes.ICONST_1));

        list.add(new VarInsnNode(Opcodes.ASTORE, 1));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));

        list.add(new LdcInsnNode(targetMethod));
        list.add(new InsnNode(Opcodes.ICONST_1));

        list.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));
        list.add(new InsnNode(Opcodes.DUP));

        // add args
        for(int i = 0; i < argSize ; i++)
        {
            list.add(new InsnNode(Opcodes.ICONST_0 + i));
            list.add(new LdcInsnNode(Type.getType(Object.class)));
            list.add(new InsnNode(Opcodes.AASTORE));

            if(i != argSize - 1)
            {
                list.add(new InsnNode(Opcodes.DUP));
            }
        }

        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"));
        list.add(new InsnNode(Opcodes.ACONST_NULL));
        list.add(new InsnNode(Opcodes.ICONST_0 + argSize));

        list.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
        list.add(new InsnNode(Opcodes.DUP));

        // invoke args
        for(int i = 0; i < argSize ; i++)
        {
            list.add(new InsnNode(Opcodes.ICONST_0 + i));
            list.add(new VarInsnNode(Opcodes.ALOAD, i + 1));
            list.add(new InsnNode(Opcodes.AASTORE));

            if(i != argSize - 1)
            {
                list.add(new InsnNode(Opcodes.DUP));
            }
        }

        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));
        list.add(new InsnNode(Opcodes.POP));
    }

    public static void injectMethodInstructions(InsnList list, Class<?> targetClass, String targetMethod)
    {
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "java/lang/Object", "getClass", "()Ljava/lang/Class;"));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;"));

        list.add(new LdcInsnNode(targetClass.getName()));

        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "java/lang/ClassLoader",
                "loadClass",
                "(Ljava/lang/String;)Ljava/lang/Class;"));

        list.add(new VarInsnNode(Opcodes.ASTORE, 1));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));

        list.add(new LdcInsnNode(targetMethod));
        list.add(new InsnNode(Opcodes.ICONST_1));

        list.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));

        list.add(new InsnNode(Opcodes.DUP));
        list.add(new InsnNode(Opcodes.ICONST_0));

        list.add(new LdcInsnNode(Type.getType(Object.class)));
        list.add(new InsnNode(Opcodes.AASTORE));

        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"));
        list.add(new InsnNode(Opcodes.ACONST_NULL));
        list.add(new InsnNode(Opcodes.ICONST_1));

        list.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));

        list.add(new InsnNode(Opcodes.DUP));
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new InsnNode(Opcodes.AASTORE));

        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));
        list.add(new InsnNode(Opcodes.POP));
    }

    public static void injectMethod(Class<?> targetClass, String targetMethod, MethodNode method, boolean override)
    {
        InsnList list = new InsnList();

        injectMethodInstructions(list, targetClass, targetMethod);

        if(override)
        {
            list.add(new InsnNode(Opcodes.RETURN));
        }
        else
        {
            method.instructions.forEach(list::add);
        }

        method.instructions = list;
    }

    public static String arrayToString(Object[] objects, boolean staticFields)
    {
        StringBuilder builder = new StringBuilder().append("[");

        for(int i = 0; i < objects.length; i++)
        {
            Object o = objects[i];

            builder.append(toString(o, staticFields));

            if(i != objects.length - 1) builder.append(", ");
        }

        return builder.append("]").toString();
    }

    public static String toString(Object o)
    {
        return toString(o, false);
    }

    public static String toString(Object o, boolean staticFields)
    {
        // if object is null
        if(o == null) return "null";

        // if object is primitive
        if(Arrays.asList(
                boolean.class,
                int.class,
                char.class,
                String.class,
                Boolean.class,
                Integer.class,
                double.class,
                Double.class,
                float.class,
                Float.class,
                Short.class,
                short.class,
                Long.class,
                long.class,
                Character.class
        ).contains(o.getClass()))
        {
            return String.valueOf(o);
        }

        // if object is array
        if(o instanceof Object[])
        {
            return arrayToString((Object[]) o, staticFields);
        }

        // if object has already toString, and it's not declared in Object's class
        try
        {
            Method method = o.getClass().getMethod("toString");
            if(!method.getDeclaringClass().equals(Object.class)) return o.toString();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(o.getClass().getSimpleName()).append("@").append(o.hashCode()).append("{ ");

        Field[] fields = o.getClass().getFields();
        for(int i = 0; i < fields.length; i++)
        {
            try
            {
                Field f = fields[i];
                if(!f.isAccessible()) f.setAccessible(true);

                // skip static fields
                if(!staticFields && Modifier.isStatic(f.getModifiers())) continue;

                String name = f.getName();
                Object value = f.get(o);

                builder.append(name).append(": ").append(toString(value)).append(i != fields.length - 1 ? ", " : "");
            }
            catch (IllegalAccessException ignored) {}
        }

        return builder.append(" }").toString();
    }
}
