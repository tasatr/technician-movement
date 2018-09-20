package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.Logging

object TurbineActor {
  //#greeter-messages
  def props(turbineID: String): Props = Props(new TurbineActor(turbineID))
  //#greeter-messages
  final case class SetStatus(date: Long, power: String, status: String)
}

class TurbineActor(turbineID: String) extends Actor {
  import TurbineActor._

  val log = Logging(context.system, this)

  var vessel = ""
  var currentStatus = "Working"
  var brokenSince: Long = -1

  def setNewStatus(date: Long, power: String, status: String) {

    status match {
      case "Broken" => {
          //TODO: throw error
          if (currentStatus == "Working") {
            //This turbine has just stopped working. Set status to "Broken"
            currentStatus = "Broken"
            brokenSince = date
            val errorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine is broken", "open")
            log.error(errorMessage)
          } else {
            //TODO: check how long it has been broken
            if (date - brokenSince > 4*60*60*1000) {
              //This turbine has been broken for 4 hours
              val errorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine has been broken for more than 4 hours", "open")
              log.error(errorMessage)
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