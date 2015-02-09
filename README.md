Implementing hot publishers in Akka Streams
===============================

Akka Streams

Producing a single element:
```scala
def single[T](element: T)
def apply[T](future: Future[T])
```
Producing a sequence of elements by pulling them on demand:
```scala
def apply[T](iterable: Iterable[T])
def apply[T](f: () ⇒ Iterator[T])
```
Producing a sequence of elements with a specific frequency:
```scala
def apply[T](initialDelay: FiniteDuration, interval: FiniteDuration, tick: () ⇒ T)
```
Basic sources producing no elements or being in error state immediately:
```scala
def empty[T]()
def failed[T](cause: Throwable)
```
Reactive Streams protocol is pull-based in general. To allow the back-pressure to work properly, each subscriber must inform the publisher of the number of elements it can handle and the publisher must never send more than requested. Almost all simple sources listed above are pull-based: Iterable flow rate is completely controlled by the subscriber, the Future result is stored until retrieved. However, the time-based tick source is an exception. Elements are produced by it with a constant frequency, specified on creation. So how can this source be implemented to work together with a subscriber?

When adapting a producer that gives no means of control over generation of elements, we can use one of the following approaches, each of them with different trade-offs.

1. Discard the newly produced element if the subscriber has not indicated any demand (lose data)
2. Buffer the element until the subscriber can receive it (preserve some data or risk of running out of memory)
3. Using a buffer of recently produced elements, aggregate them into a single one (partially preserve information), 
4. Throttle produced elements by constant number or constant time (keep the rate small and preserve only the newest one)
5. A combination of the above

The first approach is the simplest one. If the subscriber has not requested any elements at the time one was produced, it is discarded. The subscriber will receive only the elements that were produced while the demand was active, in order. This solution can be used if the elements produced are not critical and can be lost. However, it has a potential downside 

The tick source uses the first approach. If the subscriber is not prepared to receive the tick at the time it is produced, it is dropped.

There is also a most straightforward example of a push-based source: a generic source that produces an element at any time when it receives a signal (message, method call etc.). It could be then used to implement any source that cannot be controlled.


