package uk.gov.hmcts.reform.fpl.actions

import io.gatling.core.structure.ChainBuilder
import uk.gov.hmcts.reform.fpl.actions.ccd._
import uk.gov.hmcts.reform.fpl.actions.ccd.Callback._

object ApplicationActions {
  val create: ChainBuilder = triggerCallbacks("Create application", "case-initiation", AboutToSubmit, Submitted)
  val populateOrdersAndDirectionsNeeded: ChainBuilder = triggerCallbacks("Populate orders and directions needed", "orders-needed", AboutToSubmit)
  val populateChildren: ChainBuilder = triggerCallbacks("Populate children", "enter-children", AboutToStart, MidEvent, AboutToSubmit)
  val populateRespondents: ChainBuilder = triggerCallbacks("Populate respondents", "enter-respondents", AboutToStart, MidEvent, AboutToSubmit)
  val populateApplicant: ChainBuilder = triggerCallbacks("Populate applicant", "enter-applicant", AboutToStart, MidEvent, AboutToSubmit)
  val populateOtherProceedings: ChainBuilder = triggerCallbacks("Populate other proceedings", "enter-other-proceedings", MidEvent)
  val uploadDocuments: ChainBuilder = triggerCallbacks("Upload documents", "enter-social-work-other", MidEvent)
  val submit: ChainBuilder = triggerCallbacks("Submit application", "case-submission", AboutToStart, MidEvent, AboutToSubmit)

  val delete: ChainBuilder = triggerCallbacks("Delete application", "case-deletion", AboutToSubmit)
}
