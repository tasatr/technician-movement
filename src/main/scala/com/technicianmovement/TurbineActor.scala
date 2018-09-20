package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.Logging

object TurbineActor {
  //#greeter-messages
  def props(turbineID: String): Props = Props(new TurbineActor(turbineID))
  //#greeter-messages
  final case class SetStatus(date: String, power: String, status: String)
}

class TurbineActor(turbineID: String) extends Actor {
  import TechnicianActor._

  val log = Logging(context.system, this)

  var vessel = ""
  var currentStatus = ""

  def setNewStatus(date: String, power: String, status: String) {

    status match {
      case "Broken" => {
          //TODO: throw error
          val errorMessage = Utils.getErrorMessage(date, turbineID, "", "Turbine is broken", "open");
          log.error(errorMessage);
        
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