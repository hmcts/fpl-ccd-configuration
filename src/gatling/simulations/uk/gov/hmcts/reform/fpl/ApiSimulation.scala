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
      .exec(repeat(2) {
        ApplicationActions.populateChildren
      })
      .exec(repeat(2) {
        ApplicationActions.populateRespondents
      })
      .exec(ApplicationActions.populateApplicant)
      .exec(ApplicationActions.populateOtherProceedings)
      .exec(repeat(2) {
        ApplicationActions.uploadDocuments
      })
      .exec(ApplicationActions.submit)
      .exec(IDAM.deleteAccount)

  private val deleteApplication = scenario("Delete EPO application")
    .exec(IDAM.registerAndSignIn)
    .exec(S2S.leaseServiceToken)
    .exec(ApplicationActions.create)
    .exec(ApplicationActions.delete)
    .exec(IDAM.deleteAccount)

  setUp(
    submitApplication
      .inject(rampUsers(SimulationParams.numberOfUsers).during(SimulationParams.rampUpTimeInSeconds)),
    deleteApplication
      .inject(rampUsers(math.ceil(SimulationParams.numberOfUsers * 0.1).toInt).during(SimulationParams.rampUpTimeInSeconds)) // extra 10% of users delete drafts
  ).protocols(
    http
      .baseUrl(SystemConfig.url)
      .contentTypeHeader(ApplicationJson)
      .silentUri(s"${SystemConfig.idamUrl}/.*|${SystemConfig.s2sUrl}/.*")
  )
}
