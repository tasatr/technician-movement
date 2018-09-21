package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.Logging
import scala.util.matching.Regex

object TechnicianActor {
  //#greeter-messages
  def props(name: String, loggerActor: ActorRef): Props = Props(new TechnicianActor(name, loggerActor))
  //#greeter-messages
  final case class SetStatus(date: Long, newVessel: String, newMovement: String)
}

class TechnicianActor(name: String, loggerActor: ActorRef) extends Actor with ActorLogging {
  import TechnicianActor._
  import LoggerActor._

  var vessel = ""
  var currentStatus = ""

  def setNewStatus(date: Long, newVessel: String, newMovement: String) {
    //Check if vessel is ship or turbine
    val vesselPattern: Regex = "[vV]essel*".r
    var isTurbine: Boolean = false

    vesselPattern.findFirstMatchIn(newVessel) match {
      case Some(_) => isTurbine = false
      case None    => isTurbine = true
    }

    if (isTurbine) {
      //Update turbine actor
      context.system.actorSelection("/user/" + newVessel) ! TurbineActor.UpdateTechnician(date, name, newMovement)
    }

    newMovement match {
      case "Exit" => {
        if (currentStatus == "Enter" && vessel == newVessel) {
          //This is the only acceptable scenario in case Exit is received
          log.info("This is correct. Update the existing status from " + currentStatus + " to " + newMovement + " on vessel " + vessel)
          vessel = newVessel
          currentStatus = newMovement
        } else if (currentStatus == "" && vessel == "") {
          vessel = newVessel
          currentStatus = newMovement
        } else {
          //Throw error : technician can only exit from the vessel that it has previously entered
          val errorMessage = Utils.getErrorMessage(date, newVessel, name, "Invalid movement: Previously vessel: " + vessel + ", new vessel: " + newVessel + ". Previous movement: " + currentStatus + ", new movement: " + newMovement, "open");
          log.error(errorMessage)
          loggerActor ! LogError(errorMessage)
          vessel = newVessel
          currentStatus = newMovement
        }
      }
      case "Enter" => {
        if (currentStatus == "Exit") {
          log.info("This is correct. Update the existing status from " + currentStatus + " to " + newMovement + ". Previous vessel " + vessel + ", new vessel " + newVessel)
          //TODO: check that if turbine was entered, then ship was exited and vice versa
          vessel = newVessel
          currentStatus = newMovement
        } else if (currentStatus == "" && vessel == "") {
          vessel = newVessel
          currentStatus = newMovement
        } else {
          val errorMessage = Utils.getErrorMessage(date, newVessel, name, "Invalid movement: Previously vessel: " + vessel + ", new vessel: " + newVessel + ". Previous movement: " + currentStatus + ", new movement: " + newMovement, "open");
//          log.error(new InvalidMovementException("This is an incorrect state."), errorMessage)
          loggerActor ! LogError(errorMessage)
          vessel = newVessel
          currentStatus = newMovement
        }
      }
      case _ => {
        log.warning("This is an unknown movement: " + newMovement)
      }
    }
  }

  def receive = {
    case SetStatus(date, vesselName, newStatus) => {
      setNewStatus(date, vesselName, newStatus)
    }
    case _ =>
      //#greeter-send-message
      log.warning("Unknown message")
    //#greeter-send-message
  }
}