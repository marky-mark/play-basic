package modules

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext

trait ExecutionContexts {
  def actorSystem: ActorSystem

  def executionContext(name: String): ExecutionContext = actorSystem.dispatchers.lookup(name)
}
