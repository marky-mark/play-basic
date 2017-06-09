package com.markland.api.client.play_basic

import com.markland.api.client.common.client._
import com.markland.service.models._
import play.api.libs.json.Json
import com.markland.service.tags
import com.markland.service.ContentTypes
import com.markland.service.ContentType
import play.api.libs.ws.{WSClient, WSRequest}

import scala.util.{Either, Left, Right}

trait SalesChannelsInfosClient {
  
  def listRaw(rawRequest: WSRequest, xFlowId: Option[tags.ids.FlowId], acceptEncoding: Option[String], ifModifiedSince: Option[String], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Seq[Info]]]]
  def postRaw(rawRequest: WSRequest, body: Info, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]]
  def getRaw(rawRequest: WSRequest, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Info]]]
  def putRaw(rawRequest: WSRequest, body: Info, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]]
  def list(salesChannelId: tags.ids.SalesChannelId, xFlowId: Option[tags.ids.FlowId], acceptEncoding: Option[String], ifModifiedSince: Option[String], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Seq[Info]]]]
  def post(salesChannelId: tags.ids.SalesChannelId, body: Info, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]]
  def get(salesChannelId: tags.ids.SalesChannelId, infoId: tags.ids.InfoId, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Info]]]
  def put(salesChannelId: tags.ids.SalesChannelId, infoId: tags.ids.InfoId, body: Info, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]]
}
     

trait Client {
  def salesChannelsInfos: SalesChannelsInfosClient
}

class ClientImpl(baseUrl: String, wsClient: => WSClient) extends Client {
  import JsonOps._

  override val salesChannelsInfos: SalesChannelsInfosClient = SalesChannelsInfosClient

