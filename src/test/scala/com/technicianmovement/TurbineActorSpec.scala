package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, Matchers }
import akka.testkit.{ TestKit, TestProbe }
import scala.concurrent.duration._
import scala.language.postfixOps
import TurbineActor._
import LoggerActor._

//#test-classes
class TurbineActorSpec(_system: ActorSystem)
  extends TestKit(_system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {
  //#test-classes

  def this() = this(ActorSystem("TurbineActorSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "A Turbine Actor" should {
    "throw error when status first becomes Broken" in {
      //#specification-example
      val testProbe = TestProbe()
      val turbineID = "B4B"
      val turbineActor = system.actorOf(Props(new TurbineActor(turbineID, testProbe.ref)), turbineID)
      
      turbineActor ! TurbineActor.SetStatus(TimeSettings.getTimestamp("2015-11-23 00:00:00", "yyyy-MM-dd hh:mm:ss"), "3.12", "Working")
      turbineActor ! TurbineActor.SetStatus(TimeSettings.getTimestamp("2015-11-24 00:02:00", "yyyy-MM-dd hh:mm:ss"), "3.12", "Broken")
      testProbe.expectMsg(500 millis, LogError("{\"error\" : \"Turbine is broken\", \"date\" : \"2015-11-24T00:02:00.000+0000\", \"turbine\" : \"B4B\", \"person\" : \"\", \"error_state\" : \"open\"}"))
    }
  }
}
//#full-example
