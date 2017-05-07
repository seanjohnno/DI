package com.seanjohnno.di;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DI {

    private static Map<Class<?>, Providers.Provider> providers = new HashMap<>();
    private static DI instance = new DI();

    public interface Providers {

        interface Provider<T, B> {
            T provide(DI diContainer, B builder);
        }

        class SingletonProvider<T, B> implements Provider<T, B> {
            private T data;
            private Provider<T, B> provider;
            SingletonProvider(Provider<T, B> provider) {
                this.provider = provider;
            }
            @Override
            public T provide(DI diContainer, B builder) {
                if(data == null) {
                    data = provider.provide(diContainer, builder);
                }
                return data;
            }
        }

        class ScopedProvider<T, B> implements Provider<T, B> {
            private Map<Object, T> dataMap = new HashMap<>();
            private Provider<T, B> provider;
            ScopedProvider(Provider<T, B> provider) {
                this.provider = provider;
            }
            @Override
            public T provide(DI diContainer, B builder) {
                throw new UnsupportedOperationException();
            }

            public T provide(DI diContainer, B builder, Object scopedId) {
                T data = dataMap.get(scopedId);
                if(data == null) {
                    data = provider.provide(diContainer, builder);
                    dataMap.put(scopedId, data);
                }
                return data;
            }

            public void remove(Object scopedId) {
                dataMap.remove(scopedId);
            }
        }
    }

    public static <T, B> DI add(Class<T> type, Providers.Provider<T, B> provider) {
        providers.put(type, provider);
        return instance;
    }

    public static <T, B> DI addSingleton(Class<T> type, Providers.Provider<T, B> provider) {
        providers.put(type, new Providers.SingletonProvider<T, B>(provider));
        return instance;
    }

    public static <T, B> T get(Class<T> type) {
        return get(type, null);
    }

    public static <T, B> T get(Class<T> type, B builder) {
        @SuppressWarnings("unchecked") Providers.Provider<T, B> provider = providers.get(type);
        if(provider == null) {
            throw new NoSuchElementException();
        }
        return provider.provide(instance, builder);
    }

    public static <T, B> T getScoped(Class<T> type, Object scopeId) {
        @SuppressWarnings("unchecked") Providers.ScopedProvider<T, B> scopedProvider = getScopedProvider(type);
        return scopedProvider.provide(instance, null, scopeId);
    }

    public static <T, B> T getScoped(Class<T> type, B builder, Object scopeId) {
        @SuppressWarnings("unchecked") Providers.ScopedProvider<T, B> scopedProvider = getScopedProvider(type);
        return scopedProvider.provide(instance, builder, scopeId);
    }

    public static <T> void removeScoped(Class<T> type, Object scopeId) {
        Providers.ScopedProvider scopedProvider = getScopedProvider(type);
        scopedProvider.remove(scopeId);
    }

    public static DI clear() {
        providers.clear();
        return instance;
    }

    private static <T, B> Providers.ScopedProvider<T, B> getScopedProvider(Class<T> type) {
        @SuppressWarnings("unchecked") Providers.Provider<T, B> provider = providers.get(type);
        if(provider == null) {
            throw new NoSuchElementException();
        } else if(!(provider instanceof Providers.ScopedProvider)) {
            provider = new Providers.ScopedProvider<T, B>(provider);
            add(type, provider);
        }

        return (Providers.ScopedProvider<T, B>)provider;
    }
}
