package com.example

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.Logging



object TechnicianActor {
  //#greeter-messages
  def props(name: String): Props = Props(new TechnicianActor(name))
  //#greeter-messages
  final case class SetStatus(newVessel: String, newMovement: String)
}

class TechnicianActor(name: String) extends Actor {
  import TechnicianActor._

  val log = Logging(context.system, this)
  
  var vessel = ""
  var currentStatus = ""

  def setNewStatus(newVessel: String, newMovement: String) {

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
          log.error(new Throwable("This is an incorrect state."), " Previously vessel: " + vessel + ", new vessel: " + newVessel + ". Previous movement: " + currentStatus + ", new movement: " + newMovement)
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
        
          //TODO: throw error
          log.error(new Throwable("This is an incorrect state."), " Previously vessel: " + vessel + ", new vessel: " + newVessel + ". Previous movement: " + currentStatus + ", new movement: " + newMovement)
        }
      }
      case _ => {
        log.error(new Throwable("This is an unknown movement."), "This is an unknown movement.")
        //TODO: throw error
      }
    }
    //Throw error if a person moves onto a turbine without having exited a ship

    //Throw error if a person exits a turbine withouth having entered a turbine

  }

  def receive = {
    case SetStatus(vesselName, newStatus) => {
      setNewStatus(vesselName, newStatus)
    }
    case _ =>
      //#greeter-send-message
      log.warning("Unknown message")
    //#greeter-send-message
  }
}