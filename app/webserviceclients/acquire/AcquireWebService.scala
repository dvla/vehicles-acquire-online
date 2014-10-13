package webserviceclients.acquire

import play.api.libs.ws.WSResponse
import scala.concurrent.Future

// TODO Do we still need this abstraction, now the code base is more mockable?
trait AcquireWebService {
  def callDisposeService(request: AcquireRequestDto, trackingId: String): Future[WSResponse]
}
