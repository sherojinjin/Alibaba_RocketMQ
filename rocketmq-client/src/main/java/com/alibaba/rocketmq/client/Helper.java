package com.alibaba.rocketmq.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class Helper {

    public static<T extends  Throwable> void checkNotNull(String varName, Object var, Class<T> clazz) throws T {
        if (null == var) {
            throw buildException(clazz, "Variable " + varName + " cannot be null");
        }
    }


    static<T extends Throwable> T buildException(Class<T> clazz, String msg) {
        try {
            Constructor<T> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(msg);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
