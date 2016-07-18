# RxAndroidDemo

### 参考资料:
[有什么使用了RxJava或RxAndroid的开源项目？](https://www.zhihu.com/question/35511144)

[给 Android 开发者的 RxJava 详解](http://gank.io/post/560e15be2dca930e00da1083#toc_1)

[什么是函数响应式编程（Java&Android版本）](https://asce1885.gitbooks.io/android-rd-senior-advanced/content/)

## 1) 创建Observer

###`Observer`：即观察者，它决定事件触发的时候将有怎样的行为。
```java
public interface Observer<T> {
     void onCompleted();
     void onError(Throwable e);
     void onNext(T t);
}
```
```java
Observer<String> observer = new Observer<String>() {
    @Override
    public void onNext(String s) {
        Log.d(tag, "Item: " + s);
    }

    @Override
    public void onCompleted() {
        Log.d(tag, "Completed!");
    }

    @Override
    public void onError(Throwable e) {
        Log.d(tag, "Error!");
    }
};
```
###`Subscriber`：除了`Observer`接口之外，RxJava 还内置了一个实现了 `Observer` 的抽象类：`Subscriber`。 

`Subscriber` 对 `Observer` 接口进行了一些扩展，但他们的基本使用方式是完全一样的。
不仅基本使用方式一样，实质上，在 RxJava 的 subscribe 过程中，Observer 也总是会先被转换成一个 Subscriber 再使用。所以如果你只想使用基本功能，选择 `Observer` 和 `Subscriber` 是完全一样的。

```java
public abstract class Subscriber<T> implements Observer<T>, Subscription {
 	...
}
```

```java
public interface Subscription {
    void unsubscribe();
    boolean isUnsubscribed();
}
```

```java
Subscriber<String> subscriber = new Subscriber<String>() {
    @Override
    public void onNext(String s) {
        Log.d(tag, "Item: " + s);
    }

    @Override
    public void onCompleted() {
        Log.d(tag, "Completed!");
    }

    @Override
    public void onError(Throwable e) {
        Log.d(tag, "Error!");
    }
};
```
它们的区别对于使用者来说主要有两点：

1. `onStart()`: 这是 `Subscriber` 增加的方法。它会在 `subscribe` 刚开始，而事件还未发送之前被调用，可以用于做一些准备工作，例如数据的清零或重置。这是一个可选方法，默认情况下它的实现为空。需要注意的是，如果对准备工作的线程有要求（例如弹出一个显示进度的对话框，这必须在主线程执行）， `onStart()` 就不适用了，因为它总是在 `subscribe` 所发生的线程被调用，而不能指定线程。要在指定的线程来做准备工作，可以使用 `doOnSubscribe()` 方法，具体可以在后面的文中看到。
2. `unsubscribe()`: 这是 `Subscriber` 所实现的另一个接口 `Subscription` 的方法，用于取消订阅。在这个方法被调用后，`Subscriber` 将不再接收事件。一般在这个方法调用前，可以使用 `isUnsubscribed()` 先判断一下状态。 `unsubscribe()` 这个方法很重要，因为在 `subscribe()` 之后， `Observable` 会持有 `Subscriber` 的引用，这个引用如果不能及时被释放，将有内存泄露的风险。所以最好保持一个原则：要在不再使用的时候尽快在合适的地方（例如 `onPause()` `onStop()` 等方法中）调用 `unsubscribe()` 来解除引用关系，以避免内存泄露的发生。

## 2) 创建 Observable
###`Observable`即被观察者，它决定什么时候触发事件以及触发怎样的事件。 RxJava 使用`create()`方法来创建一个`Observable`，并为它定义事件触发规则。

```java
Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
    @Override
    public void call(Subscriber<? super String> subscriber) {
        subscriber.onNext("Hello");
        subscriber.onNext("Hi");
        subscriber.onNext("Aloha");
        subscriber.onCompleted();
    }
});
```
可以看到，这里传入了一个 `OnSubscribe` 对象作为参数。`OnSubscribe` 会被存储在返回的 `Observable` 对象中，它的作用相当于一个计划表，当 `Observable` 被订阅的时候，`OnSubscribe` 的 `call()` 方法会自动被调用，事件序列就会依照设定依次触发（对于上面的代码，就是观察者Subscriber 将会被调用三次 `onNext()` 和一次 `onCompleted()`。这样，由被观察者调用了观察者的回调方法，就实现了由被观察者向观察者的事件传递，即观察者模式。

`create()` 方法是 RxJava 最基本的创造事件序列的方法。基于这个方法， RxJava 还提供了一些方法用来快捷创建事件队列，例如：

* `just(T...)`: 将传入的参数依次发送出来。

```java
Observable observable2 = Observable.just("Hello", "Hi", "Aloha");
```
* `from(T[]) / from(Iterable<? extends T>)` : 将传入的数组或 Iterable 拆分成具体对象后，依次发送出来。

```java
String[] words = {"Hello", "Hi", "Aloha"};
Observable observable3 = Observable.from(words);
```
上面 `just(T...)` 的例子和 `from(T[])` 的例子，都和之前的 `create(OnSubscribe)` 的例子是等价的。

## 3) Subscribe (订阅)
创建了 `Observable` 和 `Observer` 之后，再用 `subscribe()` 方法将它们联结起来，整条链子就可以工作了。代码形式很简单：

```java
observable.subscribe(observer);
// 或者：
observable.subscribe(subscriber);
```
`Observable.subscribe(Subscriber)` 的内部实现是这样的（仅核心代码）：

```java
// 注意：这不是 subscribe() 的源码，而是将源码中与性能、兼容性、扩展性有关的代码剔除后的核心代码。
// 如果需要看源码，可以去 RxJava 的 GitHub 仓库下载。
public Subscription subscribe(Subscriber subscriber) {
    subscriber.onStart();
    onSubscribe.call(subscriber);
    return subscriber;
}
```
可以看到，`subscriber()`做了3件事：

1. 调用 `Subscriber.onStart()` 。这个方法在前面已经介绍过，是一个可选的准备方法。
2. 调用 `Observable` 中的 `OnSubscribe.call(Subscriber)` 。在这里，事件发送的逻辑开始运行。从这也可以看出，在 RxJava 中， `Observable` 并不是在创建的时候就立即开始发送事件，而是在它被订阅的时候，即当 `subscribe()` 方法执行的时候。
3.  将传入的 `Subscriber` 作为 `Subscription` 返回。这是为了方便 `unsubscribe()`。

除了 `subscribe(Observer)` 和 `subscribe(Subscriber)` ，`subscribe()` 还支持不完整定义的回调，RxJava 会自动根据定义创建出 `Subscriber` 。形式如下：

```java
Action1<String> onNextAction = new Action1<String>() {
    // onNext()
    @Override
    public void call(String s) {
        Log.d(tag, s);
    }
};
Action1<Throwable> onErrorAction = new Action1<Throwable>() {
    // onError()
    @Override
    public void call(Throwable throwable) {
        // Error handling
    }
};
Action0 onCompletedAction = new Action0() {
    // onCompleted()
    @Override
    public void call() {
        Log.d(tag, "completed");
    }
};

// 自动创建 Subscriber ，并使用 onNextAction 来定义 onNext()
observable.subscribe(onNextAction);
// 自动创建 Subscriber ，并使用 onNextAction 和 onErrorAction 来定义 onNext() 和 onError()
observable.subscribe(onNextAction, onErrorAction);
// 自动创建 Subscriber ，并使用 onNextAction、 onErrorAction 和 onCompletedAction 来定义 onNext()、 onError() 和 onCompleted()
observable.subscribe(onNextAction, onErrorAction, onCompletedAction);
```
简单解释一下这段代码中出现的 `Action1` 和 `Action0`。 `Action0` 是 RxJava 的一个接口，它只有一个方法 `call()`，这个方法是无参无返回值的；由于 `onCompleted()` 方法也是无参无返回值的，因此 `Action0` 可以被当成一个包装对象，将 `onCompleted()` 的内容打包起来将自己作为一个参数传入 `subscribe()` 以实现不完整定义的回调。这样其实也可以看做将 `onCompleted()` 方法作为参数传进了 `subscribe()`，相当于其他某些语言中的『闭包』。 `Action1` 也是一个接口，它同样只有一个方法 `call(T param)`，这个方法也无返回值，但有一个参数；与 Action0 同理，由于 `onNext(T obj)` 和 `onError(Throwable error)` 也是单参数无返回值的，因此 Action1 可以将 `onNext(obj)` 和 `onError(error)`打包起来传入 `subscribe()` 以实现不完整定义的回调。事实上，虽然 `Action0` 和 `Action1` 在 API 中使用最广泛，但 RxJava 是提供了多个 `ActionX `形式的接口 (例如 `Action2`, `Action3`) 的，它们可以被用以包装不同的无返回值的方法。

## 4） 场景示例

####打印字符串数组

将字符串数组 names 中的所有字符串依次打印出来：

```java
String[] names = {"Hello", "Hi", "Aloha"};
Observable.from(names)
    .subscribe(new Action1<String>() {
        @Override
        public void call(String name) {
            Log.d(tag, name);
        }
    });
```

正如上面两个例子这样，创建出 Observable 和 Subscriber ，再用 subscribe() 将它们串起来，一次 RxJava 的基本使用就完成了。非常简单。
然而，在 RxJava 的默认规则中，事件的发出和消费都是在同一个线程的。也就是说，如果只用上面的方法，实现出来的只是一个同步的观察者模式。观察者模式本身的目的就是『后台处理，前台回调』的异步机制，因此异步对于 RxJava 是至关重要的。而要实现异步，则需要用到 RxJava 的另一个概念： `Scheduler` 。