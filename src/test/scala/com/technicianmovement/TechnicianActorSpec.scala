//#full-example
package com.technicianmovement

import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, Matchers }
import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import scala.concurrent.duration._
import scala.language.postfixOps
import TechnicianActor._

//#test-classes
class TechnicianActorSpec(_system: ActorSystem)
  extends TestKit(_system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {
  //#test-classes

  def this() = this(ActorSystem("TechnicianActorSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  //#first-test
  //#specification-example
  "A Technician Actor" should {
    "update its status when instructed" in {
      //#specification-example
      val testProbe = TestProbe()
//      val helloGreetingMessage = "hello"
//      val helloGreeter = system.actorOf(Greeter.props(helloGreetingMessage, testProbe.ref))
//      val greetPerson = "Akka"
//      helloGreeter ! WhoToGreet(greetPerson)
//      helloGreeter ! Greet
//      testProbe.expectMsg(500 millis, Greeting(helloGreetingMessage + ", " + greetPerson))
    }
  }
  //#first-test
}
//#full-example
