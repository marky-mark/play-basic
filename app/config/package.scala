import java.time.Duration
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigException}

import scala.collection.JavaConverters._
import play.api.Configuration

import scala.util.Try

//inside package class is really so that all classes can see these objects....did this for fun really
//

package object config {

  implicit class ConfigOps(val configuration: Configuration) extends AnyVal {
    def getMandatoryConfig(name: String): Configuration = configuration.getConfig(name).getOrElse(sys.error(s"Missing '$name' Configuration"))

    def getMandatoryLong(name: String): Long = configuration.getLong(name).getOrElse(sys.error(s"Missing '$name' Long"))

    def getMandatoryInt(name: String): Int = configuration.getInt(name).getOrElse(sys.error(s"Missing '$name' Int"))

    def getMandatoryDouble(name: String): Double = configuration.getDouble(name).getOrElse(sys.error(s"Missing '$name' Double"))

    def getMandatoryString(name: String): String = configuration.getString(name).getOrElse(sys.error(s"Missing '$name' String"))

    def getMandatoryBoolean(name: String): Boolean = configuration.getBoolean(name).getOrElse(sys.error(s"Missing '$name' Boolean"))

    def asJavaMap(): java.util.Map[String, AnyRef] = configuration.underlying.root().unwrapped()

    def asScalaMap(): Map[String, AnyRef] = asJavaMap().asScala.toMap
  }

  implicit class ConfigGetOpt(val config: Config) extends AnyVal {
    def getIntOpt(value: String):       Option[Int]       = Try(Option(config.getInt(value))).recover { case _: ConfigException.Missing => None }.get
    def getStringOpt(value: String):    Option[String]    = Try(Option(config.getString(value))).recover { case _: ConfigException.Missing => None }.get
    def getConfigOpt(value: String):    Option[Config]    = Try(Option(config.getConfig(value))).recover { case _: ConfigException.Missing => None }.get
    def getBooleanOpt(value: String):   Option[Boolean]   = Try(Option(config.getBoolean(value))).recover { case _: ConfigException.Missing => None }.get
    def getTimeUnitOpt(value: String):  Option[TimeUnit]  = getStringOpt(value).map(TimeUnit.valueOf)
    def getDurationOpt(value: String):  Option[Duration]  = Try(Option(config.getDuration(value))).recover { case _: ConfigException.Missing => None }.get
  }

}
