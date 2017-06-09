package api.client

import com.markland.service.models.Info
import com.markland.service.tags.ids._
import com.markland.api.client.play_basic.{Client, ClientImpl}
import com.markland.api.client.common.client.RequestTimeout
import com.markland.api.client.common.client.Result.{Error, Success}
import models.Models._
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz._

trait ServiceInfo {
  def list(salesChannelId: SalesChannelId, flowId: Option[FlowId])(implicit ec: ExecutionContext): EitherT[Future, ServiceError, Iterable[Info]]
}

class ServiceInfoImpl(wsClient: WSClient,
                      configuration: InfoClientConfig) extends ServiceInfo {

  private implicit val requestTimeout = RequestTimeout[Client](30.seconds)
  private val client = new ClientImpl(baseUrl = configuration.baseUrl, wsClient = wsClient)

  override def list(salesChannelId: SalesChannelId, flowId: Option[FlowId])(implicit ec: ExecutionContext): EitherT[Future, ServiceError, Iterable[Info]] = EitherT {

      val res: Future[Either[Error[Nothing], Success[Seq[Info]]]] = client.salesChannelsInfos.list(
        salesChannelId = salesChannelId, xFlowId = flowId, ifModifiedSince = None, acceptEncoding = Some("gzip, deflate")
      ).run()

      res.map(i => i.fold(handleError, handleSuccess))
  }

  private def handleError(error: Error[Nothing]): \/[ServiceError, Iterable[Info]] = {
    Logger.error(error.message.getOrElse("Category client error: failed to obtain categories from BM"))
    ServiceError(`type` = ClientError, message = s"Response code: ${error.responseCode}. Reason: ${error.message.getOrElse("Unknown")}").left
  }

  private def handleSuccess(success: Success[Seq[Info]]): \/[ServiceError, Iterable[Info]] = {
    success.body match {
      case Some(result) =>
        Logger.info(s"Number of categories, returned by BM: ${result.size}")
        result.right
      case _ => ServiceError(`type` = ClientError, message = "Empty body in the BM response").left
    }
  }

}

object ServiceInfo {
  def apply(config: InfoClientConfig,
            wsClient: WSClient
            ): ServiceInfo = {
    new ServiceInfoImpl(wsClient, config)
  }
}
