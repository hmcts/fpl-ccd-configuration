package uk.gov.hmcts.reform.fpl.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import io.gatling.core.structure.ChainBuilder

object Action {
  val action: ChainBuilder = exec(
    http("Test")
      .get(url = "")
  )
}