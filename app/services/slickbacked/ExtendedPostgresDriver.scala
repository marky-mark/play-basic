package services.slickbacked

import com.github.tminglei.slickpg._


trait ExtendedPostgresDriver extends ExPostgresProfile  with PgArraySupport with PgPlayJsonSupport with PgDate2Support {
  override def pgjson = "jsonb"

  override val api = ExtendedAPI

  object ExtendedAPI extends API with ArrayImplicits with JsonImplicits with DateTimeImplicits

}

object ExtendedPostgresDriver extends ExtendedPostgresDriver