  object SalesChannelsInfosClient extends SalesChannelsInfosClient {
    def listRaw(rawRequest: WSRequest, xFlowId: Option[tags.ids.FlowId], acceptEncoding: Option[String], ifModifiedSince: Option[String], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Seq[Info]]]] = {
      require(Seq(ContentTypes.ApplicationJson, ContentTypes.ApplicationProblemJson, ContentTypes.ApplicationZip) contains contentType, "Unacceptable contentType specified")
      val headers = Seq(
        "Content-Type" -> s"${contentType.value}; charset=UTF-8"
      ) ++ Seq(
        xFlowId.map("X-Flow-Id" -> _.toString),
        acceptEncoding.map("Accept-Encoding" -> _.toString),
        ifModifiedSince.map("If-Modified-Since" -> _.toString)
      ).flatten
      val request = rawRequest.withHeaders(headers:_*).withRequestTimeout(requestTimeout.duration.toMillis)

      RequestHandler[Either[Result.Error[Nothing], Result.Success[Seq[Info]]]](request, "GET", identity[play.api.libs.ws.WSRequest]) {
        case resp if resp.status == 200 =>
          resp.json.validate[Seq[Info]].fold(
            invalid = errors => Left(Result.Error(resp.status, Some("Invalid body for 'Seq[Info]': " + errors.mkString(", ")), rawResponse = resp)),
            valid = body => Right(Result.Success(resp.status, message = Some("Successful"), Some(body), rawResponse = resp))
          )
        case resp if resp.status == 400 =>
          Left(Result.Error(resp.status, message = Some("Bad Request"), rawResponse = resp))
        case resp if resp.status == 401 =>
          Left(Result.Error(resp.status, message = Some("Unauthorized"), rawResponse = resp))
        case resp if resp.status == 403 =>
          Left(Result.Error(resp.status, message = Some("Forbidden"), rawResponse = resp))
        case resp if resp.status == 404 =>
          Left(Result.Error(resp.status, message = Some("Not Found"), rawResponse = resp))
        case resp =>
          Left(Result.Error(resp.status, message = Some("Unexpected response code"), rawResponse = resp))
      }
    }
           
    def postRaw(rawRequest: WSRequest, body: Info, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]] = {
      require(Seq(ContentTypes.ApplicationJson) contains contentType, "Unacceptable contentType specified")
      val headers = Seq(
        "Content-Type" -> s"${contentType.value}; charset=UTF-8"
      ) ++ Seq(
        xFlowId.map("X-Flow-Id" -> _.toString)
      ).flatten
      val request = rawRequest.withHeaders(headers:_*).withRequestTimeout(requestTimeout.duration.toMillis)
    
      RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]](request, "POST", _.withBody(Json.toJson(body))) {
        case resp if resp.status == 201 =>
          Right(Result.Success(resp.status, message = Some("Created"), rawResponse = resp))
        case resp if resp.status == 400 =>
          Left(Result.Error(resp.status, message = Some("Bad Request"), rawResponse = resp))
        case resp if resp.status == 401 =>
          Left(Result.Error(resp.status, message = Some("Unauthorized"), rawResponse = resp))
        case resp if resp.status == 403 =>
          Left(Result.Error(resp.status, message = Some("Forbidden"), rawResponse = resp))
        case resp =>
          Left(Result.Error(resp.status, message = Some("Unexpected response code"), rawResponse = resp))
      }
    }
           
    def getRaw(rawRequest: WSRequest, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Info]]] = {
      require(Seq(ContentTypes.ApplicationJson, ContentTypes.ApplicationProblemJson) contains contentType, "Unacceptable contentType specified")
      val headers = Seq(
        "Content-Type" -> s"${contentType.value}; charset=UTF-8"
      ) ++ Seq(
        xFlowId.map("X-Flow-Id" -> _.toString)
      ).flatten
      val request = rawRequest.withHeaders(headers:_*).withRequestTimeout(requestTimeout.duration.toMillis)
    
      RequestHandler[Either[Result.Error[Nothing], Result.Success[Info]]](request, "GET", identity[play.api.libs.ws.WSRequest]) {
        case resp if resp.status == 200 =>
          resp.json.validate[Info].fold(
            invalid = errors => Left(Result.Error(resp.status, Some("Invalid body for 'Info': " + errors.mkString(", ")), rawResponse = resp)),
            valid = body => Right(Result.Success(resp.status, message = Some("Successful"), Some(body), rawResponse = resp))
          )
        case resp if resp.status == 400 =>
          Left(Result.Error(resp.status, message = Some("Bad Request"), rawResponse = resp))
        case resp if resp.status == 401 =>
          Left(Result.Error(resp.status, message = Some("Unauthorized"), rawResponse = resp))
        case resp if resp.status == 403 =>
          Left(Result.Error(resp.status, message = Some("Forbidden"), rawResponse = resp))
        case resp if resp.status == 404 =>
          Left(Result.Error(resp.status, message = Some("Not Found"), rawResponse = resp))
        case resp =>
          Left(Result.Error(resp.status, message = Some("Unexpected response code"), rawResponse = resp))
      }
    }
           
    def putRaw(rawRequest: WSRequest, body: Info, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]] = {
      require(Seq(ContentTypes.ApplicationJson) contains contentType, "Unacceptable contentType specified")
      val headers = Seq(
        "Content-Type" -> s"${contentType.value}; charset=UTF-8"
      ) ++ Seq(
        xFlowId.map("X-Flow-Id" -> _.toString)
      ).flatten
      val request = rawRequest.withHeaders(headers:_*).withRequestTimeout(requestTimeout.duration.toMillis)
    
      RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]](request, "PUT", _.withBody(Json.toJson(body))) {
        case resp if resp.status == 200 =>
          Right(Result.Success(resp.status, message = Some("Successful"), rawResponse = resp))
        case resp if resp.status == 400 =>
          Left(Result.Error(resp.status, message = Some("Bad Request"), rawResponse = resp))
        case resp if resp.status == 401 =>
          Left(Result.Error(resp.status, message = Some("Unauthorized"), rawResponse = resp))
        case resp if resp.status == 403 =>
          Left(Result.Error(resp.status, message = Some("Forbidden"), rawResponse = resp))
        case resp if resp.status == 404 =>
          Left(Result.Error(resp.status, message = Some("Not Found"), rawResponse = resp))
        case resp =>
          Left(Result.Error(resp.status, message = Some("Unexpected response code"), rawResponse = resp))
      }
    }
           
    def list(salesChannelId: tags.ids.SalesChannelId, xFlowId: Option[tags.ids.FlowId], acceptEncoding: Option[String], ifModifiedSince: Option[String], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Seq[Info]]]] = {
      
      listRaw(wsClient.url(s"$baseUrl/api/sales-channels/${play.utils.UriEncoding.encodePathSegment(salesChannelId.toString, "UTF-8")}/infos"), xFlowId, acceptEncoding, ifModifiedSince, contentType)
    }
           
    def post(salesChannelId: tags.ids.SalesChannelId, body: Info, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]] = {
      
      postRaw(wsClient.url(s"$baseUrl/api/sales-channels/${play.utils.UriEncoding.encodePathSegment(salesChannelId.toString, "UTF-8")}/infos"), body, xFlowId, contentType)
    }
           
    def get(salesChannelId: tags.ids.SalesChannelId, infoId: tags.ids.InfoId, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Info]]] = {
      
      getRaw(wsClient.url(s"$baseUrl/api/sales-channels/${play.utils.UriEncoding.encodePathSegment(salesChannelId.toString, "UTF-8")}/infos/${play.utils.UriEncoding.encodePathSegment(infoId.toString, "UTF-8")}"), xFlowId, contentType)
    }
           
    def put(salesChannelId: tags.ids.SalesChannelId, infoId: tags.ids.InfoId, body: Info, xFlowId: Option[tags.ids.FlowId], contentType: ContentType = ContentTypes.ApplicationJson)(implicit requestTimeout: RequestTimeout[Client]): RequestHandler[Either[Result.Error[Nothing], Result.Success[Nothing]]] = {
      
      putRaw(wsClient.url(s"$baseUrl/api/sales-channels/${play.utils.UriEncoding.encodePathSegment(salesChannelId.toString, "UTF-8")}/infos/${play.utils.UriEncoding.encodePathSegment(infoId.toString, "UTF-8")}"), body, xFlowId, contentType)
    }
           
  }
       
}
       
