package pt.streams

import akka.actor.{Props, ActorSystem}
import akka.stream.actor.ActorPublisher
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.Source
import org.reactivestreams.Publisher
import org.reactivestreams.tck.{PublisherVerification, TestEnvironment}
import org.scalatest.testng.TestNGSuiteLike
import scala.concurrent.duration._

class PeriodicPublisher(maxElements:Long) extends PushBasedBufferedPublisher[String] {
   implicit val materializer = FlowMaterializer()
   implicit val _ = context.dispatcher
   Source(50 milli, 5 milli, () => "a")
     .take(maxElements.toInt)
     .foreach(self ! Publish(_))
     .onComplete(_=>{
      self ! Complete
   })

}
class ConcurrentPublisherTest(env: TestEnvironment, publisherShutdownTimeout: Long)
  extends PublisherVerification[String](env, publisherShutdownTimeout) with TestNGSuiteLike {

   def this() {
      this(new TestEnvironment(1000), 500)
   }
   implicit val sys = ActorSystem()
   implicit val materializer = FlowMaterializer()


   def createPublisher(elements: Long): Publisher[String] =
      ActorPublisher(sys.actorOf(Props(classOf[PeriodicPublisher], elements)))

   // we dont have any errors to happen in the stream
   override def createErrorStatePublisher(): Publisher[String] = null

}