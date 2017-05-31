package services.slickbacked

import config.ConfigOps
import play.api.Configuration
import slick.basic.DatabaseConfig

class DatabaseProvider(configuration: Configuration) {

  val databaseConfig = DatabaseConfig.forConfig[ExtendedPostgresDriver] (
    configuration.getMandatoryString("database.config.name"),
    configuration.underlying
  )
  val dataModel = new DataModel(databaseConfig.profile)
  val database = databaseConfig.db

}
