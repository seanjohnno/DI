## 1. DI

Lightweight dependency injection container that I've been using in my Android projects. 

There's only a single .java file (Graph.java) and a matching test file so just copy and paste to use. Hope it's of some use :)

## 2. Usage

### 2.1. Standard

```
// Given an example interface and implementation

public interface Person {
  String sayHello();
}

public class EnglishMan implements Person {
  public String sayHello() { return "Hello!"; }
}

// Add a provider to the container
Graph graph = new Graph.Builder()
            .add(Person.class, new Graph.Suppliers.Supplier<Person, Void>() {
                @Override
                public TestInterface supply(Graph graph, Void empty) {
                    return new EnglishMan();
                }
            })
            .build();

// ...and getting an instance:
Person person = graph.get(Person.class);
String theySaid = person.sayHello();
System.out.println(theySaid); // Prints "Hello!"
```

### 2.2. Passing parameters for construction

We may need to pass parameters to our providers to fulfill a dependency. Here's how to do this:

```
// Given a new implementation of our Person interface
public class Man implements Person {
  private String helloMsg;
  public Man(String helloMsg) { this.helloMsg = helloMsg; }
  public String sayHello() { return helloMsg; }
}

// Add a provider to the container (notice we now use the second parameter):
Graph graph = new Graph.Builder()
            .add(Person.class, new Graph.Suppliers.Supplier<Person, String>() {
                @Override
                public TestInterface supply(Graph graph, String helloMsg) {
                    return new Man(helloMsg);
                }
            })
            .build();

// When we get we can pass in our parameters
Person spanishMan = graph.get(Person.class, "Hola!");
String theySaid = spanishPerson.sayHello();
System.out.println(theySaid); // Prints "Hola!"
```
Since our supply method is given the graph as an argument we can also use this to provide nested dependencies.

### 2.3. Scoped

Sometimes, it's useful to have a dependency we've created to persist or stay in 'scope'. So the supply method is called the first tme we request it but on subsequent times we'll get the same instance.

Re-using our Man class from above:

```
...
Person spanishMan = graph.getScoped(Person.class, "Hola!", "SpanishScope");
Person englishMan = graph.getScoped(Person.class, "Hello!", "EnglishScope");

// Requesting objects with the same scope will yield the same instances
graph.getScoped(Person.class, "SpanishScope") == spanishMan; // true
graph.getScoped(Person.class, "EnglishScope") == englishMan; // true
```

*Important* Scoped objects are stored, you'll need to remove them when you're done:

```
// Removing scoped objects
graph.remove(Person.class, "SpanishScope");
graph.remove(Person.class, "EnglishScope");

```

### 2.4. Singleton

For times when we always want the same instance delivered, perhaps for something like a networking interface, we can use addSingleton:

```
Graph graph = new Graph.Builder()
        .addSingleton(INetworking.class, new Graph.Suppliers.Supplier<INetworking, NetworkConfig>() {
            @Override
            public INetworking supply(Graph graph, NetworkConfig networkConfig) {
                return new NetworkingImplementation(networkConfig);
            }
        })
        .build();
NetworkConfig config = /*Config creation*/;
INetworking networking = graph.get(INetworking.class, config);
```

When we call graph.get we'll always get the same instance i.e. the supply method will only be called once.

As mentioned before, we could actually provide the NetworkConfig dependency using the graph:

```
Graph graph = new Graph.Builder()
        .add(INetworkConfig.class, /*Config creation*/)
        .addSingleton(INetworking.class, new Graph.Suppliers.Supplier<INetworking, Void>() {
            @Override
            public INetworking supply(Graph graph, NetworkConfig networkConfig) {
                return new NetworkingImplementation(graph.get(NetworkConfig.class));
            }
        })
        .build();
INetworking networking = graph.get(INetworking.class);
```

### 2.5. Android Scoping Addendum

Just a small note on scoping objects for the Activity lifecycle:

I've been generating a scoping ID and then persisting this across configuration chnages using the onSaveInstanceState/onRestoreInstanceState methods. To make sure the objects are cleared when the activity is actually finished I've been overriding finish/onBackPressed and calling DI.remove here.

