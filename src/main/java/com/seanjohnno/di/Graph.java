package com.seanjohnno.di;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public interface Graph {

    <T> T get(Class<T> type);
    <T, B> T get(Class<T> type, B builder);
    <T> T getScoped(Class<T> type, Object scopeId);
    <T, B> T getScoped(Class<T> type, B builder, Object scopeId);
    <T> void removeScoped(Class<T> type, Object scopeId);


    class Builder {

        private Map<Class<?>, Suppliers.Supplier> suppliers = new HashMap<>();

        public <T, B> Builder add(Class<T> type, Suppliers.Supplier<T, B> provider) {
            suppliers.put(type, provider);
            return this;
        }

        public <T, B> Builder addSingleton(Class<T> type, Suppliers.Supplier<T, B> provider) {
            suppliers.put(type, new Suppliers.SingletonSupplier<>(provider));
            return this;
        }

        public Graph build() {
            return new GraphImpl(this);
        }

    }

    interface Suppliers {

        interface Supplier<T, B> {
            T supply(Graph diContainer, B builder);
        }

        class SingletonSupplier<T, B> implements Supplier<T, B> {
            private T data;
            private Supplier<T, B> provider;
            SingletonSupplier(Supplier<T, B> provider) {
                this.provider = provider;
            }
            @Override
            public T supply(Graph diContainer, B builder) {
                if(data == null) {
                    data = provider.supply(diContainer, builder);
                }
                return data;
            }
        }

        class ScopedSupplier<T, B> implements Supplier<T, B> {
            private Map<Object, T> dataMap = new HashMap<>();
            private Supplier<T, B> provider;
            ScopedSupplier(Supplier<T, B> provider) {
                this.provider = provider;
            }

            @Override
            public T supply(Graph diContainer, B builder) {
                return provider.supply(diContainer, builder);
            }

            public T provide(Graph diContainer, B builder, Object scopedId) {
                T data = dataMap.get(scopedId);
                if(data == null) {
                    data = provider.supply(diContainer, builder);
                    dataMap.put(scopedId, data);
                }
                return data;
            }

            public void remove(Object scopedId) {
                dataMap.remove(scopedId);
            }
        }
    }

    class GraphImpl implements Graph {

        private Map<Class<?>, Suppliers.Supplier> suppliers = new HashMap<>();

        GraphImpl(Builder builder) {
            suppliers = builder.suppliers;
        }

        @Override
        public <T> T get(Class<T> type) {
            return get(type, null);
        }

        @Override
        public <T, B> T get(Class<T> type, B builder) {
            @SuppressWarnings("unchecked") Suppliers.Supplier<T, B> supplier = suppliers.get(type);
            if(supplier == null) {
                throw new NoSuchElementException();
            }
            return supplier.supply(this, builder);
        }

        @Override
        public <T> T getScoped(Class<T> type, Object scopeId) {
            @SuppressWarnings("unchecked") Suppliers.ScopedSupplier<T, ?> scopedProvider = getScopedProvider(type);
            return scopedProvider.provide(this, null, scopeId);
        }

        @Override
        public <T, B> T getScoped(Class<T> type, B builder, Object scopeId) {
            @SuppressWarnings("unchecked") Suppliers.ScopedSupplier<T, B> scopedProvider = getScopedProvider(type);
            return scopedProvider.provide(this, builder, scopeId);
        }

        @Override
        public <T> void removeScoped(Class<T> type, Object scopeId) {
            @SuppressWarnings("unchecked") Suppliers.ScopedSupplier<T, ?> scopedProvider = getScopedProvider(type);
            scopedProvider.remove(scopeId);
        }

        private <T, B> Suppliers.ScopedSupplier<T, B> getScopedProvider(Class<T> type) {
            @SuppressWarnings("unchecked") Suppliers.Supplier<T, B> provider = suppliers.get(type);
            if(provider == null) {
                throw new NoSuchElementException();
            } else if(!(provider instanceof Suppliers.ScopedSupplier)) {
                provider = new Suppliers.ScopedSupplier<T, B>(provider);
                suppliers.put(type, provider);
            }

            return (Suppliers.ScopedSupplier<T, B>)provider;
        }
    }
}
