package org.eu.smileyik.luaInMinecraftBukkitII.reflect;

import org.eu.smileyik.luajava.exception.Result;
import org.eu.smileyik.luajava.reflect.ConvertablePriority;
import org.eu.smileyik.luajava.type.LuaTable;
import org.eu.smileyik.luajava.util.ParamRef;
import org.eu.smileyik.simpledebug.DebugLogger;
import org.jetbrains.annotations.NotNull;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class LuaTable2Object {
    /**
     * 将 LuaTable 转化为 JavaBean 实体. 所提供的 JavaBean 类需要有默认无参构造器.
     * @param t     LuaTable
     * @param clazz 目标类型
     * @return 目标类型实例
     * @param <T> 目标类型
     */
    public static <T> Result<T, Exception> covert(@NotNull final LuaTable t,
                                                  @NotNull final Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            ParamRef<Object> ref = ParamRef.wrapper();
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                Method writeMethod = property.getWriteMethod();
                if (writeMethod == null) {
                    continue;
                }
                String propertyName = property.getName();
                t.get(propertyName)
                        .mapResultValue(luaObj -> {
                            ref.clear();
                            int convertResult = ConvertablePriority.isConvertableType(
                                    Integer.MAX_VALUE, luaObj, property.getPropertyType(), ref);
                            if (convertResult != ConvertablePriority.NOT_MATCH) {
                                luaObj = ref.isEmpty() ? luaObj : ref.getParamAndClear();
                                try {
                                    writeMethod.invoke(obj, luaObj);
                                } catch (Exception e) {
                                    return Result.failure(e);
                                }
                            }
                            return Result.success();
                        })
                        .ifFailureThen(err -> {
                            DebugLogger.debug("failed to convert %s: %s", propertyName, err.getMessage());
                            DebugLogger.debug(err);
                        });
            }
            DebugLogger.debug("converted %s to %s", t, obj);
            return Result.success(obj);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }
}
