package services.slickbacked

import java.sql.{Connection, DriverManager}

import com.typesafe.config.{Config, ConfigFactory}
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.Configuration

object DBProvided {
  val config: Config = ConfigFactory.load()
  val dbConfig = config.getConfig("database.postgres.db")

  val flyway = new Flyway()
  flyway.setDataSource(dbConfig.getString("url"), dbConfig.getString("user"), dbConfig.getString("password"))
  flyway.setLocations("filesystem:flyway/migration/postgres")

  val databaseProvider: DatabaseProvider = new DatabaseProvider(Configuration(config))
  val db = databaseProvider.database
  val dataModel = databaseProvider.dataModel
}

trait DBProvided extends BeforeAndAfterEach {
  self: Suite =>
  Class.forName("org.h2.Driver")

  val databaseProvider = DBProvided.databaseProvider
  val db = DBProvided.db
  val dataModel = DBProvided.dataModel

  val flyway = DBProvided.flyway
  var h2Connection: Connection = _

  def createDb() = {
    h2Connection = DriverManager.getConnection("jdbc:h2:file:./build/servers;MODE=PostgreSQL")
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
    h2Connection.close()
    super.afterEach()
  }
}
