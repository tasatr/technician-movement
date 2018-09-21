package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, Matchers }
import akka.testkit.{ TestKit, TestProbe }
import scala.concurrent.duration._
import scala.language.postfixOps
import TechnicianActor._
import LoggerActor._

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

  "A Technician Actor" should {
    "throw error when technician exits a vessel without having entered it" in {
      val testProbe = TestProbe()
      val technicianName = "tech1"
      val technicianActor = system.actorOf(Props(new TechnicianActor(technicianName, testProbe.ref)), technicianName)
      
      technicianActor ! TechnicianActor.SetStatus(TimeSettings.getTimestamp("2015-11-23 00:00:00", "yyyy-MM-dd hh:mm:ss"), "vessel1", "Enter")
      technicianActor ! TechnicianActor.SetStatus(TimeSettings.getTimestamp("2015-11-24 00:02:00", "yyyy-MM-dd hh:mm:ss"), "vessel2", "Exit")
      testProbe.expectMsg(500 millis, LogError("{\"error\" : \"Invalid movement: Previously vessel: vessel1, new vessel: vessel2. Previous movement: Enter, new movement: Exit\", \"date\" : \"2015-11-24T00:02:00.000+0000\", \"turbine\" : \"vessel2\", \"person\" : \"tech1\", \"error_state\" : \"open\"}"))

      technicianActor ! TechnicianActor.SetStatus(TimeSettings.getTimestamp("2015-11-24 00:04:00", "yyyy-MM-dd hh:mm:ss"), "vessel3", "Exit")
      testProbe.expectMsg(500 millis, LogError("{\"error\" : \"Invalid movement: Previously vessel: vessel2, new vessel: vessel3. Previous movement: Exit, new movement: Exit\", \"date\" : \"2015-11-24T00:04:00.000+0000\", \"turbine\" : \"vessel3\", \"person\" : \"tech1\", \"error_state\" : \"open\"}"))
    }
  }
  
}
//#full-example
