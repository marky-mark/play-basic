package services.slickbacked

import com.typesafe.config.{Config, ConfigFactory}
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.Configuration

object DBProvidedIT {

  println("Sleeping a bit....this is due to \"sbt dockerComposeTest\" starting the tests too early...sorry ¯\\_(ツ)_/¯")
  Thread.sleep(10000)

  val config: Config = ConfigFactory.load("application.it.conf")
  val dbConfig = config.getConfig("database.postgres.db")

  val flyway = new Flyway()
  flyway.setDataSource(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))
  flyway.setLocations("filesystem:flyway/migration/postgres")

  val databaseProvider: DatabaseProvider = new DatabaseProvider(Configuration(config))
  val db = databaseProvider.database
  val dataModel = databaseProvider.dataModel
}

trait DBProvidedIT extends BeforeAndAfterEach { self: Suite =>

  val databaseProvider = DBProvidedIT.databaseProvider
  val db = DBProvidedIT.db
  val dataModel = DBProvidedIT.dataModel

  val flyway = DBProvidedIT.flyway

  def createDb() = {
    flyway.migrate()
  }

  def dropDb() = {
    flyway.clean()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    createDb()
  }

  override protected def afterEach(): Unit = {
    dropDb()
    super.afterEach()
  }
}
