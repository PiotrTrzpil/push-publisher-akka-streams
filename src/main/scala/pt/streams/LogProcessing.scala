package pt.streams

import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request

trait PushBasedBufferedPublisher[A] extends ActorPublisher[A] {
   case class Publish(message:A)
   case object Complete

   val limit = 10
   var buffer = Vector[A]()
   var completeRequested = false

   def totalDemandInt =
      if (totalDemand > Int.MaxValue)
         Int.MaxValue
      else totalDemand.toInt

   def splitBuffer = {
      val split = if(buffer.size < totalDemandInt) 0
      else buffer.size - totalDemandInt
      buffer.splitAt(split)
   }

   def tryStreaming() = if (isActive) {
      val (remaining, taken) = splitBuffer
      taken.reverse.foreach(onNext)
      buffer = remaining

      if (completeRequested && buffer.isEmpty) {
         onComplete()
      }
   }

   def receive = {
      case Request(n) =>
         tryStreaming()
      case Publish(message) =>
         if (buffer.size >= limit) {
            buffer = message +: buffer.dropRight(1)
         } else {
            buffer = message +: buffer
         }
         tryStreaming()
      case Complete =>
         completeRequested = true
         tryStreaming()
   }
}
trait PushBasedPublisher[A] extends ActorPublisher[A] {
   case class Publish(message:A)
   case object Complete

   def receive = {
      case Request(n) =>
      case Publish(message) =>
         if (isActive && totalDemand > 0) {
            onNext(message)
         }
      case Complete =>
         onComplete()
   }
}

