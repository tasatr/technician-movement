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
    var isOldVesselTurbine: Boolean = false

    vesselPattern.findFirstMatchIn(newVessel) match {
      case Some(_) => isTurbine = false
      case None    => isTurbine = true
    }

    vesselPattern.findFirstMatchIn(vessel) match {
      case Some(_) => isOldVesselTurbine = false
      case None    => isOldVesselTurbine = true
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
          //This is the first record for the current technician
          vessel = newVessel
          currentStatus = newMovement
        } else {
          //Throw error : technician can only exit from the vessel that it has previously entered
          val errorMessage = Utils.getErrorMessage(date, newVessel, name, "Invalid exiting (old:'" + vessel + "(" + currentStatus + ")' - new:'" + newVessel + "(" + newMovement + ")')", "open");
          log.error(errorMessage)
          loggerActor ! LogError(errorMessage)
          vessel = newVessel
          currentStatus = newMovement
        }
      }
      case "Enter" => {
        if (currentStatus == "Exit" && Utils.logicalXOR(isTurbine, isOldVesselTurbine)) {
          log.info("This is correct. Update the existing status from " + currentStatus + " to " + newMovement + ". Previous vessel " + vessel + ", new vessel " + newVessel)
          //Check that if turbine was entered, then ship was exited and vice versa
          vessel = newVessel
          currentStatus = newMovement
        } else if (currentStatus == "" && vessel == "") {
          //This is the first record for the current technician
          vessel = newVessel
          currentStatus = newMovement
//        } else if (currentStatus == "Exit" && !isTurbine && !isOldVesselTurbine) {
//          //A technician might take multiple ships to work?
//          log.info("This is correct. Update the existing status from " + currentStatus + " to " + newMovement + ". Previous vessel " + vessel + ", new vessel " + newVessel)
//          vessel = newVessel
//          currentStatus = newMovement
        } else {
          //Throw an error if user enters a turbine after exiting a turbine, or enters a ship after exiting a ship
          val errorMessage = Utils.getErrorMessage(date, newVessel, name, "Invalid entering (old:'" + vessel + "(" + currentStatus + ")' - new:'" + newVessel + "(" + newMovement + ")')", "open");
          log.error(errorMessage)
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