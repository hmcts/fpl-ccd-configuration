package uk.gov.hmcts.reform.fpl.actions.ccd

import java.util.concurrent.ThreadLocalRandom

import io.gatling.core.Predef._
import io.gatling.core.action.builder.PauseBuilder
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.action.HttpRequestActionBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

object Callback {
  def triggerCallbacks(actionName: String, eventName: String, callbacks: Callback*): ChainBuilder = {
    val builders = callbacks.flatMap(callback => {
      val request = new HttpRequestActionBuilder(callback.buildRequest(eventName))
      if (callback.pause.isDefined) List(request, new PauseBuilder(callback.pause.get, None)) else List(request)
    })
    group(actionName) {
      ChainBuilder(builders.reverse.toList)
    }
  }
}

// Callbacks flow:
// about-to-start
//   > [user fills form and clicks continue - takes 5-15s]
//   > mid-event
//   > [user clicks submit button - takes 1-3s]
//   > about-to-submit
//   > [CCD persists data - takes 0-2s]
//   > submitted
trait Callback {
  val uri: String
  val pause: Option[Duration]

  def buildRequest(eventName: String): HttpRequestBuilder = {
    http(s"${uri} callback")
      .post(url = s"/callback/${eventName}/${uri}")
      .header("Authorization", "Bearer ${user_token}")
      .header("ServiceAuthorization", "Bearer ${service_token}")
      .header("user-id", "${id}")
      .body(RawFileBody("callback-request.json")) // could be more dynamic
      .check(status.is(200))
  }

  protected def range(min: Duration, max: Duration): Duration = {
    ThreadLocalRandom.current.nextLong(min.toMillis, max.toMillis) millis
  }
}

case object AboutToStart extends Callback {
  override val uri: String = "about-to-start"
  override val pause = Some(range(5 seconds, 15 seconds)) // for user to fill form and click continue
}

case object MidEvent extends Callback {
  override val uri: String = "mid-event"
  override val pause: Option[Duration] = Some(range(1 second, 3 seconds)) // for user to click submit button
}

case object AboutToSubmit extends Callback {
  override val uri: String = "about-to-submit"
  override val pause: Option[Duration] = Some(range(0 seconds, 2 seconds)) // for CCD to persist data
}

case object Submitted extends Callback {
  override val uri: String = "submitted"
  override val pause: Option[Duration] = None
}