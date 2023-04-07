package io.github.navpil.q.oldschool;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.concurrent.ConcurrentHashMap;

public class UberContextHorribleHack {

    private static ConcurrentHashMap<String, ServiceLocator> locatorMap = new ConcurrentHashMap<>();

    public static void putServiceLocator(String appname, ServiceLocator locator) {
        locatorMap.put(appname, locator);
    }

    public static <T> T getClassForApp(String appname, Class<T> clazz) {
        return locatorMap.get(appname).getService(clazz);
    }
}
