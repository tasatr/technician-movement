//#full-example
package com.example

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.stream.scaladsl.{ FileIO }
import java.nio.file.Paths
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.alpakka.csv.scaladsl.CsvToMap
import akka.stream.ActorMaterializer
import java.io.File
import java.nio.file.StandardOpenOption._
import scala.util.{ Failure, Success }
import akka.util.Timeout
import akka.event.Logging
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global

object SerializationDemo extends App {
  
  val baseTime = TimeSettings.getEarliestTime("movements.csv", "turbines.csv")
  
  implicit val system = ActorSystem("technicians")
  implicit val mat = ActorMaterializer()


  def generateActor(name: String, vessel: String, movementType: String, inTime: Long) = {
    // Create the 'technician' actors
      val convertedTime = TimeSettings.getConvertedTime(inTime)

//    println("creating actor " + name);
    try {
      val technicianActor: ActorRef = system.actorOf(Props(new TechnicianActor(name)), name)
      println("Schedule it in " + convertedTime + " milliseconds")
      system.scheduler.scheduleOnce(new FiniteDuration(convertedTime, TimeUnit.MILLISECONDS)) {
        technicianActor ! TechnicianActor.SetStatus(vessel, movementType)
      }
    } catch {
      case e: Exception =>
//        println(e.getMessage)
        system.scheduler.scheduleOnce(new FiniteDuration(convertedTime, TimeUnit.MILLISECONDS)) {
          system.actorSelection("/user/" + name) ! TechnicianActor.SetStatus(vessel, movementType)
        }
    }

  }
    
  FileIO.fromPath(Paths.get("movements.csv"))
    .via(CsvParsing.lineScanner())
    .via(CsvToMap.toMap())
    .map(_.mapValues(_.utf8String))
    .runForeach(x => generateActor(x("Person"), x("Location"), x("Movement type"), TimeSettings.getTimestamp(x("Date"), "dd.MM.yyyy hh:mm") - baseTime))

} 
  