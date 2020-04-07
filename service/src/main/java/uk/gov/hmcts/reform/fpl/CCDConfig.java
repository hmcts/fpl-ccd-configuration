package uk.gov.hmcts.reform.fpl;


import com.google.common.base.CaseFormat;
import de.cronn.reflection.util.TypedPropertyGetter;
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
        role(SYSTEM_UPDATE).setApiOnly();

        // Disable AuthorisationCaseField generation for these roles.
        // TODO: complete configuration of permissions for these roles.
        role(CCD_SOLICITOR, CCD_LASOLICITOR).noCaseEventToField();

        // Events
        buildUniversalEvents();
        buildOpen();
        buildSubmittedEvents();
        buildPrepareForHearing();
        buildGatekeepingEvents();
        buildTransitions();

        // UI tabs and inputs.
        buildTabs();
        buildWorkBasketResultFields();
        buildWorkBasketInputFields();

        // Restrict specific fields for Cafcass and Local Authority.
        field("dateSubmitted").blacklist("R", LOCAL_AUTHORITY);
        field("evidenceHandled").blacklist(CAFCASS, LOCAL_AUTHORITY);
    }

    private void buildWorkBasketResultFields() {
        workBasketResultFields()
            .field(CaseData::getCaseName, "Case name")
            .field(CaseData::getFamilyManCaseNumber, "FamilyMan case number")
            .field("[STATE]", "State")
            .field(CaseData::getCaseLocalAuthority, "Local authority")
            .field("dateAndTimeSubmitted", "Date submitted")
            .field("evidenceHandled", "Supplementary evidence handled");
    }

    private void buildWorkBasketInputFields() {
        workBasketInputFields()
            .field(CaseData::getCaseLocalAuthority, "Local authority")
            .field(CaseData::getCaseName, "Case name")
            .field(CaseData::getFamilyManCaseNumber, "FamilyMan case number")
            .field("[CASE_REFERENCE]", "CCD Case Number")
            .field(CaseData::getDateSubmitted, "Date submitted")
            .field("evidenceHandled", "Supplementary evidence handled");
    }

    private void buildTabs() {
        tab("HearingTab", "Hearings")
            .restrictedField(CaseData::getHearingDetails).exclude(CAFCASS)
            .restrictedField(CaseData::getHearing).exclude(LOCAL_AUTHORITY);

        tab("DraftOrdersTab", "Draft orders")
            .exclude(LOCAL_AUTHORITY, HMCTS_ADMIN, GATEKEEPER, JUDICIARY)
            .showCondition("standardDirectionOrder.orderStatus!=\"SEALED\" OR caseManagementOrder!=\"\" OR sharedDraftCMODocument!=\"\" OR cmoToAction!=\"\"")
            .field(CaseData::getStandardDirectionOrder, "standardDirectionOrder.orderStatus!=\"SEALED\"")
            .field(CaseData::getSharedDraftCMODocument)
            .restrictedField(CaseData::getCaseManagementOrder_Judiciary).exclude(CAFCASS)
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
            .field(CaseData::getChildren1)
            .field(CaseData::getRespondents1)
            .field(CaseData::getApplicants)
            .field(CaseData::getSolicitor)
            .field(CaseData::getOthers)
            .restrictedField(CaseData::getRepresentatives).exclude(CAFCASS, LOCAL_AUTHORITY);

        tab("LegalBasisTab", "Legal basis")
            .exclude(LOCAL_AUTHORITY)
            .field(CaseData::getStatementOfService)
            .field(CaseData::getGroundsForEPO)
            .field(CaseData::getGrounds)
            .field(CaseData::getRisks)
            .field(CaseData::getFactorsParenting)
            .field(CaseData::getInternationalElement)
            .field(CaseData::getProceeding)
            .field(CaseData::getAllocationDecision)
            .field(CaseData::getAllocationProposal)
            .field(CaseData::getHearingPreferences);

        tab("DocumentsTab", "Documents")
            .exclude(LOCAL_AUTHORITY)
            .field(CaseData::getSocialWorkChronologyDocument)
            .field(CaseData::getSocialWorkStatementDocument)
            .field(CaseData::getSocialWorkAssessmentDocument)
            .field(CaseData::getSocialWorkCarePlanDocument)
            .field("standardDirectionsDocument")
            .field("otherCourtAdminDocuments")
            .field(CaseData::getSocialWorkEvidenceTemplateDocument)
            .field(CaseData::getThresholdDocument)
            .field(CaseData::getChecklistDocument)
            .field("courtBundle")
            .field(CaseData::getOtherSocialWorkDocuments)
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
    }

    private void buildUniversalEvents() {
        event("internal-change:SEND_DOCUMENT")
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
            .endButtonLabel("")
            .name("Attach scanned docs")
            .fields()
            .page(1)
                .pageLabel("BulkScanning")
                .field("scannedDocuments").optional().done()
            .page(2)
                .pageLabel("BulkScanning")
                .field("evidenceHandled").mandatory();

        event("allocatedJudge")
            .forAllStates()
            .name("Allocated Judge")
            .description("Add allocated judge to a case")
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .grant("CRU", JUDICIARY, HMCTS_ADMIN, GATEKEEPER)
            .grant("R", CAFCASS)
            .fields()
            .page("AllocatedJudge")
                .field(CaseData::getAllocatedJudge).complex()
                    .mandatory(Judge::getJudgeTitle)
                    .mandatory(Judge::getOtherTitle)
                    .mandatory(Judge::getJudgeLastName)
                    .mandatory(Judge::getJudgeFullName);

        event("addCaseNote")
            .forAllStates()
            .name("Add a case note")
            .grant("CRU", JUDICIARY)
            .aboutToSubmitWebhook("add-note")
            .fields()
            .page("CaseNote")
                .mandatory(CaseData::getCaseNote);

        event("uploadDocuments")
            .forAllStates()
            .explicitGrants()
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .grant("CRU", CCD_LASOLICITOR)
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
    }

    private EventBuilder<CaseData, UserRole, State> buildCreateOrderEvent(State state) {
        return event("createOrder")
                .forState(state)
                .explicitGrants()
                .grant("CRU", HMCTS_ADMIN, JUDICIARY)
                .name("Create an order")
                .showSummary()
                .allWebhooks("create-order")
                .fields()
                .page("OrderTypeAndDocument")
                    .complex(CaseData::getOrderTypeAndDocument)
                        .mandatory(OrderTypeAndDocument::getType)
                        .mandatory(OrderTypeAndDocument::getSubtype)
                        .readonly(OrderTypeAndDocument::getDocument, "document=\"DO_NOT_SHOW\"")
                    .done()
                    .field("pageShow", ReadOnly, "orderTypeAndDocument=\"DO_NOT_SHOW\"")
                .page("OrderDateOfIssue")
                    .midEventWebhook("validate-order/date-of-issue")
                    .field("dateOfIssue_label").readOnly().label("dateOfIssue_label").done()
                    .field(CaseData::getDateOfIssue).mandatory().showSummary().done()
                .page("OrderAppliesToAllChildren")
                    .showCondition("pageShow=\"Yes\"")
                    .midEventWebhook("create-order/populate-selector")
                    .field(CaseData::getOrderAppliesToAllChildren).mandatory().showSummary().done()
                .page("ChildrenSelection")
                    .showCondition("orderAppliesToAllChildren=\"No\"")
                    .midEventWebhook("validate-order/child-selector")
                    .label("children_label", "")
                    .complex(CaseData::getChildSelector).done()
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
                .done();
    }

    private void buildTransitions() {
        event("submitApplication")
                .forStateTransition(Open, Submitted)
                .name("Submit application")
                .displayOrder(17) // TODO - necessary?
                .explicitGrants()
                .grantHistoryOnly(LOCAL_AUTHORITY, HMCTS_ADMIN, GATEKEEPER, JUDICIARY, CAFCASS)
                .grant("CRU", CCD_LASOLICITOR)
                .endButtonLabel("Submit")
                .allWebhooks("case-submission")
                .retries(1,2,3,4,5)
                .fields()
                    .field("submissionConsentLabel").readOnly().type("Text").label(" ").done()
                    .field("submissionConsent").mandatory().type("MultiSelectList").fieldTypeParameter("Consent").label(" ");

        event("populateSDO")
                .forStateTransition(Submitted, Gatekeeping)
                .name("Populate standard directions")
                .displayOrder(14) // TODO - necessary?
                .explicitGrants()
                .grant("C", UserRole.SYSTEM_UPDATE)
                .fields()
                    .optional(CaseData::getAllParties)
                    .optional(CaseData::getLocalAuthorityDirections)
                    .optional(CaseData::getRespondentDirections)
                    .optional(CaseData::getCafcassDirections)
                    .optional(CaseData::getOtherPartiesDirections)
                    .optional(CaseData::getCourtDirections);

        event("deleteApplication")
                .forStateTransition(Open, Deleted)
                .displayOrder(18) // TODO - necessary?
                .grantHistoryOnly(LOCAL_AUTHORITY)
                .grant("CRU", CCD_LASOLICITOR)
                .name("Delete an application")
                .aboutToSubmitWebhook("case-deletion")
                .endButtonLabel("Delete application")
                .fields()
                    .field("deletionConsent", Mandatory, null, "MultiSelectList", "DeletionConsent", " ");

        event("internal-changeState:Gatekeeping->PREPARE_FOR_HEARING")
            .forStateTransition(Gatekeeping, PREPARE_FOR_HEARING)
            .name("-")
            .endButtonLabel("")
            .explicitGrants()
            .grant("C", SYSTEM_UPDATE);
    }

    private void buildGatekeepingEvents() {
        grant(Gatekeeping, "CRU", GATEKEEPER);
        event("otherAllocationDecision")
                .forState(Gatekeeping)
                .name("Allocation decision")
                .description("Entering other proceedings and allocation proposals")
                .showSummary()
                .aboutToStartWebhook("allocation-decision", 1, 2, 3, 4, 5)
                .aboutToSubmitWebhook()
                .fields()
                    .field(CaseData::getAllocationDecision, Mandatory, true);

        buildHearingBookingDetails(Gatekeeping)
            .grant("CRU", GATEKEEPER);
        buildSharedEvents(Gatekeeping);
        buildNoticeOfProceedings(Gatekeeping);

        event("draftSDO")
                .forState(Gatekeeping)
                .name("Draft standard directions")
                .allWebhooks("draft-standard-directions")
                .fields()
                    .page("SdoDateOfIssue")
                        .midEventWebhook("validate-order/date-of-issue")
                        .field("dateOfIssue_label").readOnly().done()
                        .mandatory(CaseData::getDateOfIssue)
                    .page("judgeAndLegalAdvisor")
                        .optional(CaseData::getJudgeAndLegalAdvisor)
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

        buildStandardDirections(Gatekeeping, "AfterGatekeeping", "");
        buildUploadC2(Gatekeeping)
            .submittedWebhook("upload-c2");
        buildCreateOrderEvent(Gatekeeping)
            .showSummaryChangeOption();
        event("uploadDocumentsAfterGatekeeping")
                .forState(Gatekeeping)
                .name("Documents")
                .description("Only here for backwards compatibility with case history")
                .explicitGrants()
                .grant("R", LOCAL_AUTHORITY, CCD_LASOLICITOR);
        buildLimitedUploadDocuments(Gatekeeping, 11)
            .grant("R", LOCAL_AUTHORITY);
        buildStatementOfService(Gatekeeping);
        buildManageRepresentatives(Gatekeeping);
        buildPlacement(Gatekeeping);
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
                .optional(Direction::getDirectionNeeded)
                .optional(Direction::getDirectionText, "{{FIELD_NAME}}.readOnly!=\"Yes\" AND {{FIELD_NAME}}.directionNeeded!=\"No\"")
                .optional(Direction::getDateToBeCompletedBy);
    }

    private void buildStandardDirections(State state, String suffix, String buttonLabel) {
        event("uploadStandardDirections" + suffix)
                .forState(state)
                .name("Documents")
                .description("Upload standard directions")
                .explicitGrants()
                .endButtonLabel(buttonLabel)
                .grant("CRU", HMCTS_ADMIN)
                .fields()
                    .label("standardDirectionsLabel", "Upload standard directions and other relevant documents, for example the C6 Notice of Proceedings or C9 statement of service.")
                    .label("standardDirectionsTitle", "## 1. Standard directions")
                    .field("standardDirectionsDocument", Optional, null, "Document", null, "Upload a file")
                    .field("otherCourtAdminDocuments", Optional, null, "Collection", "CourtAdminDocument", "Other documents");
    }

    private void buildSubmittedEvents() {
        grant(Submitted, "CRU", HMCTS_ADMIN);

        buildHearingBookingDetails(Submitted)
            .grant("CRU", JUDICIARY, GATEKEEPER)
            .aboutToSubmitWebhook();
        this.buildStandardDirections(Submitted, "", "Save and continue");
        buildUploadC2(Submitted)
            .submittedWebhook("upload-c2");

        event("sendToGatekeeper")
                .forState(Submitted)
                .name("Send to gatekeeper")
                .description("Send email to gatekeeper")
                .explicitGrants()
                .grant("CRU", HMCTS_ADMIN)
                .aboutToStartWebhook("notify-gatekeeper")
                .submittedWebhook()
                .fields()
                    .label("gateKeeperLabel", "Let the gatekeeper know there's a new case")
                    .mandatory(CaseData::getGatekeeperEmail);
        buildSharedEvents(Submitted);
        buildNoticeOfProceedings(Submitted);
        buildStatementOfService(Submitted);
        buildCreateOrderEvent(Submitted);

        event("uploadDocumentsAfterSubmission")
                .forState(Submitted)
                .explicitGrants()
                .grant("R", LOCAL_AUTHORITY, CCD_LASOLICITOR)
                .name("Documents")
                .description("Only here for backwards compatibility with case history");

        buildLimitedUploadDocuments(Submitted, 15)
            .grant("R", LOCAL_AUTHORITY);
        buildManageRepresentatives(Submitted)
            .showSummaryChangeOption()
            .submittedWebhook();
        buildPlacement(Submitted);
    }

    private void buildStatementOfService(State state) {
        event("addStatementOfService")
                .forState(state)
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
        grant(PREPARE_FOR_HEARING, "CRU", HMCTS_ADMIN);
        buildHearingBookingDetails(PREPARE_FOR_HEARING);
        buildSharedEvents( PREPARE_FOR_HEARING);

        event("uploadOtherCourtAdminDocuments-PREPARE_FOR_HEARING")
            .forState(PREPARE_FOR_HEARING)
            .name("Documents")
            .description("Upload documents")
            .grant("CRU", HMCTS_ADMIN)
            .fields()
            .field("otherCourtAdminDocuments", Optional, null, "Collection", "CourtAdminDocument", "Other documents");

        buildLimitedUploadDocuments(PREPARE_FOR_HEARING, 8)
            .grant("CRU", CCD_SOLICITOR);

        buildUploadC2(PREPARE_FOR_HEARING);
        buildNoticeOfProceedings(PREPARE_FOR_HEARING);
        buildCreateOrderEvent(PREPARE_FOR_HEARING);

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
                .list(CaseData::getAllPartiesCustomCMO).complex(Direction.class, this::renderDirection)
                .page("localAuthorityDirections")
                    .showCondition(notSendToJudge)
                     .label("localAuthorityDirectionsLabelCMO", "## For the local authority")
                     .list(CaseData::getLocalAuthorityDirectionsCustomCMO).complex(Direction.class, this::renderDirection)
                .page(2)
                    .showCondition(notSendToJudge)
                     .label("respondentsDirectionLabelCMO", "## For the parents or respondents")
                     .field("respondents_label", ReadOnly, null, "TextArea", null, " ")
                     .list(CaseData::getRespondentDirectionsCustomCMO).complex(Direction.class)
                        .mandatory(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getParentsAndRespondentsAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("cafcassDirections")
                    .showCondition(notSendToJudge)
                     .label("cafcassDirectionsLabelCMO", "## For Cafcass")
                     .list(CaseData::getCafcassDirectionsCustomCMO).complex(Direction.class, this::renderDirection)
                .page(3)
                    .showCondition(notSendToJudge)
                     .label("otherPartiesDirectionLabelCMO", "## For other parties")
                     .field("others_label", ReadOnly, null, "TextArea", null, " ")
                     .list(CaseData::getOtherPartiesDirectionsCustomCMO).complex(Direction.class)
                        .mandatory(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getOtherPartiesAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("courtDirections")
                    .showCondition(notSendToJudge)
                     .label("courtDirectionsLabelCMO", "## For the court")
                     .list(CaseData::getCourtDirectionsCustomCMO).complex(Direction.class, this::renderDirection)
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
            .explicitGrants()
            .grant("CRU", JUDICIARY)
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
                    .field(CaseData::getCaseManagementOrder_Judiciary).optional().complex()
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
                    .list(CaseData::getAllPartiesCustomCMO).complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("localAuthorityDirections")
                    .showCondition(sendToJudge)
                     .label("localAuthorityDirectionsLabelCMO", "## For the local authority")
                     .list(CaseData::getLocalAuthorityDirectionsCustomCMO).complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("respondentsDirections")
                    .showCondition(sendToJudge)
                     .field("respondentsDirectionLabelCMO").readOnly().blacklist(JUDICIARY).done()
                     .field("respondents_label", ReadOnly, null, "TextArea", null, " ")
                     .list(CaseData::getRespondentDirectionsCustomCMO).complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getParentsAndRespondentsAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("cafcassDirections")
                    .showCondition(sendToJudge)
                     .label("cafcassDirectionsLabelCMO", "## For Cafcass")
                     .list(CaseData::getCafcassDirectionsCustomCMO).complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("otherPartiesDirections")
                    .showCondition(sendToJudge)
                     .label("otherPartiesDirectionLabelCMO", "## For other parties")
                     .field("others_label", ReadOnly, null, "TextArea", null, " ")
                     .list(CaseData::getOtherPartiesDirectionsCustomCMO).complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getOtherPartiesAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("courtDirections")
                    .showCondition(sendToJudge)
                     .label("courtDirectionsLabelCMO", "## For the court")
                     .list(CaseData::getCourtDirectionsCustomCMO).complex(Direction.class)
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

        buildStatementOfService(PREPARE_FOR_HEARING);
        buildManageRepresentatives(PREPARE_FOR_HEARING)
            .submittedWebhook();

        buildComply( "COMPLY_LOCAL_AUTHORITY", CaseData::getLocalAuthorityDirections, Mandatory, ReadOnly)
            .description("Allows Local Authority user access to comply with their directions as well as ones for all parties")
            .grant("CRU", LOCAL_AUTHORITY);
        buildComply( "COMPLY_CAFCASS", CaseData::getCafcassDirections, Optional, Optional)
            .description("Allows Cafcass user access to comply with their directions as well as ones for all parties")
            .grant("CRU", CAFCASS);

        buildComply( "COMPLY_COURT", CaseData::getCourtDirectionsCustom, Optional, Optional)
            .description("Event gives Court user access to comply with their directions as well as all parties")
            .grant("CRU", HMCTS_ADMIN);

        event("COMPLY_OTHERS")
            .forState(PREPARE_FOR_HEARING)
            .explicitGrants()
            .grant("CRU", CCD_SOLICITOR)
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

        buildPlacement(PREPARE_FOR_HEARING);
        event("internal-change:CMO_PROGRESSION")
            .forState(PREPARE_FOR_HEARING)
            .name("-")
            .endButtonLabel("")
            .explicitGrants()
            .grant("CRU", SYSTEM_UPDATE)
            .aboutToSubmitWebhook("cmo-progression");
    }

    private EventBuilder<CaseData, UserRole, State> buildLimitedUploadDocuments(State state, int displayOrder) {
        return event("limitedUploadDocuments")
            .forState(state)
            .name("Documents")
            .description("Upload documents")
            .displayOrder(displayOrder)
            .explicitGrants()
            .grant("CRU", CCD_SOLICITOR)
            .fields()
                .field("otherCourtAdminDocuments", Optional, null, "Collection",
                    "CourtAdminDocument", "Other documents")
            .done();
    }


    private void buildPlacement(State state) {
        event("placement")
            .forState(state)
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

    private EventBuilder<CaseData, UserRole, State> buildManageRepresentatives(State state) {
        return event("manageRepresentatives")
            .forState(state)
            .name("Manage representatives")
            .aboutToStartWebhook()
            .aboutToSubmitWebhook()
            .explicitGrants()
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

    private EventBuilder<CaseData, UserRole, State> buildHearingBookingDetails(State state) {
        return event( "hearingBookingDetails")
            .forState(state)
            .grant("CRU", HMCTS_ADMIN)
            .name("Add hearing details")
            .description("Add hearing booking details to a case")
            .aboutToStartWebhook("add-hearing-bookings")
            .showSummary()
            .fields()
            .midEventWebhook("add-hearing-bookings")
            .complex(CaseData::getHearingDetails, HearingBooking.class)
                .mandatory(HearingBooking::getType)
                .mandatory(HearingBooking::getTypeDetails, "hearingDetails.type=\"OTHER\"")
                .mandatory(HearingBooking::getVenue)
                .mandatory(HearingBooking::getStartDate)
                .mandatory(HearingBooking::getEndDate)
                .mandatory(HearingBooking::getHearingNeedsBooked)
                .mandatory(HearingBooking::getHearingNeedsDetails, "hearingDetails.hearingNeedsBooked!=\"NONE\"")
                .complex(HearingBooking::getJudgeAndLegalAdvisor)
                    .mandatory(JudgeAndLegalAdvisor::getJudgeTitle)
                    .mandatory(JudgeAndLegalAdvisor::getOtherTitle, "hearingDetails.judgeAndLegalAdvisor.judgeTitle=\"OTHER\"")
                    .mandatory(JudgeAndLegalAdvisor::getJudgeLastName, "hearingDetails.judgeAndLegalAdvisor.judgeTitle!=\"MAGISTRATES\" AND hearingDetails.judgeAndLegalAdvisor.judgeTitle!=\"\"")
                    .optional(JudgeAndLegalAdvisor::getJudgeFullName, "hearingDetails.judgeAndLegalAdvisor.judgeTitle=\"MAGISTRATES\"")
                    .optional(JudgeAndLegalAdvisor::getLegalAdvisorName)
                    .done()
                .done()
            .done();
    }

    private void buildSharedEvents(State state) {
        event("amendChildren")
                .forState(state)
                .name("Children")
                .description("Amending the children for the case")
                .aboutToStartWebhook("enter-children")
                .aboutToSubmitWebhook()
                .showEventNotes()
                .fields()
                    .optional(CaseData::getChildren1);
        event("amendRespondents")
                .forState(state)
                .name("Respondents")
                .description("Amending the respondents for the case")
                .aboutToStartWebhook("enter-respondents")
                .aboutToSubmitWebhook()
                .showEventNotes()
                .fields()
                    .optional(CaseData::getRespondents1);
        event("amendOthers")
                .forState(state)
                .name("Others to be given notice")
                .description("Amending others for the case")
                .showEventNotes()
                .aboutToStartWebhook("enter-others")
                .aboutToSubmitWebhook()
                .fields()
                    .optional(CaseData::getOthers);
        event("amendInternationalElement")
                .forState(state)
                .name("International element")
                .description("Amending the international element")
                .showEventNotes()
                .fields()
                    .optional(CaseData::getInternationalElement);
        event("amendOtherProceedings")
                .forState(state)
                .name("Other proceedings")
                .description("Amending other proceedings and allocation proposals")
                .showEventNotes()
                .fields()
                    .midEventWebhook("enter-other-proceedings")
                    .optional(CaseData::getProceeding);
        event("amendAttendingHearing")
                .forState(state)
                .name("Attending the hearing")
                .description("Amend extra support needed for anyone to take part in hearing")
                .showEventNotes()
                .fields()
                    .optional(CaseData::getHearingPreferences);


    }

    private void buildNoticeOfProceedings(State state) {
        event("createNoticeOfProceedings")
        .forState(state)
        .name("Create notice of proceedings")
        .grant("CRU", HMCTS_ADMIN)
        .showSummary()
        .aboutToStartWebhook("notice-of-proceedings")
        .aboutToSubmitWebhook()
        .fields()
            .field("proceedingLabel",  ReadOnly, null, "Text", null, " ")
            .complex(CaseData::getNoticeOfProceedings)
                .complex(NoticeOfProceedings::getJudgeAndLegalAdvisor)
                    .optional(JudgeAndLegalAdvisor::getJudgeTitle)
                    .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                    .optional(JudgeAndLegalAdvisor::getJudgeFullName)
                    .optional(JudgeAndLegalAdvisor::getLegalAdvisorName)
                .done()
                .mandatory(NoticeOfProceedings::getProceedingTypes);
    }

    private EventBuilder<CaseData, UserRole, State> buildUploadC2(State state) {
        return event("uploadC2")
        .forState(state)
        .explicitGrants()
        .grant("CRU", UserRole.CAFCASS, HMCTS_ADMIN, CCD_LASOLICITOR, CCD_SOLICITOR)
        .grantHistoryOnly(LOCAL_AUTHORITY)
        .name("Upload a C2")
        .description("Upload a c2 to the case")
        .aboutToSubmitWebhook()
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
        grantHistory(Open,LOCAL_AUTHORITY);
        event("openCase")
                .initialState(Open)
                .name("Start application")
                .description("Create a new case – add a title")
                .grant("CRU", LOCAL_AUTHORITY)
                .aboutToSubmitWebhook("case-initiation")
                .submittedWebhook()
                .retries(1,2,3,4,5)
                .fields()
                    .optional(CaseData::getCaseName);

        event("ordersNeeded").forState(Open)
                .name("Orders and directions needed")
                .description("Selecting the orders needed for application")
                .aboutToSubmitWebhook("orders-needed")
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getOrders);

        event("hearingNeeded").forState(Open)
                .name("Hearing needed")
                .description("Selecting the hearing needed for application")
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getHearing);

        event("enterChildren").forState(Open)
                .name("Children")
                .description("Entering the children for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .list(CaseData::getChildren1).optional();

        event("enterRespondents").forState(Open)
                .name("Respondents")
                .description("Entering the respondents for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .midEventWebhook()
                    .list(CaseData::getRespondents1).optional();

        event("enterApplicant").forState(Open)
                .name("Applicant")
                .description("Entering the applicant for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .midEventWebhook()
                    .optional(CaseData::getApplicants)
                    .optional(CaseData::getSolicitor);

        event("enterOthers").forState(Open)
                .name("Others to be given notice")
                .description("Entering others for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getOthers);

        event("enterGrounds").forState(Open)
                .name("Grounds for the application")
                .description("Entering the grounds for the application")
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .field("EPO_REASONING_SHOW", Optional, "groundsForEPO CONTAINS \"Workaround to show groundsForEPO. Needs to be hidden from UI\"", "MultiSelectList", "ShowHide", "EPO Reason show or hide")
                    .optional(CaseData::getGroundsForEPO, "EPO_REASONING_SHOW CONTAINS \"SHOW_FIELD\"")
                    .optional(CaseData::getGrounds);

        event("enterRiskHarm").forState(Open)
                .name("Risk and harm to children")
                .description("Entering opinion on risk and harm to children")
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getRisks);

        event("enterParentingFactors").forState(Open)
                .name("Factors affecting parenting")
                .description("Entering the factors affecting parenting")
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getFactorsParenting);

        event("enterInternationalElement").forState(Open)
                .name("International element")
                .description("Entering the international element")
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getInternationalElement);

        event("otherProceedings").forState(Open)
                .name("Other proceedings")
                .description("Entering other proceedings and proposals")
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .midEventWebhook("enter-other-proceedings")
                    .optional(CaseData::getProceeding);

        event("otherProposal").forState(Open)
                .name("Allocation proposal")
                .description("Entering other proceedings and allocation proposals")
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .label("allocationProposal_label", "This should be completed by a solicitor with good knowledge of the case. Use the [President's Guidance](https://www.judiciary.uk/wp-content/uploads/2013/03/President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf) and [schedule](https://www.judiciary.uk/wp-content/uploads/2013/03/Schedule-to-the-President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf) on allocation and gatekeeping to make your recommendation.")
                    .field(CaseData::getAllocationProposal).complex()
                        .mandatory(Allocation::getProposal)
                        .optional(Allocation::getProposalReason);

        event("attendingHearing").forState(Open)
                .name("Attending the hearing")
                .description("Enter extra support needed for anyone to take part in hearing")
                .displayOrder(13)
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getHearingPreferences);

        event("changeCaseName").forState(Open)
                .name("Change case name")
                .description("Change case name")
                .displayOrder(15)
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getCaseName);
    }
}
