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

//import akka.stream.scaladsl.Framing
//import akka.util.ByteString

//#greeter-companion
//#greeter-messages
object Technician {
  //#greeter-messages
  def props(name: String): Props = Props(new Technician(name))
  //#greeter-messages
  final case class SetVessel(vesselName: String)
  final case class ChangeStatus(newStatus: String)
  case object Greet
}
//#greeter-messages
//#greeter-companion

//#technician-actor
class Technician(name: String) extends Actor {
  import Technician._
  
  var vessel = ""
  var currentStatus = ""
  
  def receive = {
    case SetVessel(vesselName) => {
      vessel = vesselName
      println("Setting vessel to " + vesselName)
    }
    case ChangeStatus(newStatus) => {
      currentStatus = newStatus
      println(name + " new status is: " + newStatus)
    }
    case _ =>
      //#greeter-send-message
      println("Unknown message: " + _)
    //#greeter-send-message
  }
}
//#technician-actor

//#printer-companion
//#printer-messages
object Printer {
  //#printer-messages
  def props: Props = Props[Printer]
  //#printer-messages
  final case class Greeting(greeting: String)
}
//#printer-messages
//#printer-companion

//#printer-actor
class Printer extends Actor with ActorLogging {
  import Printer._

  def receive = {
    case Greeting(greeting) =>
      log.info("Greeting received (from " + sender() + "): " + greeting)
  }
}
//#printer-actor

object SerializationDemo extends App {

  implicit val system = ActorSystem("technicians")
  implicit val mat = ActorMaterializer()

  // Create the printer actor
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  def generateActor (name: String, vessel: String, movementType: String) = {
    // Create the 'technician' actors
    println("creating actor " + name);
    val technicianActor: ActorRef = system.actorOf(Technician.props(name), name)
    
    technicianActor ! Technician.SetVessel(vessel)
    technicianActor ! Technician.ChangeStatus(movementType)
  }
  
  FileIO.fromPath(Paths.get("movements.csv"))
    .via(CsvParsing.lineScanner())
    .via(CsvToMap.toMap())
    .map(_.mapValues(_.utf8String))
    .runForeach(x => generateActor(x("Person"), x("Location"), x("Movement type")))
    

} 
  
  
//#main-class
//object AkkaQuickstart extends App {
//  import Greeter._
//  
//  //Read 'movements.csv' file
//  implicit val system = ActorSystem("serialization")
//  implicit val mat = ActorMaterializer()
//
//    FileIO.fromPath(Paths.get("movements.csv"))
//  .via(CsvParsing.lineScanner())
//  .via(CsvToMap.toMap())
//  .map(_.mapValues(_.utf8String))
//  .runForeach(println)
//
//  
//  // Create the 'helloAkka' actor system
//  val system: ActorSystem = ActorSystem("helloAkka") 
//  
//  //#create-actors
//  // Create the printer actor
//  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")
//
//  // Create the 'greeter' actors
//  val howdyGreeter: ActorRef =
//    system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
//  val helloGreeter: ActorRef =
//    system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
//  val goodDayGreeter: ActorRef =
//    system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")
//  //#create-actors
//
//  //#main-send-messages
//  howdyGreeter ! WhoToGreet("Akka")
//  howdyGreeter ! Greet
//
//  howdyGreeter ! WhoToGreet("Lightbend")
//  howdyGreeter ! Greet
//
//  helloGreeter ! WhoToGreet("Scala")
//  helloGreeter ! Greet
//
//  goodDayGreeter ! WhoToGreet("Play")
//  goodDayGreeter ! Greet
//  //#main-send-messages
//}
//#main-class
//#full-example
