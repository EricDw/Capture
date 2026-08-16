# Overview
This is a Kotlin Multiplatform project primarily targeting Android, and the Desktop (JVM).
Server, Web, and iOS (and in that order) may come later, but for now are not in scope.

Capture uses Actor Models and Hierarchical State Trees as the foundation of its design.
This is because combining these to patterns provides scalability and flexibility.
In many cases this combination also replaces the need for other common frameworks and libraries.
ViewModels, Jetpack Navigation, Hilt Dependency Injection, just to name a few.

## Actor Models

Actors are used to standardize how data is held, mutated, and communicated through the application.

### What is an Actor Model
**Disclaimer**

The following definition is not a formal one.
Actor Models in this application do not adhere to the formal definition 100%.
So if you research the actor model you will find stricter and different rules.

---

An Actor Model is an object that manages a set of data and communicates through message passing.

**Independent state**

Each actor keeps its own private data safe inside itself.

**No shared memory**

Actors never modify another actor's data directly, which stops data corruption.

**Mailbox queue**

Incoming messages wait in a line (mailbox) and the actor handles them one at a time.
In our application this would probably only be applicable the to Actors that handle IO operations.

**Asynchronous messaging**

Senders do not wait for a reply before moving on to other tasks.
"Tell, don't ask" is the recommended pattern and is enforced by the design of our `Actor` interface.

**Disclaimer**
The UI is an exception to this rule because the UI layer runs on a single thread.

### Why use an Actor Model

Actors simplify the way information flows throughout the app.
They do this by having only a single function: `fun tell(message: Any)`.
Constraining the way in which information is communicated also forces it to flow in the same manner everywhere.
To help conceptualize this is to imagine two people having a letter based conversation.

They are often come in the form of Actor Trees which the way the State Tree system works.
Each State in the State Trees, as explained below, are also an `Actor` instance.


### When to use an Actor Model

Any object that needs to hold, manage, and or synchronize data could (and in most cases should) be an `Actor`.

An object that owns and writes to a specific location on disk. 
Those `Actor` should not only be an `Actor` but also one that handles 1 message at a time.

### Where to use an Actor Model

The UI layer for holding state.
The data layer for synchronizing IO to disk.

### How to use Actor Models effectively

Never modify the `Actor` interface.
It may be tempting or seem reasonable at times to add another function but don't.
Adding complexity to the interface erodes its purpose and value.

**Tell, don't ask**
Most of the time this is done because one object wants to "ask a question" of an `Actor`. 
This usually comes in the form of a new `fun suspend ask(message: Any): Response` or other such function.
Waiting on a response is sometimes needed, but it can always be accomplished by the `message` being passed to the `tell` function. 

If the response to a message does not need to be blocking or awaited on you can often pass a reference to the caller.
For example:

```kotlin
class GetSomeData(
    val sender: Actor
)
```

If the response to a message _does_ **_need_** to be blocking or awaited on you can often pass a `Defferred<T>`.
This is the recommended approach.
For example:

```kotlin
class GetSomeString(
    val response: CompletableDeffered<String> = CompletableDeffered()
)
```

## State Trees
Capture believes in and embraces State Trees. 

### What is a State Tree
A hierarchical state tree (often called a hierarchical state machine) is an organized way to manage complex logic. 
It nests small states inside larger parent states.
If a parent state is active, its child state can run specific sub-actions, making code cleaner and easier to scale.

### Why use State Trees

**Nested Design**

States live inside other states like folders on a computer.
The UI matches this hierarchy exactly. 
In fact, the Compose runtime is basically a tree management system at its heart.

**Shared Logic**

Parent states handle general rules, while child states handle details.

**Clean Transitions**

UI navigation is often just a transition between states.
State Trees make this task intuitive to handle.
Stack based navigation seems logical at the outset; You can only one place (AKA the top of the stack) at a time.
But this analogy breaks down the moment you add back navigation and substacks.
Those scenarios typically produce more infrastructure to manage because they are trying to be Trees.

**Information Organization and Communication**

Data hoisting is a simple endeavor when dealing with trees.
Pushing data up a node is easy.
Bubbling up events and pushing down data to and from a root node in a tree is also a simple matter.
If the child State has a reference to its holder then it can just tell the holder the information.

**Dependency Injection**

There is almost now need for a formal dependency injection framework when using a State Tree.

### Where to use a State Tree
Primarily as a replacement for the ViewModel and a state holder.
Anywhere you would have a ViewModel you can use a State Tree.
That is exactly how Capture uses them today.

### How to use State Trees Effectively

State Trees are effectively holders, owners, and mutators of the state they own.
That is why the also implement the `Actor` interface.

Properties can and should be added to `State`s. 
Properties **_should never be mutable from outside_** the State Tree itself.
The State Tree is considered to be the owner of the state of the app.

`State`s are `Actors` and as such they should **_never have public functions beyond the `tell` function_**. 
Private functions are completely fine.

### AppContext and MutableAppContext
The application context is split into two interfaces to enforce clear boundaries:

- **AppContext**: A read-only interface intended for the UI layer. It provides access to the current state and common application-level properties.
- **MutableAppContext**: A mutable interface intended for use within state implementations (e.g., `AppState` subclasses). It allows updating the application state, navigation stack, and other global properties.

**Strict Rules:**
1. **No Casting in UI**: The UI layer must only interact with `AppContext`. Casting from `AppContext` to `MutableAppContext` in the UI is strictly prohibited.
2. **Encapsulated Controller**: The application controller is an internal implementation detail of `AppContextImpl`. It is not exposed through either `AppContext` or `MutableAppContext`. All communication with the controller must happen via the `tell(message)` method on the context. Unhandled messages in the context are automatically forwarded to the controller.

Do not put too many properties in a single `State` 5-7 max.
If you have to expose more than that consider wrapping properties together in a class.
Properties that change together, live together.