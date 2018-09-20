package com.technicianmovement

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }

object LoggerActor {
  def props: Props = Props[LoggerActor]
  final case class LogError(errorMessage: String)
}

class LoggerActor extends Actor with ActorLogging {
  import LoggerActor._

  def receive = {
    case LogError(errorMessage) =>
      log.error(errorMessage)
  }
}