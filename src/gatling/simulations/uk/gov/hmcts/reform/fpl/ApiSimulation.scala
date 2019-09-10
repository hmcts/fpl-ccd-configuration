package uk.gov.hmcts.reform.fpl

import io.gatling.core.Predef._
import io.gatling.http.HeaderValues.ApplicationJson
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fpl.actions.ApplicationActions
import uk.gov.hmcts.reform.fpl.actions.auth.{IDAM, S2S}
import uk.gov.hmcts.reform.fpl.config.{SimulationParams, SystemConfig}

class ApiSimulation extends Simulation {
  private val submitApplication = scenario("Submit EPO application")
      .exec(IDAM.registerAndSignIn)
      .exec(S2S.leaseServiceToken)
      .exec(ApplicationActions.create)
      .exec(ApplicationActions.populateOrdersAndDirectionsNeeded)
      .exec(ApplicationActions.populateChildren)
      .exec(ApplicationActions.populateRespondents)
      .exec(ApplicationActions.populateApplicant)
      .exec(ApplicationActions.populateOtherProceedings)
      .exec(ApplicationActions.uploadDocuments)
      .exec(ApplicationActions.submit)
      .exec(IDAM.deleteAccount)

  private val deleteApplication = scenario("Delete EPO application")
    .exec(IDAM.registerAndSignIn)
    .exec(S2S.leaseServiceToken)
    .exec(ApplicationActions.create)
    .exec(ApplicationActions.delete)
    .exec(IDAM.deleteAccount)

  setUp(
    submitApplication.inject(rampUsers(SimulationParams.numberOfUsers).during(SimulationParams.rampUpTimeInSeconds)),
    deleteApplication.inject(rampUsers(SimulationParams.numberOfUsers).during(SimulationParams.rampUpTimeInSeconds))
  ).protocols(
    http.baseUrl(SystemConfig.url).contentTypeHeader(ApplicationJson)
  )
}
