package modules

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import play.api.Logger

import scala.concurrent.ExecutionContext

trait ExecutionContexts {
  def actorSystem: ActorSystem

  def executionContext(name: String): ExecutionContext = actorSystem.dispatchers.lookup(name)

  def fixedSizeExecutionContext(name: String, size: Int): ExecutionContext = {
    ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(size),
      Logger.error(s"Unhandled error in $name context", _)
    )
  }
}
