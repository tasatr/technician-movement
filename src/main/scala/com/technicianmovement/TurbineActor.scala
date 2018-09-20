package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.Logging

object TurbineActor {
  //#greeter-messages
  def props(turbineID: String, loggerActor: ActorRef): Props = Props(new TurbineActor(turbineID, loggerActor))
  //#greeter-messages
  final case class SetStatus(date: Long, power: String, status: String)
}

class TurbineActor(turbineID: String, loggerActor: ActorRef) extends Actor with ActorLogging {
  import TurbineActor._
  import LoggerActor._
  
//  val log = Logging(context.system, this)

  var vessel = ""
  var currentStatus = "Working"
  var brokenSince: Long = -1
  var technician = ""
  var technicianEntered: Long = -1
  var technicianExited: Long = -1
  

  def setNewStatus(date: Long, power: String, status: String) {

    status match {
      case "Broken" => {
          if (currentStatus == "Working") {
            //This turbine has just stopped working. Set status to "Broken"
            currentStatus = "Broken"
            brokenSince = date
            val errorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine is broken", "open")
            loggerActor ! LogError(errorMessage)
          } else {
            //TODO: check how long it has been broken
            if (date - brokenSince > 4*60*60*1000) {
              //This turbine has been broken for 4 hours
              val errorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine has been broken for more than 4 hours", "open")
              loggerActor ! LogError(errorMessage)              
            }
          }        
      }
        case _ => {//do nothing
      }
      
    }
  }

  def receive = {
    case SetStatus(date, power, status) => {
      setNewStatus(date, power, status)
    }
    case _ =>
      //#greeter-send-message
      log.warning("Unknown message")
    //#greeter-send-message
  }
}