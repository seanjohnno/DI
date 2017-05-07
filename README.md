## 1. DI

Lightweight dependency injection container that I've been using in my Android projects. 

There's only a single .java file (DI.java) and a matching test so just copy and paste to use. Hope it's of some use :)

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
DI.add(Person.class, new DI.Providers.Provider<Person, Void>() {
            @Override
            public TestInterface provide(DI diContainer, Void empty) {
                return new EnglishMan();
            }
        });

// ...and getting an instance:
Person person = DI.get(Person.class);
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
DI.add(Person.class, new DI.Providers.Provider<Person, String>() {
            @Override
            public TestInterface provide(DI diContainer, String helloMsg) {
                return new Man(helloMsg);
            }
        });

// When we get we can pass in our parameters
Person spanishMan = DI.get(Person.class, "Hola!");
String theySaid = spanishPerson.sayHello();
System.out.println(theySaid); // Prints "Hola!"
```

### 2.3. Scoped

Sometimes, it's useful to have a dependency we've created to persist or stay in 'scope'. So the provide method is called the first tme we request it but on subsequent times we'll get the same instance.

Re-using our Man class from above:

```
Person spanishMan = DI.getScoped(Person.class, "Hola!", "SpanishScope");
Person englishMan = DI.getScoped(Person.class, "Hello!", "EnglishScope");

// Requesting objects with the same scope will yield the same instances
DI.getScoped(Person.class, "SpanishScope") == spanishMan; // true
DI.getScoped(Person.class, "EnglishScope") == englishMan; // true
```

*Important* Scoped objects are statically stored, you'll need to remove them when you're done:

```
// Removing scoped objects
DI.remove(Person.class, "SpanishScope");
DI.remove(Person.class, "EnglishScope");

```

### 2.4. Singleton

For times when we always want the same instance delivered, perhaps for something like a networking interface, we can use addSingleton:

```
DI.addSingleton(INetworking.class, new DI.Providers.Provider<Person, NetworkConfig>() {
            @Override
            public INetworking provide(DI diContainer, NetworkConfig networkConfig) {
                return new NetworkingImplementation(networkConfig);
            }
        });
```

When we call DI.get we'll always get the same instance i.e. the provide method will only be called once. 

### 2.5. Android Scoping Addendum

Just a small note on scoping objects for the Activity lifecycle:

I've been generating a scoping ID and then persisting this across configuration chnages using the onSaveInstanceState/onRestoreInstanceState methods. To make sure the objects are cleared when the activity is actually finished I've been overriding finish/onBackPressed and calling DI.remove here.

