package filters

import play.Logger
import play.api.mvc._

import scala.concurrent.Future

object LoggingFilter extends Filter{
  override def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {
    val startTime = System.currentTimeMillis
    next(rh).map { result =>
      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime
      play.Logger.of("accesslog").info(s"method=${rh.method} uri=${rh.uri} remote-address=${rh.remoteAddress} " +
        s"domain=${rh.domain} query-string=${rh.rawQueryString} " +
        s"referrer=${rh.headers.get("referrer").getOrElse("N/A")} " +
        s"user-agent=[${rh.headers.get("user-agent").getOrElse("N/A")}]" +
        s"took ${requestTime}ms and returned ${result.header.status}")
      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}
