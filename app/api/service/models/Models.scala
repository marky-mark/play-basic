package api.service.models

import api.service.tags._



case class Info(
  id: Option[ids.InfoId] = None,
  name: String,
  data: play.api.libs.json.JsObject,
  meta: Seq[String]
)
         

case class Problem(
  title: String,
  status: Int,
  detail: String,
  trackingId: Option[ids.TrackingId] = None
)
         

         
