package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.Logging

object TurbineActor {
  def props(turbineID: String, loggerActor: ActorRef): Props = Props(new TurbineActor(turbineID, loggerActor))
  final case class SetStatus(date: Long, power: String, status: String)
  final case class UpdateTechnician(date: Long, name: String, movement: String)
}

class TurbineActor(turbineID: String, loggerActor: ActorRef) extends Actor with ActorLogging {
  import TurbineActor._
  import LoggerActor._
  
  var vessel = ""
  var currentStatus = "Working"
  var brokenSince: Long = -1
  var technician = ""
  var technicianEntered: Long = -1
  var technicianExited: Long = -1
  var isInErrorState: Boolean = false
  
  def updateTechnicianStatus(date: Long, name: String, movement: String) {
    technician = name
    movement match {
      case "Enter" => 
        technicianEntered = date
        technicianExited = -1
        isInErrorState = false
      case "Exit" => 
        technicianExited = date
        technicianEntered = -1
        isInErrorState = false
    }
  }

  def setNewStatus(date: Long, power: String, status: String) {

    status match {
      case "Broken" => {
          if (currentStatus == "Working") {
            //This turbine has just stopped working. Set status to "Broken"
            currentStatus = "Broken"
            brokenSince = date
            val errorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine is broken", "open")
            log.error(errorMessage)
            loggerActor ! LogError(errorMessage)
          } else {
            if (technicianExited > 0 && (date - technicianExited > 3*60*1000) && !isInErrorState) {
              //Throw an error if technician exited the turbine and it is still broken after 3 minutes
              isInErrorState = true;
              val errorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine is still broken 3 minutes after technician exited", "open")
              log.error(errorMessage)
              loggerActor ! LogError(errorMessage)
            } else if (date - brokenSince > 4*60*60*1000 && technicianEntered < 0 && !isInErrorState){
              //Throw an error if it has been broken for more than 4 hours and technician has not entered the turbine
              isInErrorState = true;
              val errorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine has been broken for more than 4 hours", "open")
              log.error(errorMessage)
              loggerActor ! LogError(errorMessage)  
              
              //Close the previously thrown error of a broken turbine
              val closedErrorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine is broken", "closed")
              log.error(closedErrorMessage)
              loggerActor ! LogError(closedErrorMessage)
            }
          }
      }
      case "Working" => {
        isInErrorState = false
        currentStatus = "Working"
        brokenSince = -1
      }
        case _ => {//do nothing
      }
      
    }
  }

  def receive = {
    case SetStatus(date, power, status) => {
      setNewStatus(date, power, status)
    }
    case UpdateTechnician(date, name, movement) => updateTechnicianStatus(date, name, movement)
    case _ =>
      log.warning("Unknown message")
  }
}