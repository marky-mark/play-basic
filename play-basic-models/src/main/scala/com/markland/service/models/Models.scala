package com.markland.service.models

import com.markland.service.tags._

import play.api.libs.json._


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
         

         
