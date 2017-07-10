package services

import java.sql.Timestamp

import org.joda.time.{DateTime, DateTimeZone}

object DateTimeHelper {
  implicit class TimestampOps(val self: Timestamp) extends AnyVal {
    def toDateTime: DateTime = new DateTime(self.getTime, DateTimeZone.UTC)
  }

  implicit class DateTimeOps(val self: DateTime) extends AnyVal {
    def toTimestamp: Timestamp = new Timestamp(self.getMillis)
  }
}
