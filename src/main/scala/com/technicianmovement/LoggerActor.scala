/**
 * This class takes care of logging error messages into a file. It is also handy for testing purposes.
 */
package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import java.io.FileWriter

object LoggerActor {
  def props: Props = Props[LoggerActor]
  final case class LogError(errorMessage: String)
}

class LoggerActor extends Actor with ActorLogging {
  import LoggerActor._

  def receive = {
    case LogError(errorMessage) => {
      val fw = new FileWriter("error.log", true)
      try {
        fw.write(customFormat(errorMessage) + "\n")
      } finally fw.close()
    }
  }
  
  def customFormat(json: String): String = {
    var formattedJson = json.replace("{", "{\n")
    formattedJson = formattedJson.replace("}", "\n}")
    formattedJson = formattedJson.replace(",", ",\n")
    return formattedJson
  }
}