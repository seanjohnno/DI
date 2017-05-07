package com.seanjohnno.di;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class DITest {

    private static final String HELLO = "hello";

    @Test
    public void testDependenciesFetched_whenDependenciesProvided() {
        addHelloDependency();
        TestInterface test = DI.get(TestInterface.class);
        assertEquals(HELLO, test.speak());
    }

    @Test
    public void testDependenciesFetched_andInitialisedCorrectly_whenDependenciesProvided() {
        String speak = "blah blah blah";

        addCustomMsgDependency(speak);
        TestInterface test = DI.get(TestInterface.class);
        assertEquals(speak, test.speak());
    }

    @Test
    public void testDifferentDependenciesFetched_whenNotSingleton() {
        addHelloDependency();
        TestInterface retrievedOne = DI.get(TestInterface.class);
        TestInterface retrievedTwo = DI.get(TestInterface.class);
        assertNotEquals(retrievedOne, retrievedTwo);
    }

    @Test
    public void testSingletonDependencyFecthed_withSingletonProvider() {
        addSingletonHelloDependency();
        TestInterface test = DI.get(TestInterface.class);
        assertEquals(HELLO, test.speak());
    }

    @Test
    public void testSingletonDependencyFetched_andInitialisedCorrectly_withSingletonProvider() {
        String speak = "blah blah blah";

        addSingletonCustomMsgDependency(speak);
        TestInterface test = DI.get(TestInterface.class);
        assertEquals(speak, test.speak());
    }

    @Test
    public void testSameSingletonDependencyFecthed_withSingletonProvider() {
        addSingletonHelloDependency();
        TestInterface retrievedOne = DI.get(TestInterface.class);
        TestInterface retrievedTwo = DI.get(TestInterface.class);
        assertEquals(retrievedOne, retrievedTwo);
    }

    @Test
    public void testScopedDependenciesFetched_whenDependenciesProvided() {
        addHelloDependency();
        TestInterface test = DI.getScoped(TestInterface.class, "HelloScope");
        assertEquals(HELLO, test.speak());
    }

    @Test
    public void testScopedDependenciesFetched_andInitialisedCorrectly_whenDependenciesProvided() {
        DI.add(TestInterface.class, new DI.Providers.Provider<TestInterface, String>() {
            @Override
            public TestInterface provide(DI diContainer, String builder) {
                return new TestInterface.DIImpl(builder);
            }
        });

        TestInterface test = DI.getScoped(TestInterface.class, "boo", "testScope");
        assertEquals("boo", test.speak());
    }

    @Test
    public void testDifferentDependenciesFetched_whenScopesProvided() {
        addHelloDependency();
        TestInterface retrievedOne = DI.getScoped(TestInterface.class, "hello1");
        TestInterface retrievedTwo = DI.getScoped(TestInterface.class, "hello2");
        assertNotEquals(retrievedOne, retrievedTwo);
    }

    @Test(expected = NoSuchElementException.class)
    public void testExceptionThrown_WhenDependencyNotProvided() {
        DI.clear();
        DI.get(TestInterface.class);
    }

    @Test
    public void testExceptionThrown_WhenDependencyNotProvidedForScoped() {
        DI.getScoped(TestInterface.class, "");
    }

    @Test
    public void testScopedDependencyRemoval() {
        String scope = "hello1";

        addHelloDependency();
        TestInterface retrievedOne = DI.getScoped(TestInterface.class, scope);
        DI.removeScoped(TestInterface.class, scope);

        TestInterface retrievedTwo = DI.getScoped(TestInterface.class, scope);
        assertNotEquals(retrievedOne, retrievedTwo);
    }

    private void addSingletonHelloDependency() {
        DI.addSingleton(TestInterface.class, new DI.Providers.Provider<TestInterface, Void>() {
            @Override
            public TestInterface provide(DI diContainer, Void builder) {
                return new TestInterface.HelloImpl();
            }
        });
    }

    private void addSingletonCustomMsgDependency(final String msg) {
        DI.addSingleton(TestInterface.class, new DI.Providers.Provider<TestInterface, String>() {
            @Override
            public TestInterface provide(DI diContainer, String builder) {
                return new TestInterface.DIImpl(msg);
            }
        });
    }

    private void addHelloDependency() {
        DI.add(TestInterface.class, new DI.Providers.Provider<TestInterface, Void>() {
            @Override
            public TestInterface provide(DI diContainer, Void builder) {
                return new TestInterface.HelloImpl();
            }
        });
    }

    private void addCustomMsgDependency(final String msg) {
        DI.add(TestInterface.class, new DI.Providers.Provider<TestInterface, String>() {
            @Override
            public TestInterface provide(DI diContainer, String builder) {
                return new TestInterface.DIImpl(msg);
            }
        });
    }

    public interface TestInterface {
        String speak();

        class HelloImpl implements TestInterface {

            @Override
            public String speak() {
                return HELLO;
            }
        }

        class DIImpl implements TestInterface {
            private String msg;

            DIImpl(String msg) {
                this.msg = msg;
            }

            @Override
            public String speak() {
                return msg;
            }
        }
    }
}
