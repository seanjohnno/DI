package com.seanjohnno.di;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class GraphTest {

    private static final String HELLO = "hello";

    @Test
    public void testDependenciesFetched_whenDependenciesProvided() {
        Graph graph = addHelloDependency();
        TestInterface test = graph.get(TestInterface.class);
        assertEquals(HELLO, test.speak());
    }

    @Test
    public void testDependenciesFetched_andInitialisedCorrectly_whenDependenciesProvided() {
        String speak = "blah blah blah";

        Graph graph = addCustomMsgDependency(speak);
        TestInterface test = graph.get(TestInterface.class);
        assertEquals(speak, test.speak());
    }

    @Test
    public void testDifferentDependenciesFetched_whenNotSingleton() {
        Graph graph = addHelloDependency();
        TestInterface retrievedOne = graph.get(TestInterface.class);
        TestInterface retrievedTwo = graph.get(TestInterface.class);
        assertNotEquals(retrievedOne, retrievedTwo);
    }

    @Test
    public void testSingletonDependencyFecthed_withSingletonProvider() {
        Graph graph = addSingletonHelloDependency();
        TestInterface test = graph.get(TestInterface.class);
        assertEquals(HELLO, test.speak());
    }

    @Test
    public void testSingletonDependencyFetched_andInitialisedCorrectly_withSingletonProvider() {
        String speak = "blah blah blah";

        Graph graph = addSingletonCustomMsgDependency(speak);
        TestInterface test = graph.get(TestInterface.class);
        assertEquals(speak, test.speak());
    }

    @Test
    public void testSameSingletonDependencyFecthed_withSingletonProvider() {
        Graph graph = addSingletonHelloDependency();
        TestInterface retrievedOne = graph.get(TestInterface.class);
        TestInterface retrievedTwo = graph.get(TestInterface.class);
        assertEquals(retrievedOne, retrievedTwo);
    }

    @Test
    public void testScopedDependenciesFetched_whenDependenciesProvided() {
        Graph graph = addHelloDependency();
        TestInterface test = graph.getScoped(TestInterface.class, "HelloScope");
        assertEquals(HELLO, test.speak());
    }

    @Test
    public void testScopedDependenciesFetched_andInitialisedCorrectly_whenDependenciesProvided() {
        Graph graph = new Graph.Builder()
                .add(TestInterface.class, new Graph.Suppliers.Supplier<TestInterface, String>() {
                    @Override
                    public TestInterface supply(Graph diContainer, String builder) {
                        return new TestInterface.DIImpl(builder);
                    }
                })
                .build();

        TestInterface test = graph.getScoped(TestInterface.class, "boo", "testScope");
        assertEquals("boo", test.speak());
    }

    @Test
    public void testDifferentDependenciesFetched_whenScopesProvided() {
        Graph graph = addHelloDependency();
        TestInterface retrievedOne = graph.getScoped(TestInterface.class, "hello1");
        TestInterface retrievedTwo = graph.getScoped(TestInterface.class, "hello2");
        assertNotEquals(retrievedOne, retrievedTwo);
    }

    @Test(expected = NoSuchElementException.class)
    public void testExceptionThrown_WhenDependencyNotProvided() {
        Graph graph = new Graph.Builder().build();
        graph.get(TestInterface.class);
    }

    @Test(expected = NoSuchElementException.class)
    public void testExceptionThrown_WhenDependencyNotProvidedForScoped() {
        Graph graph = new Graph.Builder().build();
        graph.getScoped(TestInterface.class, "");
    }

    @Test
    public void testScopedDependencyRemoval() {
        String scope = "hello1";

        Graph graph = addHelloDependency();
        TestInterface retrievedOne = graph.getScoped(TestInterface.class, scope);
        graph.removeScoped(TestInterface.class, scope);

        TestInterface retrievedTwo = graph.getScoped(TestInterface.class, scope);
        assertNotEquals(retrievedOne, retrievedTwo);
    }

    @Test
    public void testDependencySupplied_afterScopedRequested() {
        Graph graph = addHelloDependency();
        graph.getScoped(TestInterface.class, "scopedHello");
        TestInterface retrievedTwo = graph.get(TestInterface.class);
        assertEquals(HELLO, retrievedTwo.speak());
    }

    private Graph addSingletonHelloDependency() {
        return new Graph.Builder()
                .addSingleton(TestInterface.class, new Graph.Suppliers.Supplier<TestInterface, Void>() {
                    @Override
                    public TestInterface supply(Graph diContainer, Void builder) {
                        return new TestInterface.HelloImpl();
                    }
                })
                .build();
    }

    private Graph addSingletonCustomMsgDependency(final String msg) {
        return new Graph.Builder()
                .addSingleton(TestInterface.class, new Graph.Suppliers.Supplier<TestInterface, String>() {
                    @Override
                    public TestInterface supply(Graph diContainer, String builder) {
                        return new TestInterface.DIImpl(msg);
                    }
                })
                .build();
    }

    private Graph addHelloDependency() {
        return new Graph.Builder()
                .add(TestInterface.class, new Graph.Suppliers.Supplier<TestInterface, Void>() {
                    @Override
                    public TestInterface supply(Graph diContainer, Void builder) {
                        return new TestInterface.HelloImpl();
                    }
                })
                .build();
    }

    private Graph addCustomMsgDependency(final String msg) {
        return new Graph.Builder()
                .add(TestInterface.class, new Graph.Suppliers.Supplier<TestInterface, String>() {
                    @Override
                    public TestInterface supply(Graph diContainer, String builder) {
                        return new TestInterface.DIImpl(msg);
                    }
                })
                .build();
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
