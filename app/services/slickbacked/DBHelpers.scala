package services.slickbacked

import java.sql.Connection

import org.postgresql.util.PGobject
import play.api.Logger
import play.api.libs.json.JsValue

object DBHelpers {

  def closing[R <: AutoCloseable, T](allocate: => R)(fn: R => T): T = {
    val resource = allocate
    try {
      fn(resource)
    } finally {
      try resource.close()
      catch {
        case ex: Throwable =>
          Logger.warn(s"Unexpected exception closing JDBC resource: $ex")
      }
    }
  }

  def transactionally[T](conn: Connection)(fn: => T): T = {
    try {
      conn.setAutoCommit(false)
      val result = fn
      conn.commit()
      result
    } catch {
      case ex: Throwable =>
        conn.rollback()
        throw ex
    }
  }

  def asPGObject(json: JsValue) = {
    val pobject = new PGobject()
    pobject.setType("jsonb")
    pobject.setValue(json.toString)
    pobject
  }

}
