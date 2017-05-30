package config

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigException}

import scala.util.Try

object ConfigUtils {

  implicit class ConfigGetOpt(val config: Config) extends AnyVal {
    def getIntOpt(value: String):       Option[Int]       = Try(Option(config.getInt(value))).recover { case _: ConfigException.Missing => None }.get
    def getStringOpt(value: String):    Option[String]    = Try(Option(config.getString(value))).recover { case _: ConfigException.Missing => None }.get
    def getConfigOpt(value: String):    Option[Config]    = Try(Option(config.getConfig(value))).recover { case _: ConfigException.Missing => None }.get
    def getBooleanOpt(value: String):   Option[Boolean]   = Try(Option(config.getBoolean(value))).recover { case _: ConfigException.Missing => None }.get
    def getTimeUnitOpt(value: String):  Option[TimeUnit]  = getStringOpt(value).map(TimeUnit.valueOf)
    def getDurationOpt(value: String):  Option[Duration]  = Try(Option(config.getDuration(value))).recover { case _: ConfigException.Missing => None }.get
  }
}
