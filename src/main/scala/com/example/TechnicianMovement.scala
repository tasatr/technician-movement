//#full-example
package com.example

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.stream.scaladsl.{ FileIO, Flow, Keep, Sink, Source }
import java.nio.file.Paths
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.alpakka.csv.scaladsl.CsvToMap
import akka.stream.ActorMaterializer
import java.io.File
import java.nio.file.StandardOpenOption._
import scala.util.{ Failure, Success }
import akka.util.Timeout

//import akka.stream.scaladsl.Framing
//import akka.util.ByteString

//#greeter-companion
//#greeter-messages
object TechnicianActor {
  //#greeter-messages
  def props(name: String): Props = Props(new TechnicianActor(name))
  //#greeter-messages
  final case class SetVessel(vesselName: String)
  final case class ChangeStatus(newStatus: String)
}
//#greeter-messages
//#greeter-companion

//#technician-actor
class TechnicianActor(name: String) extends Actor {
  import TechnicianActor._
  //
  //  var vessel = ""
  //  var currentStatus = ""
  //
  def receive = {
    case SetVessel(vesselName) => {
      //      vessel = vesselName
      println("Setting vessel to " + vesselName)
    }
    case ChangeStatus(newStatus) => {
      //      currentStatus = newStatus
      println(name + " new status is: " + newStatus)
    }
    case _ =>
      //#greeter-send-message
      println("Unknown message: " + _)
    //#greeter-send-message
  }
}
//#technician-actor

object SerializationDemo extends App {

  implicit val system = ActorSystem("technicians")
  implicit val mat = ActorMaterializer()

  def generateActor(name: String, vessel: String, movementType: String) = {
    // Create the 'technician' actors

    println("creating actor " + name);
    try {
      val technicianActor: ActorRef = system.actorOf(Props(new TechnicianActor(name)), name)

      println("Finished creating actor " + technicianActor.path);
      technicianActor ! TechnicianActor.SetVessel(vessel)
      technicianActor ! TechnicianActor.ChangeStatus(movementType)
    } catch {
      case e: Exception =>
        println(e.getMessage)
        system.actorSelection("/user/" + name) ! TechnicianActor.SetVessel(vessel)
        system.actorSelection("/user/" + name) ! TechnicianActor.ChangeStatus(movementType)

    }

  }

  FileIO.fromPath(Paths.get("movements.csv"))
    .via(CsvParsing.lineScanner())
    .via(CsvToMap.toMap())
    .map(_.mapValues(_.utf8String))
    .runForeach(x => generateActor(x("Person"), x("Location"), x("Movement type")))

} 
  