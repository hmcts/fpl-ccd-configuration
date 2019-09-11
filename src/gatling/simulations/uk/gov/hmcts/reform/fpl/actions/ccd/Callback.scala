package uk.gov.hmcts.reform.fpl.actions.ccd

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.action.HttpRequestActionBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

object Callback {
  def triggerCallbacks(actionName: String, eventName: String, callbacks: Callback*): ChainBuilder = {
    val builders = callbacks.map(callback => {
      new HttpRequestActionBuilder(callback.buildRequest(eventName))
    })
    group(actionName) {
      ChainBuilder(builders.toList)
    }
  }
}

// Callbacks flow:
// about-to-start
//   > [user fills form and clicks continue - takes 5-30s]
//   > mid-event
//   > [user clicks submit button - takes 1-3s]
//   > about-to-submit
//   > [CCD persists data - takes 0-2s]
//   > submitted
trait Callback {
  val uri: String

  def buildRequest(eventName: String): HttpRequestBuilder = {
    http(s"${uri} callback")
      .post(url = s"/callback/${eventName}/${uri}")
      .header("Authorization", "Bearer ${user_token}")
      .header("ServiceAuthorization", "Bearer ${service_token}")
      .header("user-id", "0") // could be taken from JWT
      .body(RawFileBody("callback-request.json")) // could be more dynamic
      .check(status.is(200))
  }
}

case object AboutToStart extends Callback {
  override val uri = "about-to-start"
}

case object MidEvent extends Callback {
  override val uri = "mid-event"
}

case object AboutToSubmit extends Callback {
  override val uri = "about-to-submit"
}

case object Submitted extends Callback {
  override val uri = "submitted"
}