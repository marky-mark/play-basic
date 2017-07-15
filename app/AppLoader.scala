import com.softwaremill.macwire._
import controllers.{Assets, Default}
import modules._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.http.HttpErrorHandler
import router.Routes
import play.api.mvc.EssentialFilter
import play.api.routing.Router

import scala.concurrent.ExecutionContext

class AppLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach { _.configure(context.environment) }
    (new BuiltInComponentsFromContext(context) with ApplicationModules).application
  }
}

trait ApplicationModules extends BuiltInComponents
  with ServiceModule
  with MetricsModule
  with SlickBackedModule
  with ControllersModule
  with EventsProducerModule
  with EventsConsumerModule {

  /**
    * could do val config = ConfigFactory.load()
    * and then def config = Config in each Module
    */

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val prefix: String = "/"

  override lazy val httpErrorHandler: HttpErrorHandler = new ErrorHandler()

  lazy val default: Default = wire[Default]
  lazy val assets: Assets = wire[Assets]
  lazy val router: Router = wire[Routes]

  override lazy val httpFilters: Seq[EssentialFilter] = Seq(metricsFilter)
  metricsPlugin.start()

}
