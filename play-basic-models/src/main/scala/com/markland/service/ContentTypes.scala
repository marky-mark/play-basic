package com.markland.service

case class ContentType(val value: String) extends AnyVal

object ContentTypes {
  implicit class ContentTypeResultOps(val result: play.api.mvc.Result) extends AnyVal {
    def as(ctype: ContentType): play.api.mvc.Result = result.as(ctype.value)
  }
  val ApplicationJson = ContentType("application/json")
  val ApplicationProblemJson = ContentType("application/problem+json")
  val ApplicationInfoPageJson = ContentType("application/x.info-page+json")
  val ApplicationZip = ContentType("application/zip")
}
     
