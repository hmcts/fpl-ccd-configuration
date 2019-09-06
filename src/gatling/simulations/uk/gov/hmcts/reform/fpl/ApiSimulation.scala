package uk.gov.hmcts.reform.fpl

import com.typesafe.config.ConfigFactory

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

import uk.gov.hmcts.reform.fpl.actions.Action.action

class ApiSimulation extends Simulation {
  private val config = ConfigFactory.load()

  private val draftApplication = scenario("Draft EPO application")
      .exec(action)

  setUp(
    draftApplication.inject(rampUsers(config.getInt("params.testUsers")).during(config.getInt("params.testRampUpSecs").seconds))
  ).protocols(
    http.baseUrl(config.getString("baseUrl")).contentTypeHeader("application/json")
  )
}
