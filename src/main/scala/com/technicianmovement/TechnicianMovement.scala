package com.technicianmovement

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
  val log = Logging(system.eventStream, "com.technicianmovement.TechnicianMovement")
   val loggerActor: ActorRef = system.actorOf(Props(new LoggerActor()))

  def generateTechnicianActor(name: String, vessel: String, movementType: String, date: Long, inTime: Long) = {
    // Create the 'technician' actors
    val convertedTime = TimeSettings.getConvertedTime(inTime)

    try {
      val technicianActor: ActorRef = system.actorOf(Props(new TechnicianActor(name, loggerActor)), name)
      log.debug("Schedule " + name + " to " + movementType + " " + vessel + " in " + convertedTime + " milliseconds")
      system.scheduler.scheduleOnce(new FiniteDuration(convertedTime, TimeUnit.MILLISECONDS)) {
        technicianActor ! TechnicianActor.SetStatus(date, vessel, movementType)
      }
    } catch {
      case e: Exception =>
        log.debug("Schedule " + name + " to " + movementType + " " + vessel + " in " + convertedTime + " milliseconds")
        system.scheduler.scheduleOnce(new FiniteDuration(convertedTime, TimeUnit.MILLISECONDS)) {
          system.actorSelection("/user/" + name) ! TechnicianActor.SetStatus(date, vessel, movementType)
        }
    }

  }
  
  def generateTurbineActor(turbineID: String, power: String, status: String, date: Long, inTime: Long) = {
    val convertedTime = TimeSettings.getConvertedTime(inTime)

    try {
      val turbineActor: ActorRef = system.actorOf(Props(new TurbineActor(turbineID, loggerActor)), turbineID)
      log.debug("Schedule " + turbineID + " in " + convertedTime + " milliseconds")
      system.scheduler.scheduleOnce(new FiniteDuration(convertedTime, TimeUnit.MILLISECONDS)) {
        turbineActor ! TurbineActor.SetStatus(date, power, status)
      }
    } catch {
      case e: Exception =>
        log.debug("Schedule " + turbineID + " in " + convertedTime + " milliseconds")
        system.scheduler.scheduleOnce(new FiniteDuration(convertedTime, TimeUnit.MILLISECONDS)) {
          system.actorSelection("/user/" + turbineID) ! TurbineActor.SetStatus(date, power, status)
        }
    }

  }
  
  FileIO.fromPath(Paths.get("turbines.csv"))
    .via(CsvParsing.lineScanner())
    .via(CsvToMap.toMap())
    .map(_.mapValues(_.utf8String))
    .runForeach(x => generateTurbineActor(x("ID"), x("ActivePower (MW)"), x("Status"), TimeSettings.getTimestamp(x("Date"), "yyyy-MM-dd hh:mm:ss"),  TimeSettings.getTimestamp(x("Date"), "yyyy-MM-dd hh:mm:ss") - baseTime))


  FileIO.fromPath(Paths.get("movements.csv"))
    .via(CsvParsing.lineScanner())
    .via(CsvToMap.toMap())
    .map(_.mapValues(_.utf8String))
    .runForeach(x => generateTechnicianActor(x("Person"), x("Location"), x("Movement type"), TimeSettings.getTimestamp(x("Date"), "dd.MM.yyyy hh:mm"),  TimeSettings.getTimestamp(x("Date"), "dd.MM.yyyy hh:mm") - baseTime))

} 
  