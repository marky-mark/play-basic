package setup

import java.time.{Clock, Instant, ZoneOffset}

import play.api.{Configuration, Environment, Mode}


trait BaseITSetup {
  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)
  val environment = Environment.simple().copy(mode = Mode.Dev)

  val configuration = Configuration.load(Environment.simple())
}
