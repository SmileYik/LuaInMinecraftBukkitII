package org.eu.smileyik.luaInMinecraftBukkitII.reflect;

import org.eu.smileyik.luaInMinecraftBukkitII.reflect.fastReflection.Lookup;
import org.eu.smileyik.luajava.LuaJavaAPI;
import org.eu.smileyik.luajava.reflect.IExecutable;
import org.eu.smileyik.luajava.reflect.IFieldAccessor;
import org.eu.smileyik.luajava.reflect.LuaInvokedMethod;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectUtil {
    public static final String PRIMITIVE_TYPE_PATTERN_STR = "(byte|short|int|long|float|double|char|boolean)";
    // public static final Pattern PRIMITIVE_TYPE_PATTERN = Pattern.compile(String.format("^%s$", PRIMITIVE_TYPE_PATTERN_STR));
    public static final Pattern PRIMITIVE_TYPE_PATTERN = Pattern.compile(String.format("^%s(\\[\\])*$", PRIMITIVE_TYPE_PATTERN_STR));

    public static final String FULL_CLASS_NAME_PATTERN_STR = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*";
    public static final Pattern FULL_CLASS_NAME_PATTERN = Pattern.compile(String.format("^%s$", FULL_CLASS_NAME_PATTERN_STR));
    public static final Pattern FULL_CLASS_ARRAY_NAME_PATTERN_1 = Pattern.compile(String.format("^\\[+L%s;$", FULL_CLASS_NAME_PATTERN_STR));
    public static final Pattern FULL_CLASS_ARRAY_NAME_PATTERN_2 = Pattern.compile(String.format("^%s(\\[\\])+$", FULL_CLASS_NAME_PATTERN_STR));

    private static final Map<String, Class<?>> PRIMITIVE_NAME_2_TYPE_MAP;
    private static final MethodHandle getReflectUtil;

    static {
        Map<String, Class<?>> primitiveName2TypeMap = new HashMap<String, Class<?>>();
        primitiveName2TypeMap.put("byte", byte.class);
        primitiveName2TypeMap.put("short", short.class);
        primitiveName2TypeMap.put("int", int.class);
        primitiveName2TypeMap.put("long", long.class);
        primitiveName2TypeMap.put("float", float.class);
        primitiveName2TypeMap.put("double", double.class);
        primitiveName2TypeMap.put("char", char.class);
        primitiveName2TypeMap.put("boolean", boolean.class);
        primitiveName2TypeMap.put("void", void.class);
        primitiveName2TypeMap.put("B", byte.class);
        primitiveName2TypeMap.put("S", short.class);
        primitiveName2TypeMap.put("I", int.class);
        primitiveName2TypeMap.put("J", long.class);
        primitiveName2TypeMap.put("F", float.class);
        primitiveName2TypeMap.put("D", double.class);
        primitiveName2TypeMap.put("C", char.class);
        primitiveName2TypeMap.put("Z", boolean.class);
        primitiveName2TypeMap.put("V", void.class);
        PRIMITIVE_NAME_2_TYPE_MAP = Collections.unmodifiableMap(primitiveName2TypeMap);

        MethodHandle handle = null;
        try {
            handle = Lookup.getFieldGetter(LuaJavaAPI.class.getDeclaredField("reflectUtil"));
        } catch (Exception e) {
            DebugLogger.debug("Failed to initialize reflect util: %s", e.getMessage());
            DebugLogger.debug(e);
        } finally {
            getReflectUtil = handle;
        }
    }

    private static org.eu.smileyik.luajava.reflect.ReflectUtil getReflectUtil() {
        try {
            return (org.eu.smileyik.luajava.reflect.ReflectUtil) getReflectUtil.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Field findFieldByType(Class<?> clazz, Class<?> targetType) {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (targetType.isAssignableFrom(field.getType())) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Object newInstance(Class<?> clazz, Object... _args)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (clazz == null) {
            throw new NullPointerException("class is null");
        }
        if (_args == null) {
            _args = new Object[0];
        }
        Object[] args = _args;
        LuaInvokedMethod<IExecutable<Constructor<?>>> constructor = getReflectUtil()
                .findConstructorByParams(clazz, args, false, false, false);
        if (constructor == null) {
            throw new NullPointerException("No constructor found in " + clazz + " by params: " + Arrays.toString(args));
        }

        constructor.getOverwriteParams().forEach((idx, value) -> args[idx] = value);
        return constructor.getExecutable().invoke(null, args);
    }

    public static Object callMethod(Object object, String methodName, Object... _args)
            throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        if (_args == null) {
            _args = new Object[0];
        }
        Object[] args = _args;
        LinkedList<LuaInvokedMethod<IExecutable<Method>>> methodByParams = getReflectUtil()
                .findMethodByParams(object.getClass(), methodName, args, false, false, false, false);
        if (methodByParams.isEmpty()) {
            throw new NullPointerException("No method found for " + methodName + " in " + object.getClass() + " by params: " + Arrays.toString(args));
        }
        if (methodByParams.size() != 1) {
            throw new NullPointerException("Multi-target method found for " + methodName + " in " + object.getClass() + " by params: " + Arrays.toString(args));
        }
        LuaInvokedMethod<IExecutable<Method>> invokedMethod = methodByParams.removeFirst();
        invokedMethod.getOverwriteParams().forEach((idx, value) -> args[idx] = value);
        boolean isStaticMethod = Modifier.isStatic(invokedMethod.getExecutable().getExecutable().getModifiers());
        return invokedMethod.getExecutable().invoke(isStaticMethod ? null : object, args);
    }

    public static boolean hasMethod(Object object, String methodName) {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        if (methodName == null || methodName.isEmpty()) {
            throw new NullPointerException("methodName is null or empty");
        }
        return getReflectUtil().existsMethodByName(
                object.getClass(), methodName, false, false, false);
    }

    public static boolean hasField(Object object, String fieldName) {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        if (fieldName == null || fieldName.isEmpty()) {
            throw new NullPointerException("fieldName is null or empty");
        }
        return getReflectUtil().findFieldByName(
                object.getClass(), fieldName, false, false, false, false) != null;
    }

    public static void setField(Object object, String fieldName, Object value) throws IllegalAccessException {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        if (fieldName == null || fieldName.isEmpty()) {
            throw new NullPointerException("fieldName is null or empty");
        }
        IFieldAccessor field = getReflectUtil().findFieldByName(
                object.getClass(), fieldName, false, false, false, false
        );
        if  (field == null) {
            throw new NullPointerException("No field found for " + fieldName + " in " + object.getClass());
        }
        field.set(object, value);
    }

    public static Object getField(Object object, String fieldName) throws IllegalAccessException {
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        if (fieldName == null || fieldName.isEmpty()) {
            throw new NullPointerException("fieldName is null or empty");
        }
        IFieldAccessor field = getReflectUtil().findFieldByName(
                object.getClass(), fieldName, false, false, false, false
        );
        if  (field == null) {
            throw new NullPointerException("No field found for " + fieldName + " in " + object.getClass());
        }
        return field.get(object);
    }

    /**
     * 快速构建重复字符串
     * @param str   字符串
     * @param times 重复次数
     */
    private static String fastRepeat(String str, int times) {
        if (str == null || str.isEmpty()) return str;
        if (times <= 0) return "";

        StringBuilder result = new StringBuilder();
        StringBuilder s = new StringBuilder(str);
        while (times > 0) {
            if ((times & 1) == 1) {
                result.append(s);
            }
            s.append(s);
            times >>= 1;
        }
        return result.toString();
    }

    /**
     * 能够以 int/int[]/java.lang.String[] 等形式读取Java类型.
     * @param className 类名
     * @return 指定的类实例
     * @throws ClassNotFoundException 如果未找到相应的类
     */
    public static Class<?> forName(String className) throws ClassNotFoundException {
        // primitive type; int/int[] style
        {
            if (PRIMITIVE_NAME_2_TYPE_MAP.containsKey(className)) {
                return PRIMITIVE_NAME_2_TYPE_MAP.get(className);
            }

            Matcher matcher = PRIMITIVE_TYPE_PATTERN.matcher(className);
            if (matcher.matches()) {
                String primitiveType = matcher.group(1);
                int d = (className.length() - primitiveType.length()) >> 1;
                String realClassName = null;
                Class<?> realClass = null;
                switch (primitiveType) {
                    case "byte":
                        realClass = byte.class;
                        realClassName = "B";
                        break;
                    case "short":
                        realClass = short.class;
                        realClassName = "S";
                        break;
                    case "int":
                        realClass = int.class;
                        realClassName = "I";
                        break;
                    case "long":
                        realClass = long.class;
                        realClassName = "J";
                        break;
                    case "float":
                        realClass = float.class;
                        realClassName = "F";
                        break;
                    case "double":
                        realClass = double.class;
                        realClassName = "D";
                        break;
                    case "char":
                        realClass = char.class;
                        realClassName = "C";
                        break;
                    case "boolean":
                        realClass = boolean.class;
                        realClassName = "Z";
                        break;
                }
                return d > 0 ? Class.forName(fastRepeat("[", d) + realClassName) : realClass;
            }
        }

        // normal
        if (
                FULL_CLASS_NAME_PATTERN.matcher(className).matches() ||
                FULL_CLASS_ARRAY_NAME_PATTERN_1.matcher(className).matches()
        ) {
            return Class.forName(className);
        }

        // java.lang.String[] Style.
        if (!FULL_CLASS_ARRAY_NAME_PATTERN_2.matcher(className).matches()) {
            throw new ClassNotFoundException(className);
        }
        int idx = className.indexOf('[');
        int d = (className.length() - idx) >> 1;
        return Class.forName(fastRepeat("[", d) + "L" + className.substring(0, idx) + ";");
    }

    public static boolean isLambdaInstance(Object instance) {
        return instance != null && instance.getClass().getSimpleName().contains("$$Lambda/");
    }

    public static Method getLambdaRealMethod(Method targetMethod) {
        Class<?> declaringClass = targetMethod.getDeclaringClass();
        if (declaringClass.getSimpleName().contains("$$Lambda/")) {
            Method found = org.eu.smileyik.luajava.reflect.ReflectUtil.foreachClass(declaringClass, true, it -> {
                if (declaringClass == it) return null;
                Method[] methods = it.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(targetMethod.getName())
                            && Arrays.equals(method.getParameterTypes(), targetMethod.getParameterTypes())) {
                        return method;
                    }
                }
                return null;
            });
            return found;
        }
        return null;
    }
}
