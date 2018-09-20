package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.Logging

object TechnicianActor {
  //#greeter-messages
  def props(name: String): Props = Props(new TechnicianActor(name))
  //#greeter-messages
  final case class SetStatus(date: Long, newVessel: String, newMovement: String)
}

class TechnicianActor(name: String) extends Actor {
  import TechnicianActor._

  val log = Logging(context.system, this)

  var vessel = ""
  var currentStatus = ""

  def setNewStatus(date: Long, newVessel: String, newMovement: String) {

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
          //TODO: throw error
          val errorMessage = Utils.getErrorMessage(date, newVessel, name, "Invalid movement: Previously vessel: " + vessel + ", new vessel: " + newVessel + ". Previous movement: " + currentStatus + ", new movement: " + newMovement, "open");
          log.error(new InvalidMovementException("This is an incorrect state."), errorMessage);
          //          log.error(new InvalidMovementException("This is an incorrect state."), " Previously vessel: " + vessel + ", new vessel: " + newVessel + ". Previous movement: " + currentStatus + ", new movement: " + newMovement)
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
          log.error(new InvalidMovementException("This is an incorrect state."), errorMessage)
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