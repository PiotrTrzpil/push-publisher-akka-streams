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
4. Throttle produced elements by constant number or constant time (keep the rate small and preserve only the newest element)
5. A combination of the above

The first approach is the simplest one. If the subscriber has not requested any elements at the time one was produced, it is discarded. The subscriber will receive only the elements that were produced while the demand was active, in order. This solution can be used if the elements produced are not critical and losing them is permitted. However, event if the elements are not critical, this approach it has a potential downside of favoring older elements over newer ones. Although the elements will be sent to the subscriber immediately, they may saturate the demand giving no chance for the newest ones.

The timed tick source uses this approach. If the subscriber is not prepared to receive the tick at the time it is produced, it is dropped.

The second approach - buffering - is really only a slight modification of the first one. Here, we have two choices: either the buffer will be bounded by some maximum number of elements or unbounded. Bounded one will in most cases just defer the moment of losing an element, but it might be useful if there are some short peaks in rate in the stream. Unbounded one on the other hand can be dangerous: a long rate peak or a consistently high rate of elements compared to the subscriber speed will result in out of memory error. This is something reactive streams strives to avoid in the first place.

The approach of aggregation - the third one - is an interesting one. We can use it only if the elements produced have the property of being combinable, and the most straightforward example of it is a state of some sensor. If our stream represents contant updates of a measured value, we could aggregate them, taking several elements in the buffer and computing value average and timespan it represents. We could do it per constant numer of elements, per constant time or use other algorithm.

Throttling 

There is also a most straightforward example of a push-based source: a generic source that produces an element at any time when it receives a signal (message, method call etc.). It could be then used to implement any source that cannot be controlled.


