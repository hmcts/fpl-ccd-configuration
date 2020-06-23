package uk.gov.hmcts.reform.fpl;


import com.google.common.base.CaseFormat;
import de.cronn.reflection.util.TypedPropertyGetter;
import org.apache.catalina.User;
import uk.gov.hmcts.ccd.sdk.types.BaseCCDConfig;
import uk.gov.hmcts.ccd.sdk.types.DisplayContext;
import uk.gov.hmcts.ccd.sdk.types.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.types.FieldCollection;
import uk.gov.hmcts.ccd.sdk.types.Webhook;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.*;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.types.DisplayContext.*;
import static uk.gov.hmcts.reform.fpl.enums.State.*;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.*;

// Found and invoked by the config generator.
// The CaseData type parameter tells the generator which class represents your case model.
public class CCDConfig extends BaseCCDConfig<CaseData, State, UserRole> {

    protected String environment() {
        return "production";
    }

    // Builds the URL for a webhook based on the event.
    protected String webhookConvention(Webhook webhook, String eventId) {
        eventId = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, eventId);
        String path = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, webhook.toString());
        return "${CCD_DEF_CASE_SERVICE_BASE_URL}/callback/" + eventId + "/" + path;
    }

    @Override
    public void configure() {
        caseType("CARE_SUPERVISION_EPO");
        setEnvironment(environment());
        setWebhookConvention(this::webhookConvention);

        // Describe the hierarchy of which roles go together.
        role(CCD_SOLICITOR, CCD_LASOLICITOR).has(LOCAL_AUTHORITY);
        role(JUDICIARY, GATEKEEPER).has(HMCTS_ADMIN);
        role(SYSTEM_UPDATE, BULK_SCAN, BULK_SCAN_SYSTEM_UPDATE).setApiOnly();

        // Disable AuthorisationCaseField generation for these roles.
        // TODO: complete configuration of permissions for these roles.
        role(CCD_SOLICITOR, CCD_LASOLICITOR).noCaseEventToField();

        // Events
        buildUniversalEvents();
        buildOpen();
        buildMultiStateEvents();
        buildSubmittedEvents();
        buildPrepareForHearing();
        buildGatekeepingEvents();
        buildClosedEvents();
        buildTransitions();

        // UI tabs and inputs.
        buildTabs();
        buildWorkBasketResultFields();
        buildWorkBasketInputFields();

        // Restrict specific fields for Cafcass and Local Authority.
        field("dateSubmitted").blacklist("R", LOCAL_AUTHORITY);
        field("evidenceHandled").blacklist(CAFCASS, LOCAL_AUTHORITY);
    }

    private void buildClosedEvents() {
        event("otherAllocationDecision-CLOSED")
            .forState(CLOSED)
            .name("Allocation decision")
            .description("Entering other proceedings and allocation proposals")
            .showSummary()
            .aboutToStartWebhook("allocation-decision", 1, 2, 3, 4, 5)
            .aboutToSubmitWebhook()
            .fields()
            .field(CaseData::getAllocationDecision, Mandatory, true);


        event("createOrder-CLOSED")
            .forState(CLOSED)
            .name("Create an order")
            .description("Create a C21 order")
            .displayOrder(3)
            .showSummary()
            .allWebhooks("create-order")
            .showSummaryChangeOption()
            .fields()
                .page("OrderDateOfIssue").midEventWebhook("validate-order/date-of-issue")
                    .complex(CaseData::getOrderTypeAndDocument)
                        .readonly(OrderTypeAndDocument::getType)
                        .field(OrderTypeAndDocument::getSubtype, Mandatory, "document=\"DO_NOT_SHOW\"")
                        .readonly(OrderTypeAndDocument::getDocument, "document=\"DO_NOT_SHOW\"")
                        .done()
                    .readonly("dateOfIssue_label")
                    .field(CaseData::getDateOfIssue).mandatory().showSummary().done()
                    .readonly("pageShow", "orderTypeAndDocument=\"DO_NOT_SHOW\"")
                .page("OrderAppliesToAllChildren").showCondition("pageShow=\"Yes\"").midEventWebhook("create-order/populate-selector")
                    .field(CaseData::getOrderAppliesToAllChildren).mandatory().showSummary().done()
                .page("ChildrenSelection")
                    .showCondition("orderAppliesToAllChildren=\"No\"")
                    .midEventWebhook("validate-order/child-selector")
                    .label("children_label", "")
                     .complex(CaseData::getChildSelector).done()
                .page("OrderTitleAndDetails")
                    .complex(CaseData::getOrder)
                        .optional(GeneratedOrder::getTitle)
                        .mandatory(GeneratedOrder::getDetails)
                        .readonly(GeneratedOrder::getDate, "document=\"DO_NOT_SHOW\"")
                    .done()
                .page("JudgeInformation")
                    .midEventWebhook("create-order/generate-document")
                    .complex(CaseData::getJudgeAndLegalAdvisor)
                        .readonly(JudgeAndLegalAdvisor::getAllocatedJudgeLabel)
                        .mandatory(JudgeAndLegalAdvisor::getUseAllocatedJudge)
                        .readonly("judgeSubHeading")
                        .mandatory(JudgeAndLegalAdvisor::getJudgeTitle)
                        .mandatory(JudgeAndLegalAdvisor::getOtherTitle)
                        .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                        .mandatory(JudgeAndLegalAdvisor::getJudgeFullName)
                        .mandatory(JudgeAndLegalAdvisor::getJudgeEmailAddress)
                        .optional(JudgeAndLegalAdvisor::getLegalAdvisorName);
    }

    private void buildWorkBasketResultFields() {
        workBasketResultFields()
            .field(CaseData::getCaseName, "Case name")
            .field(CaseData::getFamilyManCaseNumber, "FamilyMan case number")
            .stateField()
            .field(CaseData::getCaseLocalAuthority, "Local authority")
            .field("dateAndTimeSubmitted", "Date submitted", "1:DESC") //TODO make it nicer
            .field("evidenceHandled", "Supplementary evidence handled");
    }

    private void buildWorkBasketInputFields() {
        workBasketInputFields()
            .field(CaseData::getCaseLocalAuthority, "Local authority")
            .field(CaseData::getCaseName, "Case name")
            .field(CaseData::getFamilyManCaseNumber, "FamilyMan case number")
            .caseReferenceField()
            .field(CaseData::getDateSubmitted, "Date submitted")
            .field("evidenceHandled", "Supplementary evidence handled");
    }

    private void buildTabs() {
        tab("CaseHistory", "History")
            .field("caseHistory")
            .field("familyManCaseNumber", "familyManCaseNumber = \"DO_NOT_SHOW\"")
            .field("caseName", "familyManCaseNumber = \"DO_NOT_SHOW\"");

        tab("HearingTab", "Hearings")
            .restrictedField(CaseData::getHearingDetails).exclude(CAFCASS)
            .restrictedField(CaseData::getHearing).exclude(LOCAL_AUTHORITY);

        tab("DraftOrdersTab", "Draft orders")
            .exclude(LOCAL_AUTHORITY, HMCTS_ADMIN, GATEKEEPER, JUDICIARY)
            .showCondition("standardDirectionOrder.orderStatus!=\"SEALED\" OR caseManagementOrder!=\"\" OR sharedDraftCMODocument!=\"\" OR cmoToAction!=\"\"")
            .field(CaseData::getStandardDirectionOrder, "standardDirectionOrder.orderStatus!=\"SEALED\"")
            .field(CaseData::getSharedDraftCMODocument)
            .restrictedField(CaseData::getCaseManagementOrderForJudiciary).exclude(CAFCASS)
            .restrictedField(CaseData::getCaseManagementOrder).exclude(CAFCASS);

        tab("OrdersTab", "Orders")
            .exclude(LOCAL_AUTHORITY)
            .restrictedField(CaseData::getServedCaseManagementOrders).exclude(CAFCASS)
            .field(CaseData::getStandardDirectionOrder, "standardDirectionOrder.orderStatus=\"SEALED\"")
            .field(CaseData::getOrders)
            .restrictedField(CaseData::getOrderCollection).exclude(CAFCASS);

        tab("CasePeopleTab", "People in the case")
            .exclude(LOCAL_AUTHORITY)
            .field(CaseData::getAllocatedJudge)
            .restrictedField(CaseData::getChildren1).exclude(GATEKEEPER) //XXX for sure?
            .restrictedField(CaseData::getRespondents1).exclude(GATEKEEPER) //XXX for sure?
            .field(CaseData::getApplicants)
            .field(CaseData::getSolicitor)
            .restrictedField(CaseData::getOthers).exclude(GATEKEEPER) //XXX for sure?
            .restrictedField(CaseData::getRepresentatives).exclude(CAFCASS, LOCAL_AUTHORITY);

        tab("LegalBasisTab", "Legal basis")
            .exclude(LOCAL_AUTHORITY)
            .field(CaseData::getStatementOfService)
            .field(CaseData::getGroundsForEPO)
            .field(CaseData::getGrounds)
            .field(CaseData::getRisks)
            .field(CaseData::getFactorsParenting)
            .field(CaseData::getInternationalElement)
            .restrictedField(CaseData::getProceeding).exclude(GATEKEEPER) //XXX for sure?
            .field(CaseData::getAllocationDecision)
            .field(CaseData::getAllocationProposal)
            .restrictedField(CaseData::getHearingPreferences).exclude(GATEKEEPER); //XXX for sure?

        tab("DocumentsTab", "Documents")
            .exclude(LOCAL_AUTHORITY)
            .field(CaseData::getSocialWorkChronologyDocument)
            .field(CaseData::getSocialWorkStatementDocument)
            .field(CaseData::getSocialWorkAssessmentDocument)
            .field(CaseData::getSocialWorkCarePlanDocument)
            .field("standardDirectionsDocument")
            .field(CaseData::getSocialWorkEvidenceTemplateDocument)
            .field(CaseData::getThresholdDocument)
            .field(CaseData::getChecklistDocument)
            .field("courtBundle")
            .field(CaseData::getOtherSocialWorkDocuments)
            .field("otherCourtAdminDocuments")
            .field("submittedForm")
            .restrictedField(CaseData::getNoticeOfProceedingsBundle).exclude(CAFCASS)
            .field(CaseData::getC2DocumentBundle)
            .restrictedField("scannedDocuments").exclude(CAFCASS);

        tab("Confidential", "Confidential")
            .exclude(CAFCASS, LOCAL_AUTHORITY)
            .field(CaseData::getConfidentialChildren)
            .field(CaseData::getConfidentialRespondents)
            .field(CaseData::getConfidentialOthers);

        tab("PlacementTab", "Placement")
            .exclude(CAFCASS, LOCAL_AUTHORITY, HMCTS_ADMIN, GATEKEEPER, JUDICIARY)
            .field("placements")
            .field(CaseData::getPlacements)
            .field("placementsWithoutPlacementOrder");

        tab("SentDocumentsTab", "Documents sent to parties")
            .exclude(CAFCASS, LOCAL_AUTHORITY)
            .field("documentsSentToParties");

        tab("PaymentsTab", "Payment History")
            .restrictedField("paymentHistory").exclude(CAFCASS, LOCAL_AUTHORITY);

        tab("Notes", "Notes")
            .restrictedField(CaseData::getCaseNotes).exclude(CAFCASS, LOCAL_AUTHORITY);

        tab("ExpertReportsTab", "Expert Reports")
            .exclude(CAFCASS, LOCAL_AUTHORITY)
            .field("expertReport");

        tab("OverviewTab", "Overview")
            .exclude(LOCAL_AUTHORITY, GATEKEEPER, CAFCASS
            )
            .field("returnApplication")
            .field("caseCompletionDate")
            .field("caseExtensionReasonList")
            .field("extensionComments", "extensionComments!=\"\"")
            .field("closeCaseTabField");

    }

    private void buildUniversalEvents() {
        event("internal-change-SEND_DOCUMENT")
            .forAllStates()
            .name("Send document")
            .endButtonLabel("")
            .aboutToSubmitWebhook("send-document")
            .explicitGrants()
            .grant("CRU", SYSTEM_UPDATE);

        event("addFamilyManCaseNumber")
            .forAllStates()
            .name("Add case number")
            .explicitGrants()
            .grant("CRU", HMCTS_ADMIN)
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .aboutToSubmitWebhook("add-case-number")
            .submittedWebhook()
            .fields()
            .optional(CaseData::getFamilyManCaseNumber);

        event("handleSupplementaryEvidence")
            .forAllStates()
            .explicitGrants()
            .grantHistoryOnly(CAFCASS)
            .grant("CRU", HMCTS_ADMIN)
            .grant("R", LOCAL_AUTHORITY, JUDICIARY, GATEKEEPER)
            .name("Handle supplementary evidence")
            .showEventNotes()
            .fields()
            .pageLabel("Bulk Scanning ")
            .field("evidenceHandled").mandatory();

        event("attachScannedDocs")
            .forAllStates()
            .explicitGrants()
            .grant("CRUD", BULK_SCAN, BULK_SCAN_SYSTEM_UPDATE)
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .endButtonLabel("")
            .name("Attach scanned docs")
            .fields()
            .page(1)
                .pageLabel("BulkScanning")
                .field("scannedDocuments").optional().done()
            .page(2)
                .pageLabel("BulkScanning")
                .field("evidenceHandled").mandatory()
                    .blacklist("D", BULK_SCAN, BULK_SCAN_SYSTEM_UPDATE);

        event("allocatedJudge")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
            .name("Allocated Judge")
            .description("Add allocated judge to a case")
            .submittedWebhook("allocated-judge")
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .grant("CRU", JUDICIARY, HMCTS_ADMIN, GATEKEEPER)
            .grant("R", CAFCASS)
            .fields()
            .page("AllocatedJudge")
                .field(CaseData::getAllocatedJudge).complex()
                    .mandatory(Judge::getJudgeTitle)
                    .mandatory(Judge::getOtherTitle)
                    .mandatory(Judge::getJudgeLastName)
                    .mandatory(Judge::getJudgeFullName)
                    .mandatory(Judge::getJudgeEmailAddress);

        event("expertReport")
            .forAllStates()
            .name("Log expert report")
            .description("Expert report event")
            .grant("CRU", JUDICIARY, GATEKEEPER)
            .fields()
            .page(1)
                .field("expertReport")
                    .complex()
                    .mandatory("expertReportList")
                    .mandatory("expertReportDateRequested")
                    .optional("reportApproval")
                    .mandatory("reportApprovalDate", "expertReport.reportApproval=\"Yes\"");

        event("extend26WeekTimeline")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
            .grant("CRU", JUDICIARY)
            .name("Extend 26-week timeline")
            .description("Non standard track indicator event")
            .aboutToStartWebhook("case-extension")
            .aboutToSubmitWebhook("case-extension")
            .fields()
                .page("extend26WeekTimeline")
                    .mandatory("shouldBeCompletedByDate", "shouldBeCompletedByLabel=\"DO NOT SHOW\"")
                    .midEventWebhook("case-extension")
                    .readonly("shouldBeCompletedByLabel")
                    .label("extendByEightWeeksOrOtherLabel", "You can either extend this date by 8 weeks, or enter a different end date.")
                    .mandatory("caseExtensionTimeList")
                    .mandatory("extensionDateOther", "caseExtensionTimeList=\"OtherExtension\"")
                    .mandatory("caseExtensionReasonList")
                    .optional("extensionComments")
                    .mandatory("extensionDateEightWeeks", "shouldBeCompletedByConfirmationLabel=\"DO NOT SHOW\"")
                .page("extend26WeekTimelineConfirmation").midEventWebhook("case-extension").showCondition("caseExtensionTimeList=\"EightWeekExtension\"")
                    .readonly("shouldBeCompletedByConfirmationLabel")
                    .label("caseFurtherExtensionLabel", "You can extend this date again, but you will not be able to revert back to the original date.")
                    .mandatory("caseExtensionTimeConfirmationList")
                    .mandatory("eightWeeksExtensionDateOther", "caseExtensionTimeConfirmationList=\"OtherExtension\"");

        event("addCaseNote")
            .forAllStates()
            .name("Add a case note")
            .grant("CRU", JUDICIARY)
            .aboutToSubmitWebhook("add-note")
            .fields()
            .page("CaseNote")
                .mandatory(CaseData::getCaseNote);

        event("uploadDocuments")
            .forStates(OPEN, SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING, RETURNED)
            .explicitGrants()
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .grant("CRU", CCD_LASOLICITOR, CCD_LABARRISTER)
            .name("Documents")
            .description("Upload documents")
            .displayOrder(14)
            .fields()
                .midEventWebhook()
                .label("uploadDocuments_paragraph_1", "You must upload these documents if possible. Give the reason and date you expect to provide it if you don’t have a document yet.")
                .optional(CaseData::getSocialWorkChronologyDocument)
                .optional(CaseData::getSocialWorkStatementDocument)
                .optional(CaseData::getSocialWorkAssessmentDocument)
                .optional(CaseData::getSocialWorkCarePlanDocument)
                .optional(CaseData::getSocialWorkEvidenceTemplateDocument)
                .optional(CaseData::getThresholdDocument)
                .optional(CaseData::getChecklistDocument)
                .field("[STATE]", ReadOnly, "courtBundle = \"DO_NOT_SHOW\"")
                .field("courtBundle", Optional, "[STATE] != \"Open\"", "CourtBundle", null, "8. Court bundle")
                .label("documents_socialWorkOther_border_top", "-------------------------------------------------------------------------------------------------------------")
                .list(CaseData::getOtherSocialWorkDocuments).optional().done()
                .label("documents_socialWorkOther_border_bottom", "-------------------------------------------------------------------------------------------------------------");

        event("uploadDocuments-CLOSED")
            .forState(CLOSED)
            .name("Documents")
            .description("Upload documents")
            .displayOrder(9)
            .showEventNotes()
            .fields()
                .optional(CaseData::getOtherSocialWorkDocuments)
                .optional("otherCourtAdminDocuments");
    }

    private EventBuilder<CaseData, UserRole, State> buildCreateOrderEvent() {
        return event("createOrder")
                .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
                .explicitGrants()
                .grant("CRU", HMCTS_ADMIN, JUDICIARY, GATEKEEPER)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .name("Create an order")
                .showSummary()
                .allWebhooks("create-order")
                .fields()
                .page("OrderTypeAndDocument")
                    .midEventWebhook("create-order/add-final-order-flags")
                    .complex(CaseData::getOrderTypeAndDocument)
                        .mandatory(OrderTypeAndDocument::getType)
                        .mandatory(OrderTypeAndDocument::getSubtype)
                        .readonly(OrderTypeAndDocument::getDocument, "document=\"DO_NOT_SHOW\"")
                    .done()
                    .field("pageShow", ReadOnly, "orderTypeAndDocument=\"DO_NOT_SHOW\"")
                    .readonly("showFinalOrderSingleChildPage", "orderTypeAndDocument=\"DO_NOT_SHOW\"")
                    .readonly("remainingChildIndex", "orderTypeAndDocument=\"DO_NOT_SHOW\"")
                .page("OrderDateOfIssue")
                    .midEventWebhook("validate-order/date-of-issue")
                    .field("dateOfIssue_label").readOnly().label("dateOfIssue_label").done()
                    .field(CaseData::getDateOfIssue).mandatory().showSummary().done()
                .page("OrderAppliesToAllChildren")
                    .showCondition("pageShow=\"Yes\" AND showFinalOrderSingleChildPage!=\"Yes\"")
                    .midEventWebhook("create-order/populate-selector")
                    .field(CaseData::getOrderAppliesToAllChildren).mandatory().showSummary().done()
                .page("ChildrenSelection")
                    .showCondition("orderAppliesToAllChildren=\"No\" AND remainingChildIndex=\"\"")
                    .midEventWebhook("validate-order/child-selector")
                    .label("children_label", "")
                    .complex(CaseData::getChildSelector).done()
                .page("RemainingChild")
                    .showCondition("showFinalOrderSingleChildPage=\"Yes\"")
                    .label("remainingChild_label", "")
                    .field("remainingChild").readOnly().showSummary().done()
                    .label("otherFinalOrderChildren_label", "")
                    .readonly("otherFinalOrderChildren")
                .page("OrderTitleAndDetails")
                    .showCondition("orderTypeAndDocument.type=\"BLANK_ORDER\"")
                    .complex(CaseData::getOrder)
                        .optional(GeneratedOrder::getTitle)
                        .mandatory(GeneratedOrder::getDetails)
                        .readonly(GeneratedOrder::getDate, "document=\"DO_NOT_SHOW\"")
                    .done()
                .page("OrderMonths")
                    .showCondition("orderTypeAndDocument.type=\"SUPERVISION_ORDER\" AND orderTypeAndDocument.subtype=\"FINAL\"")
                    .field(CaseData::getOrderMonths).mandatory().showSummary().done()
                .page("InterimEndDate")
                    .showCondition("orderTypeAndDocument.subtype=\"INTERIM\"")
                    .midEventWebhook("validate-order/interim-end-date")
                    .field(CaseData::getInterimEndDate).mandatory().showSummary().done()
                .page("EPOChildren")
                    .showCondition("orderTypeAndDocument.type=\"EMERGENCY_PROTECTION_ORDER\"")
                    .complex(CaseData::getEpoChildren)
                        .mandatory(EPOChildren::getDescriptionNeeded)
                        .optional(EPOChildren::getDescription)
                    .done()
                .page("EPOType")
                    .showCondition("orderTypeAndDocument.type=\"EMERGENCY_PROTECTION_ORDER\"")
                    .midEventWebhook("validate-order/address")
                    .field(CaseData::getEpoType).mandatory().showSummary().done()
                    .field(CaseData::getEpoRemovalAddress).optional().showCondition("epoType=\"PREVENT_REMOVAL\"").showSummary().done()
                .page("EPOPhrase")
                    .showCondition("orderTypeAndDocument.type=\"EMERGENCY_PROTECTION_ORDER\"")
                    .field(CaseData::getEpoPhrase).mandatory().showSummary().done()
                .page("EPOEndTime")
                    .showCondition("orderTypeAndDocument.type=\"EMERGENCY_PROTECTION_ORDER\"")
                    .midEventWebhook("validate-order/epo-end-date")
                    .field(CaseData::getEpoEndDate).mandatory().showSummary().done()
                .page("JudgeInformation")
                    .midEventWebhook("create-order/generate-document")
                    .complex(CaseData::getJudgeAndLegalAdvisor)
                        .mandatory(JudgeAndLegalAdvisor::getJudgeTitle)
                        .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                        .optional(JudgeAndLegalAdvisor::getJudgeFullName)
                        .optional(JudgeAndLegalAdvisor::getLegalAdvisorName)
                    .done()
                .page("FurtherDirections")
                    .showCondition("orderTypeAndDocument.type!=\"BLANK_ORDER\"")
                    .midEventWebhook("create-order/generate-document")
                    .field(CaseData::getOrderFurtherDirections).mandatory().showSummary().done()
                .page("CloseCase")
            //TODO show conditions on builder shouldn't overwrite the page show condition, it leads to unexpected behaviour
                    .showCondition("showCloseCaseFromOrderPage=\"YES\"")
                    .field(CaseData::getCloseCaseFromOrder).mandatory().showSummary().done()
                    .field("close_case_label").readOnly().showCondition("closeCaseFromOrder=\"Yes\"").done()
                    .field("showCloseCaseFromOrderPage").readOnly().showCondition("closeCaseFromOrder=\"DO_NOT_SHOW\"").done()
                .done();
    }

    private void buildTransitions() {
        event("submitApplication")
                .forStateTransition(List.of(OPEN, RETURNED), SUBMITTED)
                .name("Submit application")
                .description("Check and send application")
                .explicitGrants()
                .grantHistoryOnly(LOCAL_AUTHORITY, HMCTS_ADMIN, GATEKEEPER, JUDICIARY, CAFCASS)
                .grant("CRU", CCD_LASOLICITOR)
                .endButtonLabel("Submit")
                .allWebhooks("case-submission")
                .retries(1,2,3,4,5)
                .fields()
                    .midEventWebhook("case-submission")
                    .label("downloadApplicationForReviewHintLabel", "**Download application** <br>Use this link to download and check the application before <br>sending:")
                    .readonly("draftApplicationDocument")
                    .field("submissionConsentLabel").readOnly().type("Text").label(" ").done()
                    .field("submissionConsent").mandatory().type("MultiSelectList").fieldTypeParameter("Consent").done()
                    .field("displayAmountToPay").readOnly().showCondition("submissionConsentLabel=\"DO_NOT_SHOW\"").done()
                    .field("amountToPay").readOnly().showCondition("displayAmountToPay=\"Yes\"");

        event("populateSDO")
                .forStateTransition(ANY, GATEKEEPING)
                .name("Populate standard directions")
                .displayOrder(14) // TODO - necessary?
                .explicitGrants()
                .grant("CU", UserRole.SYSTEM_UPDATE)
                .fields()
                    .optional(CaseData::getAllParties)
                    .optional(CaseData::getLocalAuthorityDirections)
                    .optional(CaseData::getRespondentDirections)
                    .optional(CaseData::getCafcassDirections)
                    .optional(CaseData::getOtherPartiesDirections)
                    .optional(CaseData::getCourtDirections);

        event("returnApplication")
            .forStateTransition(List.of(SUBMITTED, GATEKEEPING), RETURNED)
            .explicitGrants()
            .grant("CRU", HMCTS_ADMIN)
            .grantHistoryOnly(CCD_LASOLICITOR)
            .name("Return application")
            .description("Return an application to the LA")
            .displayOrder(19)
            .retries(1,2,3,4,5)
            .allWebhooks()
            .showEventNotes()
            .fields()
                .complex(CaseData::getReturnApplication, ReturnApplication.class, false)
                    .mandatory(ReturnApplication::getReason)
                    .mandatory(ReturnApplication::getNote)
                    .readonly(ReturnApplication::getSubmittedDate, "note = \"DO NOT SHOW\"")
                    .readonly(ReturnApplication::getReturnedDate, "note = \"DO NOT SHOW\"")
                    .readonly(ReturnApplication::getDocument, "note = \"DO NOT SHOW\"");



        event("deleteApplication")
            .forStateTransition(List.of(OPEN, RETURNED), DELETED)
            .displayOrder(18) // TODO - necessary?
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .grant("CRU", CCD_LASOLICITOR)
            .grantHistoryOnly(LOCAL_AUTHORITY) //XXX: why I need to add the grant only? was it not properly configured in past?
                                               //did I lose some magic?
            .name("Delete an application")
            .aboutToSubmitWebhook("case-deletion")
            .endButtonLabel("Delete application")
            .fields()
                .field("deletionConsent", Mandatory, null, "MultiSelectList", "DeletionConsent", " ");

        event("internal-changeState-Gatekeeping->PREPARE_FOR_HEARING")
            .forStateTransition(GATEKEEPING, PREPARE_FOR_HEARING)
            .name("-")
            .endButtonLabel("")
            .explicitGrants()
            .grant("C", SYSTEM_UPDATE);

        event("populateCase-Submitted")
            .forStateTransition(ANY, SUBMITTED)
            .description("")
            .endButtonLabel("")
            .name("Populate - submitted");

        event("populateCase-Gatekeeping")
            .forStateTransition(ANY, GATEKEEPING)
            .description("")
            .endButtonLabel("")
            .name("Populate - Gatekeeping");

        event("populateCase-PREPARE_FOR_HEARING")
            .forStateTransition(ANY, PREPARE_FOR_HEARING)
            .description("")
            .endButtonLabel("")
            .name("Populate - Prepare for hearing");


        event("closeCase")
            .forStateTransition(List.of(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING), CLOSED)
            .name("Close the case")
            .explicitGrants()
//            .grant("CRU", HMCTS_ADMIN) TOGGLED off
            .displayOrder(18)
            .aboutToStartWebhook()
            .aboutToSubmitWebhook()
            .showEventNotes()
            .endButtonLabel("Submit")
                .fields()
                    .page("1").midEventWebhook()
                        .readonly("close_case_label")
                        .complex(CaseData::getCloseCase, CloseCase.class, false)
                            .mandatory(CloseCase::getDate)
                            .readonly(CloseCase::getShowFullReason, "closeCase.date=\"DO_NOT_SHOW\"")
                            .mandatory(CloseCase::getFullReason, "closeCase.showFullReason!=\"NO\"")
                            .mandatory(CloseCase::getPartialReason, "closeCase.showFullReason!=\"YES\"")
                            .mandatory(CloseCase::getDetails, "closeCase.fullReason=\"OTHER\" OR closeCase.partialReason=\"OTHER\"");
    }

    private void buildGatekeepingEvents() {
        grant(GATEKEEPING, "CRU", GATEKEEPER);

        event("otherAllocationDecision")
            .forState(GATEKEEPING)
            .name("Allocation decision")
            .description("Entering other proceedings and allocation proposals")
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .showSummary()
            .aboutToStartWebhook("allocation-decision", 1, 2, 3, 4, 5)
            .aboutToSubmitWebhook()
            .fields()
                .field(CaseData::getAllocationDecision, Mandatory, true);

        event("draftSDO")
                .forState(GATEKEEPING)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .name("Draft standard directions")
                .allWebhooks("draft-standard-directions")
                .fields()
                    .page("SdoDateOfIssue")
                        .midEventWebhook("draft-standard-directions/date-of-issue")
                        .field("dateOfIssue_label").readOnly().done()
                        .mandatory(CaseData::getDateOfIssue)
                    .page("judgeAndLegalAdvisor")
                        .complex(CaseData::getJudgeAndLegalAdvisor, false)
                            .readonly(JudgeAndLegalAdvisor::getAllocatedJudgeLabel)
                            .mandatory(JudgeAndLegalAdvisor::getUseAllocatedJudge)
                            .mandatory("judgeSubHeading")
                            .mandatory(JudgeAndLegalAdvisor::getJudgeTitle)
                            .mandatory(JudgeAndLegalAdvisor::getOtherTitle)
                            .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                            .mandatory(JudgeAndLegalAdvisor::getJudgeFullName)
                            .mandatory(JudgeAndLegalAdvisor::getJudgeEmailAddress)
                            .optional(JudgeAndLegalAdvisor::getLegalAdvisorName)
                        .done()
                    .page("allPartiesDirections")
                        .field("allPartiesHearingDate", ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getAllParties).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getAllPartiesCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("localAuthorityDirections")
                        .field("localAuthorityDirectionsHearingDate", ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getLocalAuthorityDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getLocalAuthorityDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("parentsAndRespondentsDirections")
                        .field("respondentDirectionsHearingDate", ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getRespondentDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getRespondentDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("cafcassDirections")
                        .field("cafcassDirectionsHearingDate", ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getCafcassDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getCafcassDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("otherPartiesDirections")
                        .field("otherPartiesDirectionsHearingDate", ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getOtherPartiesDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getOtherPartiesDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("courtDirections")
                        .midEventWebhook("draft-standard-directions")
                        .field("courtDirectionsHearingDate", ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getCourtDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getCourtDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("documentReview")
                        .field(CaseData::getStandardDirectionOrder).showSummary(false)
                        .complex()
                        .field(Order::getOrderDoc).readOnly().label("Check the order").done()
                        .mandatory(Order::getOrderStatus).done();

        event("uploadDocumentsAfterGatekeeping")
                .forState(GATEKEEPING)
                .name("Documents")
                .description("Only here for backwards compatibility with case history")
                .explicitGrants()
                .grant("R", LOCAL_AUTHORITY, CCD_LASOLICITOR);

        event("notifyGatekeeper")
            .forState(GATEKEEPING)
            .grant("CRU", HMCTS_ADMIN)
            .grantHistoryOnly(JUDICIARY)
            .name("Notify gatekeeper")
            .description("Send email to gatekeeper")
            .aboutToStartWebhook("notify-gatekeeper")
            .submittedWebhook()
            .fields()
                .label("gatekeeperHintLabel", "You must add at least 1 gatekeeper")
                .label("gateKeeperLabel", "Let the gatekeeper know there's a new case")
                .mandatory(CaseData::getGatekeeperEmails);

    }

    private void renderSDODirectionsCustom(FieldCollection.FieldCollectionBuilder<Direction,?> f)  {
        f.mandatory(Direction::getDirectionType)
                .optional(Direction::getDirectionText)
                .optional(Direction::getDateToBeCompletedBy);
    }

    private void renderSDODirection(FieldCollection.FieldCollectionBuilder<Direction,?> f) {
        f.readonly(Direction::getReadOnly)
                .readonly(Direction::getDirectionRemovable)
                .readonly(Direction::getDirectionType)
                .mandatory(Direction::getDirectionNeeded, "{{FIELD_NAME}}.directionRemovable=\"Yes\"")
                .optional(Direction::getDirectionText, "{{FIELD_NAME}}.readOnly!=\"Yes\" AND {{FIELD_NAME}}.directionNeeded!=\"No\"")
                .optional(Direction::getDateToBeCompletedBy);
    }

    private void buildStandardDirections() {
        event("uploadStandardDirections")
                .forStates(SUBMITTED, GATEKEEPING)
                .name("Documents")
                .description("Upload standard directions")
                .explicitGrants()
                .grant("CRU", HMCTS_ADMIN)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .label("standardDirectionsLabel", "Upload standard directions and other relevant documents, for example the C6 Notice of Proceedings or C9 statement of service.")
                    .label("standardDirectionsTitle", "## 1. Standard directions")
                    .field("standardDirectionsDocument", Optional, null, "Document", null, "Upload a file")
                    .field("otherCourtAdminDocuments", Optional, null, "Collection", "CourtAdminDocument", "Other documents");
    }

    private void buildSubmittedEvents() {
        grant(SUBMITTED, "CRU", HMCTS_ADMIN);

        buildHearingBookingDetails();


        event("sendToGatekeeper")
                .forState(SUBMITTED)
                .name("Send to gatekeeper")
                .description("Send email to gatekeeper")
                .explicitGrants()
                .grant("CRU", HMCTS_ADMIN)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .aboutToStartWebhook("notify-gatekeeper")
                .submittedWebhook()
                .fields()
                    .label("gatekeeperHintLabel", "You must add at least 1 gatekeeper")
                    .label("gateKeeperLabel", "Let the gatekeeper know there's a new case")
                    .mandatory(CaseData::getGatekeeperEmails);

        buildStatementOfService();


        event("uploadDocumentsAfterSubmission")
                .forState(SUBMITTED)
                .explicitGrants()
                .grant("R", LOCAL_AUTHORITY, CCD_LASOLICITOR)
                .name("Documents")
                .description("Only here for backwards compatibility with case history");


    }

    private void buildStatementOfService() {
        event("addStatementOfService")
                .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING, CLOSED)
                .explicitGrants()
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .grant("CRU", CCD_LASOLICITOR)
                .name("Add statement of service (c9)")
                .description("Add statement of service")
                .showSummary()
                .aboutToStartWebhook("statement-of-service")
                .fields()
                    .label("c9Declaration", "If you send documents to a party's solicitor or a children's guardian, give their details")
                    .list(CaseData::getStatementOfService).mandatory().showSummary(true).done()
                    .field("serviceDeclarationLabel", ReadOnly, null, "Text", null, "Declaration" )
                    .field("serviceConsent", Mandatory, null, "MultiSelectList", "Consent", " ");
    }

    private void buildPrepareForHearing() {
        prefix(PREPARE_FOR_HEARING, "-");

        event("uploadOtherCourtAdminDocuments-PREPARE_FOR_HEARING")
            .forState(PREPARE_FOR_HEARING)
            .name("Documents")
            .description("Upload documents")
            .grant("CRU", HMCTS_ADMIN)
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .fields()
            .field("otherCourtAdminDocuments", Optional, null, "Collection", "CourtAdminDocument", "Other documents");

        String notSendToJudge = "cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"";
        event("draftCMO")
                .forState(PREPARE_FOR_HEARING)
                .explicitGrants()
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .grant("CRU", CCD_LASOLICITOR)
                .name("Draft CMO")
                .endButtonLabel("")
                .description("Draft Case Management Order")
                .displayOrder(1)
                .allWebhooks("draft-cmo")
                .fields()
                .page("cmoDraftInReview")
                    .showCondition("cmoToAction!=\"\" AND cmoToAction.status=\"SEND_TO_JUDGE\"")
                    .field("cmoDraftInReviewHeading").readOnly().done()
                    .field("cmoDraftInReviewHint").readOnly().done()
                    .field("cmoToAction").readOnly().showCondition("cmoDraftInReviewHeading=\"DO NOT SHOW\"").done()
                .page("hearingDate")
                    .showCondition(notSendToJudge)
                    .field("cmoHearingDateList").mandatory().type("DynamicList").label("Which hearing is this order for?").done()
                .page("allPartiesDirections")
                    .showCondition(notSendToJudge)
                    .label("allPartiesLabelCMO", "## For all parties")
                .field("allPartiesPrecedentLabelCMO").readOnly().immutable().fieldTypeParameter("Direction").label("Add completed directions from the precedent library or your own template.").done()
                .list("allPartiesCustomCMO").complex(Direction.class, this::renderDirection)
                .page("localAuthorityDirections")
                    .showCondition(notSendToJudge)
                     .label("localAuthorityDirectionsLabelCMO", "## For the local authority")
                     .list("localAuthorityDirectionsCustomCMO").complex(Direction.class, this::renderDirection)
                .page(2)
                    .showCondition(notSendToJudge)
                     .label("respondentsDirectionLabelCMO", "## For the parents or respondents")
                     .field("respondents_label", ReadOnly, null, "TextArea", null, " ")
                     .list("respondentDirectionsCustomCMO").complex(Direction.class)
                        .mandatory(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getParentsAndRespondentsAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("cafcassDirections")
                    .showCondition(notSendToJudge)
                     .label("cafcassDirectionsLabelCMO", "## For Cafcass")
                     .list("cafcassDirectionsCustomCMO").complex(Direction.class, this::renderDirection)
                .page(3)
                    .showCondition(notSendToJudge)
                     .label("otherPartiesDirectionLabelCMO", "## For other parties")
                     .field("others_label", ReadOnly, null, "TextArea", null, " ")
                     .list("otherPartiesDirectionsCustomCMO").complex(Direction.class)
                        .mandatory(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getOtherPartiesAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("courtDirections")
                    .showCondition(notSendToJudge)
                     .label("courtDirectionsLabelCMO", "## For the court")
                     .list("courtDirectionsCustomCMO").complex(Direction.class, this::renderDirection)
                .page(5)
                    .showCondition(notSendToJudge)
                     .label("orderBasisLabel", "## Basis of order")
                     .label("addRecitalLabel", "### Add recital")
                     .list("recitals").optional().fieldTypeParameter("Recitals").label("Recitals").done()
                .page("schedule")
                    .showCondition(notSendToJudge) .field("schedule", Mandatory, null, "Schedule", null, "Schedule")
                    .midEventWebhook()
                    .field("schedule", Mandatory, null, "Schedule", null, "Schedule")
            .page("documentReview")
                .showCondition(notSendToJudge)
                .field(CaseData::getCaseManagementOrder).complex()
                    .field(CaseManagementOrder::getOrderDoc).label("Check the order").readOnly().done()
                    .readonly(CaseManagementOrder::getHearingDate, "orderDoc=\"DO_NOT_SHOW\"")
                    .field(CaseManagementOrder::getStatus).label("Is this ready to be sent to the judge?").mandatory();

        String sendToJudge = "cmoToAction.status=\"SEND_TO_JUDGE\"";
        event("actionCMO")
            .forState(PREPARE_FOR_HEARING)
            .grant("CRU", JUDICIARY)
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .name("Action CMO")
            .description("Allows Judge user access to action a case management order")
            .displayOrder(1)
            .allWebhooks("action-cmo")
            .fields()
                .page("actionCMOPlaceholder")
                    .showCondition(notSendToJudge)
                    .field("actionCMOPlaceholderHeading").readOnly().blacklist("CU", JUDICIARY).done()
                    .field("actionCMOPlaceholderHint").readOnly().blacklist("CU", JUDICIARY).done()
                .page("NOT_SHOWN").previousPage()
                    .showCondition("actionCMOPlaceholderHeading=\"DO_NOT_SHOW\"")
                    .field(CaseData::getCaseManagementOrderForJudiciary).optional().complex()
                        .readonly(CaseManagementOrder::getHearingDate)
                        .readonly(CaseManagementOrder::getOrderDoc)
                        .readonly(CaseManagementOrder::getHearingDate, "orderDoc=\"DO_NOT_SHOW\"")
                    .done()
                .page("OrderDateOfIssue")
                    .showCondition(sendToJudge)
                    .midEventWebhook("validate-order/date-of-issue")
                    .field("dateOfIssue_label").readOnly().done()
                    .mandatory(CaseData::getDateOfIssue)
                .page("allPartiesDirections")
                    .showCondition(sendToJudge)
                    .label("allPartiesLabelCMO", "## For all parties")
                    .field("allPartiesPrecedentLabelCMO").readOnly().fieldTypeParameter("Direction").label("Add completed directions from the precedent library or your own template.").immutable().done()
                    .list("allPartiesCustomCMO").complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("localAuthorityDirections")
                    .showCondition(sendToJudge)
                     .label("localAuthorityDirectionsLabelCMO", "## For the local authority")
                     .list("localAuthorityDirectionsCustomCMO").complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("respondentsDirections")
                    .showCondition(sendToJudge)
                     .field("respondentsDirectionLabelCMO").readOnly().blacklist(JUDICIARY).done()
                     .field("respondents_label", ReadOnly, null, "TextArea", null, " ")
                     .list("respondentDirectionsCustomCMO").complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getParentsAndRespondentsAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("cafcassDirections")
                    .showCondition(sendToJudge)
                     .label("cafcassDirectionsLabelCMO", "## For Cafcass")
                     .list("cafcassDirectionsCustomCMO").complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("otherPartiesDirections")
                    .showCondition(sendToJudge)
                     .label("otherPartiesDirectionLabelCMO", "## For other parties")
                     .field("others_label", ReadOnly, null, "TextArea", null, " ")
                     .list("otherPartiesDirectionsCustomCMO").complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getOtherPartiesAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("courtDirections")
                    .showCondition(sendToJudge)
                     .label("courtDirectionsLabelCMO", "## For the court")
                     .list("courtDirectionsCustomCMO").complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                 .page("recitals")
                    .showCondition(sendToJudge)
                     .field("orderBasisLabel").readOnly().blacklist(JUDICIARY).done()
                     .label("addRecitalLabel", "### Add recital")
                     .list("recitals").optional().fieldTypeParameter("Recitals").label("Recitals").done()
                .page("schedule")
                    .midEventWebhook("action-cmo")
                    .showCondition(sendToJudge) .field("schedule", Mandatory, null, "Schedule", null, "Schedule")
                    .field("schedule", Mandatory, null, "Schedule", null, "Schedule")
                .page("OrderActionDocumentReview")
                    .showCondition(sendToJudge)
                    .field(CaseData::getOrderAction).context(Complex).complex()
                        .readonly(OrderAction::getDocument)
                        .mandatory(OrderAction::getType)
                        .mandatory(OrderAction::getNextHearingType)
                        .mandatory(OrderAction::getChangeRequestedByJudge)
                    .done()
                .page("nextHearing")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\" AND orderAction.type=\"SEND_TO_ALL_PARTIES\"")
                    .label("nextHearingDateHeading", "## Basis of order")
                    .label("nextHearingDateHintText", "### Add recital")
                    .field(CaseData::getNextHearingDateList).mandatory().done();

        buildComply( "COMPLY_LOCAL_AUTHORITY", CaseData::getLocalAuthorityDirections, Mandatory, ReadOnly)
            .description("Allows Local Authority user access to comply with their directions as well as ones for all parties")
            .explicitGrants()
            .grant("CRU", CCD_LASOLICITOR)
            .grantHistoryOnly(LOCAL_AUTHORITY);

        buildComply("COMPLY_CAFCASS", CaseData::getCafcassDirections, Optional, Optional)
            .description("Allows Cafcass user access to comply with their directions as well as ones for all parties")
            .explicitGrants()
            .grant("CRU", CAFCASS)
            .grantHistoryOnly(LOCAL_AUTHORITY);

        buildComply( "COMPLY_COURT", CaseData::getCourtDirectionsCustom, Optional, Optional)
            .description("Event gives Court user access to comply with their directions as well as all parties")
            .explicitGrants()
            .grant("CRU", HMCTS_ADMIN)
            .grantHistoryOnly(LOCAL_AUTHORITY);

        event("COMPLY_OTHERS")
            .forState(PREPARE_FOR_HEARING)
            .explicitGrants()
            .grant("CRU", CCD_SOLICITOR)
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .name("Comply on behalf of others")
            .description("Event gives SOLICITOR user access to comply with directions for other parties")
            .displayOrder(11)
            .aboutToStartWebhook("comply-on-behalf")
            .aboutToSubmitWebhook()
            .fields()
            .page(1)
                .label("respondents_label", "label")
                .field(CaseData::getRespondentDirectionsCustom).complex(Direction.class)
                    .readonly(Direction::getDirectionType)
                    .readonly(Direction::getDirectionNeeded, "directionText = \"DO_NOT_SHOW\"")
                    .readonly(Direction::getDateToBeCompletedBy)
                    .readonly(Direction::getParentsAndRespondentsAssignee, "respondentDirectionsCustom.parentsAndRespondentsAssignee != \"\"")
                    .field("allParties_label").readOnly().showCondition("respondentDirectionsCustom.parentsAndRespondentsAssignee = \"\"").done()
                    .field(Direction::getResponses).complex(DirectionResponse.class)
                        .readonly(DirectionResponse::getResponder)
                        .mandatory(DirectionResponse::getRespondingOnBehalfOfRespondent)
                        .optional(DirectionResponse::getComplied)
                        .optional(DirectionResponse::getDocumentDetails)
                        .optional(DirectionResponse::getFile)
                        .field("cannotComplyTitle").optional().done()
                        .mandatory(DirectionResponse::getCannotComplyReason)
                        .optional(DirectionResponse::getC2Uploaded)
                        .optional(DirectionResponse::getCannotComplyFile)
                    .done()
                .done()
            .page(2)
                .label("others_label", "label")
                .field(CaseData::getOtherPartiesDirectionsCustom).complex(Direction.class)
                    .readonly(Direction::getDirectionType)
                    .readonly(Direction::getDirectionNeeded, "directionText = \"DO_NOT_SHOW\"")
                    .readonly(Direction::getDateToBeCompletedBy)
                    .readonly(Direction::getOtherPartiesAssignee, "otherPartiesDirectionsCustom.otherPartiesAssignee=\"*\"")
                    .field("allParties_label").readOnly().showCondition("otherPartiesDirectionsCustom.otherPartiesAssignee = \"\"").done()
                    .complex(Direction::getResponses, DirectionResponse.class)
                        .readonly(DirectionResponse::getResponder)
                        .mandatory(DirectionResponse::getRespondingOnBehalfOfOthers)
                        .mandatory(DirectionResponse::getComplied)
                        .optional(DirectionResponse::getDocumentDetails)
                        .optional(DirectionResponse::getFile)
                        .field("cannotComplyTitle").optional().done()
                        .mandatory(DirectionResponse::getCannotComplyReason)
                        .optional(DirectionResponse::getC2Uploaded)
                        .optional(DirectionResponse::getCannotComplyFile);

        event("COMPLY_ON_BEHALF_COURT")
            .forState(PREPARE_FOR_HEARING)
            .explicitGrants()
            .grant("CRU", HMCTS_ADMIN)
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .name("Comply on behalf of others")
            .description("Event gives Court user access to comply with all directions on behalf of others")
            .displayOrder(11)
            .aboutToStartWebhook("comply-on-behalf")
            .aboutToSubmitWebhook()
            .fields()
            .page("Respondents directions")
                .field("localAuthorityDirectionsLabelCMO").type("Label").label("## For the local authority").readOnly().blacklist(HMCTS_ADMIN).done()
                .label("respondents_label", "## For the local authority")
                .field(CaseData::getRespondentDirectionsCustom).caseEventFieldLabel("Direction").complex(Direction.class)
                    .readonly(Direction::getDirectionType)
                    .readonly(Direction::getDirectionNeeded, "directionText = \"DO_NOT_SHOW\"")
                    .readonly(Direction::getDateToBeCompletedBy)
                    .readonly(Direction::getParentsAndRespondentsAssignee, "servedCaseManagementOrders != \"\" AND respondentDirectionsCustom.parentsAndRespondentsAssignee != \"\"")
                    .field("allParties_label", ReadOnly, "servedCaseManagementOrders != \"\" AND respondentDirectionsCustom.parentsAndRespondentsAssignee = \"\"")
                    .complex(Direction::getResponses, DirectionResponse.class)
                        .mandatory(DirectionResponse::getRespondingOnBehalfOfRespondent)
                        .optional(DirectionResponse::getComplied)
                        .optional(DirectionResponse::getDocumentDetails)
                        .optional(DirectionResponse::getFile)
                        .field("cannotComplyTitle").optional().done()
                        .mandatory(DirectionResponse::getCannotComplyReason)
                        .optional(DirectionResponse::getC2Uploaded)
                        .optional(DirectionResponse::getCannotComplyFile)
                    .done()
                .done()
            .page("Other party directions")
                .field("otherPartiesDirectionLabelCMO").type("Label").label("## For the local authority").readOnly().blacklist(HMCTS_ADMIN).done()
                .label("others_label", "## For the local authority")
                .field(CaseData::getOtherPartiesDirectionsCustom).caseEventFieldLabel("Direction").complex(Direction.class)
                    .readonly(Direction::getDirectionType)
                    .readonly(Direction::getDirectionNeeded, "directionText = \"DO_NOT_SHOW\"")
                    .readonly(Direction::getDateToBeCompletedBy)
                    .readonly(Direction::getOtherPartiesAssignee, "servedCaseManagementOrders != \"\" AND otherPartiesDirectionsCustom.otherPartiesAssignee != \"\"")
                    .field("allParties_label", ReadOnly, "servedCaseManagementOrders != \"\" AND otherPartiesDirectionsCustom.otherPartiesAssignee = \"\"")
                    .complex(Direction::getResponses, DirectionResponse.class)
                        .mandatory(DirectionResponse::getRespondingOnBehalfOfOthers)
                        .optional(DirectionResponse::getComplied)
                        .optional(DirectionResponse::getDocumentDetails)
                        .optional(DirectionResponse::getFile)
                        .field("cannotComplyTitle").optional().done()
                        .mandatory(DirectionResponse::getCannotComplyReason)
                        .optional(DirectionResponse::getC2Uploaded)
                        .optional(DirectionResponse::getCannotComplyFile)
                    .done()
                .done()
            .page("Cafcass directions")
                .field("cafcassDirectionsLabelCMO").type("Label").label("## For the local authority").readOnly().blacklist(HMCTS_ADMIN).done()
                .field(CaseData::getCafcassDirectionsCustom).caseEventFieldLabel("Direction").complex(Direction.class)
                    .readonly(Direction::getDirectionType)
                    .readonly(Direction::getDirectionNeeded, "directionText = \"DO_NOT_SHOW\"")
                    .readonly(Direction::getDateToBeCompletedBy)
                    .complex(Direction::getResponse)
                        .optional(DirectionResponse::getComplied)
                        .optional(DirectionResponse::getDocumentDetails)
                        .optional(DirectionResponse::getFile)
                        .field("cannotComplyTitle").optional().done()
                        .mandatory(DirectionResponse::getCannotComplyReason)
                        .optional(DirectionResponse::getC2Uploaded)
                        .optional(DirectionResponse::getCannotComplyFile)
                    .done()
                .done();


        event("internal-change-CMO_PROGRESSION")
            .forState(PREPARE_FOR_HEARING)
            .name("-")
            .endButtonLabel("")
            .explicitGrants()
            .grant("CRU", SYSTEM_UPDATE)
            .aboutToSubmitWebhook("cmo-progression");
    }

    private EventBuilder<CaseData, UserRole, State> buildLimitedUploadDocuments() {
        return event("limitedUploadDocuments")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
            .name("Documents")
            .description("Upload documents")
            .explicitGrants()
            .grant("CRU", CCD_SOLICITOR, CCD_CAFCASSSOLICITOR)
            .grant("R", LOCAL_AUTHORITY)
            .fields()
                .field("otherCourtAdminDocuments", Optional, null, "Collection",
                    "CourtAdminDocument", "Other documents")
            .done();
    }


    private void buildPlacement() {
        event("placement")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
            .name("Placement")
            .explicitGrants()
            .allWebhooks()
            .fields()
            .page("childrenList")
                .pageLabel("Child")
                .midEventWebhook()
                .showCondition("singleChild=\"NO\"")
                .field("singleChild").showCondition("childrenList=\"DO NOT SHOW\"").readOnly().done()
                .field("childrenList").mandatory().done()
            .page("placement")
                .pageLabel("Application and supporting documents")
                .field("placementChildName").showCondition("childrenList=\"DO NOT SHOW\"").mandatory().done()
                .field("placementLabel").readOnly().done()
                .field("placement").complex(Placement.class)
                    .readonly(Placement::getChildId, "placementChildName = \"DO NOT SHOW\"")
                    .mandatory(Placement::getChildName, "placementChildId = \"DO NOT SHOW\"")
                    .mandatory(Placement::getApplication)
                    .optional(Placement::getSupportingDocuments)
                    .optional(Placement::getConfidentialDocuments)
                    .optional(Placement::getOrderAndNotices)
                    .optional(Placement::getConfidentialDocuments)
                    .field(Placement::getConfidentialDocuments).complexWithParent(PlacementConfidentialDocument.class)
                        .mandatory(PlacementConfidentialDocument::getType)
                        .mandatory(PlacementConfidentialDocument::getDocument)
                        .optional(PlacementConfidentialDocument::getDescription)
                    .done()
                    .field(Placement::getOrderAndNotices).complexWithParent(PlacementOrderAndNotices.class)
                    .field("type_label").mandatory().done()
                        .mandatory(PlacementOrderAndNotices::getType)
                        .mandatory(PlacementOrderAndNotices::getDocument)
                        .optional(PlacementOrderAndNotices::getDescription)
                    .done()
                    .optional(Placement::getSupportingDocuments)
                    .field(Placement::getSupportingDocuments).complexWithParent(PlacementSupportingDocument.class)
                        .mandatory(PlacementSupportingDocument::getType)
                        .mandatory(PlacementSupportingDocument::getDocument)
                        .optional(PlacementSupportingDocument::getDescription)
                    .done()
                .done();
    }

    private EventBuilder<CaseData, UserRole, State> buildManageRepresentatives() {
        return event("manageRepresentatives")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
            .name("Manage representatives")
            .aboutToStartWebhook()
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .aboutToSubmitWebhook()
            .submittedWebhook()
            .explicitGrants()
            .showSummaryChangeOption()
            .grant("CRU", HMCTS_ADMIN)
            .fields()
                .midEventWebhook()
                .label("respondents_label", "label")
                .label("others_label", "others")
                .optional(CaseData::getRepresentatives)
            .done();
    }

    private EventBuilder<CaseData, UserRole, State> buildComply(String eventId,
        TypedPropertyGetter<CaseData, Iterable> getter, DisplayContext reasonContext,
        DisplayContext titleContext) {
        return event(eventId)
                .forState(PREPARE_FOR_HEARING)
                .explicitGrants()
                .name("Comply with directions")
                .displayOrder(11)
                .aboutToStartWebhook("comply-with-directions")
                .aboutToSubmitWebhook()
                .fields()
                    .list(getter).caseEventFieldLabel("Direction").complex(Direction.class)
                        .readonly(Direction::getDirectionType)
                        .readonly(Direction::getDirectionNeeded, "directionText = \"DO_NOT_SHOW\"")
                        .field(Direction::getDateToBeCompletedBy).readOnly().label("Deadline").done()
                        .complex(Direction::getResponse)
                            .optional(DirectionResponse::getComplied)
                            .optional(DirectionResponse::getDocumentDetails)
                            .optional(DirectionResponse::getFile)
                            .field("cannotComplyTitle").context(titleContext).done()
                            .field(DirectionResponse::getCannotComplyReason, reasonContext)
                            .optional(DirectionResponse::getC2Uploaded)
                            .optional(DirectionResponse::getCannotComplyFile)
                        .done()
                    .done()
                .done();
    }

    private void renderDirection(FieldCollection.FieldCollectionBuilder<Direction, ?> f) {
        f.mandatory(Direction::getDirectionType)
            .mandatory(Direction::getDirectionText)
            .optional(Direction::getDateToBeCompletedBy);
    }

    private EventBuilder<CaseData, UserRole, State> buildHearingBookingDetails() {
        return event( "hearingBookingDetails")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
            .grant("CRU", HMCTS_ADMIN, JUDICIARY, GATEKEEPER)
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .name("Add hearing details")
            .description("Add hearing booking details to a case")
            .aboutToStartWebhook("add-hearing-bookings")
            .aboutToSubmitWebhook()
            .submittedWebhook()
            .showSummary()
            .fields()
            .field("allocatedJudgeLabel").readOnly().showSummary().done()
            .midEventWebhook("add-hearing-bookings")
            .complex(CaseData::getHearingDetails, HearingBooking.class)
                .mandatory(HearingBooking::getType)
                .mandatory(HearingBooking::getTypeDetails, "hearingDetails.type=\"OTHER\"")
                .mandatory(HearingBooking::getVenue)
                    .mandatory("venueCustomAddress", "hearingDetails.venue=\"OTHER\"")
                    .mandatory("venueCustomAddress.AddressLine1")
                    .optional("venueCustomAddress.AddressLine2")
                    .optional("venueCustomAddress.AddressLine3")
                    .optional("venueCustomAddress.PostTown")
                    .optional("venueCustomAddress.County")
                    .mandatory("venueCustomAddress.PostCode")
                    .optional("venueCustomAddress.Country")
////TODO               doesn't seem to be supported .mandatory(HearingBooking::getVenueCustomAddress, "hearingDetails.venue=\"OTHER\"")
////                .complex(HearingBooking::getVenueCustomAddress)
////                    .mandatory(Address::getAddressLine1)
////                    .mandatory(Address::getAddressLine2)
////                    .mandatory(Address::getAddressLine3)
////                    .mandatory(Address::getPostTown)
////                    .mandatory(Address::getCounty)
////                    .mandatory(Address::getPostcode)
////                    .mandatory(Address::getCounty)
////                    .done()
                .mandatory(HearingBooking::getStartDate)
                .mandatory(HearingBooking::getEndDate)
                .mandatory(HearingBooking::getHearingNeedsBooked)
                .mandatory(HearingBooking::getHearingNeedsDetails, "hearingDetails.hearingNeedsBooked!=\"NONE\"")
            .complex(HearingBooking::getJudgeAndLegalAdvisor)
                    .mandatory(JudgeAndLegalAdvisor::getUseAllocatedJudge, "hearingDetails.startDate!=\"ALWAYS_SHOW\"")
                    .label("judgeSubHeading", "Who is issuing the order?")
                    .mandatory(JudgeAndLegalAdvisor::getJudgeTitle, "hearingDetails.judgeAndLegalAdvisor.useAllocatedJudge=\"No\"")
                    .mandatory(JudgeAndLegalAdvisor::getOtherTitle)
                    .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                    .optional(JudgeAndLegalAdvisor::getJudgeFullName)
                    .optional(JudgeAndLegalAdvisor::getLegalAdvisorName)
                    .done()
                .done()
            .done();
    }

    private void buildMultiStateEvents() {
        buildCreateOrderEvent();
        buildNoticeOfProceedings();
        buildUploadC2();
        buildStatementOfService();
        buildPlacement();
        buildStandardDirections();
        buildLimitedUploadDocuments();
        buildManageRepresentatives();


        event("amendChildren")
                .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
                .name("Children")
                .description("Amending the children for the case")
                .aboutToStartWebhook("enter-children")
                .aboutToSubmitWebhook()
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .showEventNotes()
                .fields()
                    .complex(CaseData::getChildren1, Child.class, false);
//                        .optional("party")
//                        .optional("party.firstName")
//                        .optional("party.lastName")
//                        .optional("party.dateOfBirth")
//                        .optional("party.gender")
//                        .optional("party.genderIdentification")
//                        .optional("party.livingSituation")
//                        .optional("party.firstName")
//                        .optional("party.livingSituationDetails")
//                        .optional("party.addressChangeDate")
//                        .optional("party.datePowersEnd")
//                        .optional("party.careStartDate")
//                        .optional("party.dischargeDate")
//                        .optional("party.address")
//                        .optional("party.address.AddressLine1")
//                        .optional("party.address")
//                        .optional("party.datePowersEnd")
//                        .optional("party.datePowersEnd")
//                        .optional("party.datePowersEnd")
        event("amendRespondents")
                .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
                .name("Respondents")
                .description("Amending the respondents for the case")
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .aboutToStartWebhook("enter-respondents")
                .aboutToSubmitWebhook()
                .showEventNotes()
                .fields()
                    .optional(CaseData::getRespondents1);
        event("amendOthers")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
                .name("Others to be given notice")
                .description("Amending others for the case")
                .showEventNotes()
                .aboutToStartWebhook("enter-others")
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .aboutToSubmitWebhook()
                .fields()
                    .optional(CaseData::getOthers);
        event("amendInternationalElement")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING, CLOSED)
                .name("International element")
                .description("Amending the international element")
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .showEventNotes()
                .fields()
                    .optional(CaseData::getInternationalElement);
        event("amendOtherProceedings")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
                .name("Other proceedings")
                .description("Amending other proceedings and allocation proposals")
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .showEventNotes()
                .fields()
                    .midEventWebhook("enter-other-proceedings")
                    .optional(CaseData::getProceeding);
        event("amendAttendingHearing")
            .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
                .name("Attending the hearing")
                .description("Amend extra support needed for anyone to take part in hearing")
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .showEventNotes()
                .fields()
                    .optional(CaseData::getHearingPreferences);


        event("migrateCase")
            .forAllStates()
                .name("Migrate case")
                .endButtonLabel("")
                .explicitGrants()
                .grant("CRUD", SYSTEM_UPDATE);
    }

    private void buildNoticeOfProceedings() {
        event("createNoticeOfProceedings")
        .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING)
        .name("Create notice of proceedings")
        .explicitGrants()
        .grantHistoryOnly(GATEKEEPER, LOCAL_AUTHORITY)
        .grant("CRU", HMCTS_ADMIN)
        .showSummary()
        .aboutToStartWebhook("notice-of-proceedings")
        .aboutToSubmitWebhook()
        .submittedWebhook()
        .fields()
            .field("proceedingLabel",  ReadOnly, null, "Text", null, " ")
            .complex(CaseData::getNoticeOfProceedings)
                .complex(NoticeOfProceedings::getJudgeAndLegalAdvisor)
                    .readonly(JudgeAndLegalAdvisor::getAllocatedJudgeLabel)
                    .mandatory(JudgeAndLegalAdvisor::getUseAllocatedJudge)
                    .mandatory("judgeSubHeading")
                    .mandatory(JudgeAndLegalAdvisor::getJudgeTitle)
                    .mandatory(JudgeAndLegalAdvisor::getOtherTitle)
                    .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                    .mandatory(JudgeAndLegalAdvisor::getJudgeFullName)
                    .mandatory(JudgeAndLegalAdvisor::getJudgeEmailAddress)
                    .optional(JudgeAndLegalAdvisor::getLegalAdvisorName)
                .done()
                .mandatory(NoticeOfProceedings::getProceedingTypes);
    }

    private EventBuilder<CaseData, UserRole, State> buildUploadC2() {
        return event("uploadC2")
        .forStates(SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING, CLOSED)
        .explicitGrants()
        .grant("CRU", HMCTS_ADMIN, CCD_LASOLICITOR, CCD_SOLICITOR, CAFCASS)
        .grantHistoryOnly(LOCAL_AUTHORITY)
        .name("Upload a C2")
        .description("Upload a c2 to the case")
        .aboutToSubmitWebhook()
        .submittedWebhook()
        .fields()
            .page(1)
                .midEventWebhook("upload-c2/get-fee")
                .mandatory(CaseData::getC2ApplicationType)
            .page(2)
                .midEventWebhook("upload-c2/validate-pba-number")
                .field(CaseData::getTemporaryC2Document).complex()
                    .optional(C2DocumentBundle::getNameOfRepresentative)
                    .mandatory(C2DocumentBundle::getUsePbaPayment)
                    .mandatory(C2DocumentBundle::getPbaNumber, "temporaryC2Document.usePbaPayment=\"Yes\"")
                    .optional(C2DocumentBundle::getClientCode, "temporaryC2Document.usePbaPayment=\"Yes\"")
                    .optional(C2DocumentBundle::getFileReference, "temporaryC2Document.usePbaPayment=\"Yes\"")
                    .mandatory(C2DocumentBundle::getDocument)
                    .mandatory(C2DocumentBundle::getDescription).done()
                .field("displayAmountToPay").readOnly().showCondition("c2ApplicationType=\"DO_NOT_SHOW\"").done()
                .field("amountToPay").readOnly().showCondition("displayAmountToPay=\"Yes\"").done()
            .done();
    }

    private void buildOpen() {
        // Local Authority can view the history of all events in the Open state.
        grantHistory(OPEN,LOCAL_AUTHORITY);
        event("openCase")
                .initialState(OPEN)
                .name("Start application")
                .description("Create a new case – add a title")
                .grant("CRU", LOCAL_AUTHORITY)
                .aboutToSubmitWebhook("case-initiation")
                .submittedWebhook()
                .retries(1,2,3,4,5)
                .fields()
                    .optional(CaseData::getCaseName);

        event("ordersNeeded").forState(OPEN)
                .name("Orders and directions needed")
                .description("Selecting the orders needed for application")
                .aboutToSubmitWebhook("orders-needed")
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getOrders);

        event("hearingNeeded").forStates(OPEN, RETURNED)
                .name("Hearing needed")
                .description("Selecting the hearing needed for application")
                .explicitGrants()
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getHearing);

        event("enterChildren").forStates(OPEN, RETURNED)
                .name("Children")
                .description("Entering the children for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .explicitGrants()
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .list(CaseData::getChildren1)
            //TODO how to deal with single party element
                    .complex();



        event("enterRespondents").forStates(OPEN, RETURNED)
                .name("Respondents")
                .description("Entering the respondents for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .explicitGrants()
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .midEventWebhook()
                    .list(CaseData::getRespondents1).optional();

        event("enterApplicant").forStates(OPEN, RETURNED)
                .name("Applicant")
                .description("Entering the applicant for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .midEventWebhook()
                    .optional(CaseData::getApplicants)
                    .optional(CaseData::getSolicitor);

        event("enterOthers").forStates(OPEN, RETURNED)
                .name("Others to be given notice")
                .description("Entering others for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getOthers);

        event("enterGrounds").forStates(OPEN, RETURNED)
                .name("Grounds for the application")
                .description("Entering the grounds for the application")
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .field("EPO_REASONING_SHOW", Optional, "groundsForEPO CONTAINS \"Workaround to show groundsForEPO. Needs to be hidden from UI\"", "MultiSelectList", "ShowHide", "EPO Reason show or hide")
                    .optional(CaseData::getGroundsForEPO, "EPO_REASONING_SHOW CONTAINS \"SHOW_FIELD\"")
                    .optional(CaseData::getGrounds);

        event("enterRiskHarm").forStates(OPEN, RETURNED)
                .name("Risk and harm to children")
                .description("Entering opinion on risk and harm to children")
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getRisks);

        event("enterParentingFactors").forStates(OPEN, RETURNED)
                .name("Factors affecting parenting")
                .description("Entering the factors affecting parenting")
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getFactorsParenting);

        event("enterInternationalElement").forStates(OPEN, RETURNED)
                .name("International element")
                .description("Entering the international element")
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getInternationalElement);

        event("otherProceedings").forStates(OPEN, RETURNED)
                .name("Other proceedings")
                .description("Entering other proceedings and proposals")
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .midEventWebhook("enter-other-proceedings")
                    .optional(CaseData::getProceeding);

        event("otherProposal").forStates(OPEN, RETURNED)
                .name("Allocation proposal")
                .description("Entering other proceedings and allocation proposals")
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .label("allocationProposal_label", "This should be completed by a solicitor with good knowledge of the case. Use the [President's Guidance](https://www.judiciary.uk/wp-content/uploads/2013/03/President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf) and [schedule](https://www.judiciary.uk/wp-content/uploads/2013/03/Schedule-to-the-President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf) on allocation and gatekeeping to make your recommendation.")
                    .field(CaseData::getAllocationProposal).complex()
                        .mandatory(Allocation::getProposal)
                        .optional(Allocation::getProposalReason);

        event("attendingHearing").forStates(OPEN, RETURNED)
                .name("Attending the hearing")
                .description("Enter extra support needed for anyone to take part in hearing")
                .displayOrder(13)
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getHearingPreferences);

        event("changeCaseName").forStates(OPEN, RETURNED)
                .name("Change case name")
                .description("Change case name")
                .displayOrder(15)
                .grant("CRU", CCD_LASOLICITOR)
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getCaseName);
    }

}
