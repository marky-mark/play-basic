package com.markland.api.client.common.client

import play.api.libs.ws.{WSRequest, WSResponse, WSResponseHeaders}

import scala.concurrent.{ExecutionContext, Future}

object Result {
  case class Success[T](responseCode: Int, message: Option[String], body: Option[T] = None, rawResponse: play.api.libs.ws.WSResponse)
  case class Error[T](responseCode: Int, message: Option[String] = None, body: Option[T] = None, rawResponse: play.api.libs.ws.WSResponse)
}

case class RequestTimeout[C](duration: scala.concurrent.duration.Duration)

case class StreamError(responseHeaders: WSResponseHeaders)

object RequestHandler {
  def apply[R](request: WSRequest, method: String, reqTransform: WSRequest => WSRequest = identity[WSRequest])(transform: WSResponse => R) = {
    new RequestHandler[R](request, method, reqTransform, transform)
  }
}

class RequestHandler[R] private[client] (private val request: WSRequest, private val method: String, private val reqTransform: WSRequest => WSRequest, private val responseTransform: WSResponse => R) {

  def apply(transform: WSRequest => WSRequest): RequestHandler[R] = new RequestHandler[R](request, method, reqTransform andThen transform, responseTransform)

  def run()(implicit ec: ExecutionContext): Future[R] = reqTransform(request).execute(method).map(responseTransform)
}


