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

import static uk.gov.hmcts.reform.fpl.enums.State.*;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.*;

// Found and invoked by the config generator.
// The CaseData type parameter tells the generator which class represents your case model.
public class CCDConfig extends BaseCCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure() {
        caseType("CARE_SUPERVISION_EPO");
        setEnvironment(environment());
        setWebhookConvention(this::webhookConvention);

        role(CCD_SOLICITOR, CCD_LASOLICITOR).has(LOCAL_AUTHORITY);
        role(JUDICIARY, GATEKEEPER).has(HMCTS_ADMIN);
        role(SYSTEM_UPDATE).apiOnly();

        buildUniversalEvents();
        buildTabs();
        buildOpen();
        buildSubmittedEvents();
        buildPrepareForHearing();
        buildGatekeepingEvents();
        buildTransitions();
        buildWorkBasketResultFields();
        buildWorkBasketInputFields();

        // TODO: simplify
        caseField("dateAndTimeSubmitted", null, "DateTime", null, "Date submitted");
        caseField("submittedForm", "Attached PDF", "Document");
        field("cmoToAction").blacklist(HMCTS_ADMIN, GATEKEEPER, CAFCASS, LOCAL_AUTHORITY);
        field("caseManagementOrder").blacklist(HMCTS_ADMIN, GATEKEEPER, JUDICIARY, CAFCASS);
        field("placements").blacklist("R", HMCTS_ADMIN, GATEKEEPER, JUDICIARY, CAFCASS);
        field("placementsWithoutPlacementOrder").blacklist("R", HMCTS_ADMIN, GATEKEEPER, JUDICIARY);
        field("orderBasisLabel").blacklist(JUDICIARY);
        field("respondentsDirectionLabelCMO").blacklist(JUDICIARY);

        field("actionCMOPlaceholderHeading").blacklist("CU", JUDICIARY);
        field("actionCMOPlaceholderHint").blacklist("CU", JUDICIARY);
        field("dateSubmitted").blacklist("R", LOCAL_AUTHORITY);
    }

    private void buildUniversalEvents() {
        event("internal-change:SEND_DOCUMENT")
            .forAllStates()
            .name("Send document")
            .endButtonLabel("")
            .aboutToSubmitWebhook("send-document")
            .explicitGrants()
            .grant("CRU", SYSTEM_UPDATE);

        event("handleSupplementaryEvidence")
            .forAllStates()
            .explicitGrants()
            .name("Handle supplementary evidence")
            .showEventNotes()
            .fields()
            .pageLabel("Bulk Scanning ")
            .field("evidenceHandled").context(DisplayContext.Mandatory);

        event("attachScannedDocs")
            .forAllStates()
            .explicitGrants()
            .endButtonLabel("")
            .name("Attach scanned docs")
            .fields()
            .pageLabel("BulkScanning")
            .field("scannedDocuments").context(DisplayContext.Optional).done()
            .page(2)
            .pageLabel("BulkScanning")
            .field("evidenceHandled").context(DisplayContext.Mandatory);

        event("allocatedJudge")
            .forAllStates()
            .name("Allocated Judge")
            .description("Add allocated judge to a case")
            .grantHistoryOnly(LOCAL_AUTHORITY)
            .grant("CRU", JUDICIARY, HMCTS_ADMIN, GATEKEEPER)
            .grant("R", CAFCASS)
            .fields()
            .page("AllocatedJudge")
                .field(CaseData::getAllocatedJudge);
    }

    private void buildWorkBasketResultFields() {
        workBasketResultFields()
            .field(CaseData::getCaseName, "Case name")
            .field(CaseData::getFamilyManCaseNumber, "FamilyMan case number")
            .field("[STATE]", "State")
            .field(CaseData::getCaseLocalAuthority, "Local authority")
            .field("dateAndTimeSubmitted", "Date submitted");
    }

    private void buildWorkBasketInputFields() {
        workBasketInputFields()
            .field(CaseData::getCaseLocalAuthority, "Local authority")
            .field(CaseData::getCaseName, "Case name")
            .field(CaseData::getFamilyManCaseNumber, "FamilyMan case number")
            .field(CaseData::getDateSubmitted, "Date submitted");
    }

    private void buildTabs() {
        tab("HearingTab", "Hearings")
            .restrictedField(CaseData::getHearingDetails).exclude(CAFCASS)
            .restrictedField(CaseData::getHearing).exclude(LOCAL_AUTHORITY);

        tab("DraftOrdersTab", "Draft orders")
            .exclude(LOCAL_AUTHORITY)
            .showCondition("standardDirectionOrder.orderStatus!=\"SEALED\" OR caseManagementOrder!=\"\" OR sharedDraftCMODocument!=\"\" OR cmoToAction!=\"\"")
            .field(CaseData::getStandardDirectionOrder, "standardDirectionOrder.orderStatus!=\"SEALED\"")
            .field(CaseData::getSharedDraftCMODocument)
            .field(CaseData::getCaseManagementOrder_Judiciary)
            .field(CaseData::getCaseManagementOrder);

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
            .exclude(CAFCASS, LOCAL_AUTHORITY)
            .field("placements")
            .field(CaseData::getPlacements)
            .field("placementsWithoutPlacementOrder");

        tab("SentDocumentsTab", "Documents sent to parties")
            .exclude(CAFCASS, LOCAL_AUTHORITY)
            .field("documentsSentToParties");
    }


    protected String environment() {
        return "production";
    }

    protected String webhookConvention(Webhook webhook, String eventId) {
        eventId = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, eventId);
        String path = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, webhook.toString());
        return "${CCD_DEF_CASE_SERVICE_BASE_URL}/callback/" + eventId + "/" + path;
    }

    private void buildCreateOrderEvent(State state, boolean showSummaryChange) {
        event("createOrder")
                .forState(state)
                .explicitGrants()
                .grant("CRU", HMCTS_ADMIN, JUDICIARY)
                .name("Create an order")
                .showSummary()
                .showSummaryChangeOption(showSummaryChange)
                .allWebhooks("create-order")
                .fields()
                .page("OrderTypeAndDocument")
                    .complex(CaseData::getOrderTypeAndDocument).done()
                    .field("pageShow", DisplayContext.ReadOnly, "orderTypeAndDocument=\"DO_NOT_SHOW\"")
                .page("OrderDateOfIssue")
                    .midEventWebhook("validate-order/date-of-issue")
                    .field("dateOfIssue_label").context(DisplayContext.ReadOnly).label("dateOfIssue_label").done()
                    .field(CaseData::getDateOfIssue).context(DisplayContext.Mandatory).showSummary().done()
                .page("OrderAppliesToAllChildren")
                    .showCondition("pageShow=\"Yes\"")
                    .midEventWebhook("create-order/populate-selector")
                    .field(CaseData::getOrderAppliesToAllChildren).context(DisplayContext.Mandatory).showSummary().done()
                .page("ChildrenSelection")
                    .showCondition("orderAppliesToAllChildren=\"No\"")
                    .midEventWebhook("validate-order/child-selector")
                    .label("children_label", "")
                    .complex(CaseData::getChildSelector).done()
                .page("OrderTitleAndDetails")
                    .showCondition("orderTypeAndDocument.type=\"BLANK_ORDER\"")
                    .complex(CaseData::getOrder).done()
                .page("OrderMonths")
                    .showCondition("orderTypeAndDocument.type=\"SUPERVISION_ORDER\" AND orderTypeAndDocument.subtype=\"FINAL\"")
                    .field(CaseData::getOrderMonths).context(DisplayContext.Mandatory).showSummary().done()
                .page("InterimEndDate")
                    .showCondition("orderTypeAndDocument.subtype=\"INTERIM\"")
                    .midEventWebhook("validate-order/interim-end-date")
                    .field(CaseData::getInterimEndDate).context(DisplayContext.Mandatory).showSummary().done()
                .page("EPOChildren")
                    .showCondition("orderTypeAndDocument.type=\"EMERGENCY_PROTECTION_ORDER\"")
                    .complex(CaseData::getEpoChildren).done()
                .page("EPOType")
                    .showCondition("orderTypeAndDocument.type=\"EMERGENCY_PROTECTION_ORDER\"")
                    .midEventWebhook("validate-order/address")
                    .field(CaseData::getEpoType).context(DisplayContext.Mandatory).showSummary().done()
                    .field(CaseData::getEpoRemovalAddress).context(DisplayContext.Optional).showCondition("epoType=\"PREVENT_REMOVAL\"").showSummary().done()
                .page("EPOPhrase")
                    .showCondition("orderTypeAndDocument.type=\"EMERGENCY_PROTECTION_ORDER\"")
                    .field(CaseData::getEpoPhrase).context(DisplayContext.Mandatory).showSummary().done()
                .page("EPOEndTime")
                    .showCondition("orderTypeAndDocument.type=\"EMERGENCY_PROTECTION_ORDER\"")
                    .midEventWebhook("validate-order/epo-end-date")
                    .field(CaseData::getEpoEndDate).context(DisplayContext.Mandatory).showSummary().done()
                .page("JudgeInformation")
                    .midEventWebhook("create-order/generate-document")
                    .complex(CaseData::getJudgeAndLegalAdvisor)
                        .optional(JudgeAndLegalAdvisor::getJudgeTitle)
                        .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                        .optional(JudgeAndLegalAdvisor::getJudgeFullName)
                        .optional(JudgeAndLegalAdvisor::getLegalAdvisorName).done()
                .page("FurtherDirections")
                    .showCondition("orderTypeAndDocument.type!=\"BLANK_ORDER\"")
                    .midEventWebhook("create-order/generate-document")
                    .field(CaseData::getOrderFurtherDirections).context(DisplayContext.Mandatory).showSummary().done();
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
                    .field("submissionConsentLabel").context(DisplayContext.ReadOnly).type("Text").label(" ").done()
                    .field("submissionConsent").context(DisplayContext.Mandatory).type("MultiSelectList").fieldTypeParameter("Consent").label(" ");

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
                    .field("deletionConsent", DisplayContext.Mandatory, null, "MultiSelectList", "DeletionConsent", " ");

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
                    .field(CaseData::getAllocationDecision, DisplayContext.Mandatory, true);

        addHearingBookingDetails(Gatekeeping)
            .grant("CRU", GATEKEEPER);
        buildSharedEvents(Gatekeeping);
        buildNoticeOfProceedings(Gatekeeping);

        event("draftSDO")
                .forState(Gatekeeping)
                .name("Draft standard directions")
                .allWebhooks("draft-standard-directions")
                .fields()
                    .page("judgeAndLegalAdvisor")
                        .midEventWebhook()
                        .optional(CaseData::getJudgeAndLegalAdvisor)
                    .page("allPartiesDirections")
                        .field("allPartiesHearingDate", DisplayContext.ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getAllParties).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getAllPartiesCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("localAuthorityDirections")
                        .field("localAuthorityDirectionsHearingDate", DisplayContext.ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getLocalAuthorityDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getLocalAuthorityDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("parentsAndRespondentsDirections")
                        .field("respondentDirectionsHearingDate", DisplayContext.ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getRespondentDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getRespondentDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("cafcassDirections")
                        .field("cafcassDirectionsHearingDate", DisplayContext.ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getCafcassDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getCafcassDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("otherPartiesDirections")
                        .field("otherPartiesDirectionsHearingDate", DisplayContext.ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getOtherPartiesDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getOtherPartiesDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("courtDirections")
                        .field("courtDirectionsHearingDate", DisplayContext.ReadOnly, null, "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .immutableList(CaseData::getCourtDirections).complex(Direction.class, this::renderSDODirection)
                        .complex(CaseData::getCourtDirectionsCustom, Direction.class, this::renderSDODirectionsCustom, false)
                    .page("documentReview")
                        .field(CaseData::getStandardDirectionOrder).showSummary(false)
                        .complex()
                        .field(Order::getOrderDoc).context(DisplayContext.ReadOnly).label("Check the order").done()
                        .mandatory(Order::getOrderStatus).done();

        buildStandardDirections(Gatekeeping, "AfterGatekeeping", "");
        buildUploadC2(Gatekeeping, true);
        buildCreateOrderEvent(Gatekeeping, true);
        event("uploadDocumentsAfterGatekeeping")
                .forState(Gatekeeping)
                .name("Documents")
                .description("Only here for backwards compatibility with case history")
                .explicitGrants()
                .grant("R", LOCAL_AUTHORITY, CCD_LASOLICITOR);
        buildLimitedUploadDocuments(Gatekeeping, 11)
            .grant("R", LOCAL_AUTHORITY);
        addStatementOfService(Gatekeeping);
        buildManageRepresentatives(Gatekeeping, false, false);
        buildPlacement(Gatekeeping);
    }

    private void renderSDODirectionsCustom(FieldCollection.FieldCollectionBuilder<Direction,?> f)  {
        f.optional(Direction::getDirectionType)
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
                    .field("standardDirectionsDocument", DisplayContext.Optional, null, "Document", null, "Upload a file")
                    .field("otherCourtAdminDocuments", DisplayContext.Optional, null, "Collection", "CourtAdminDocument", "Other documents");
    }

    private void buildSubmittedEvents() {
        grant(Submitted, "CRU", HMCTS_ADMIN);
        event("addFamilyManCaseNumber")
                .forState(Submitted)
                .name("Add case number")
                .aboutToSubmitWebhook("add-case-number")
                .submittedWebhook()
                .fields()
                    .optional(CaseData::getFamilyManCaseNumber);

        addHearingBookingDetails(Submitted)
            .grant("CRU", JUDICIARY, GATEKEEPER)
            .aboutToSubmitWebhook();
        this.buildStandardDirections(Submitted, "", "Save and continue");
        buildUploadC2(Submitted, true);

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

        addStatementOfService(Submitted);

        buildCreateOrderEvent(Submitted, false);

        event("uploadDocumentsAfterSubmission")
                .forState(Submitted)
                .explicitGrants()
                .grant("R", LOCAL_AUTHORITY, CCD_LASOLICITOR)
                .name("Documents")
                .description("Only here for backwards compatibility with case history");

        buildLimitedUploadDocuments(Submitted, 15)
            .grant("R", LOCAL_AUTHORITY);
        buildManageRepresentatives(Submitted, true, true);
        buildPlacement(Submitted);
    }

    private void addStatementOfService(State state) {
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
                    .field(CaseData::getStatementOfService).context(DisplayContext.Mandatory).showSummary(true).mutable().done()
                    .field("serviceDeclarationLabel", DisplayContext.ReadOnly, null, "Text", null, "Declaration" )
                    .field("serviceConsent", DisplayContext.Mandatory, null, "MultiSelectList", "Consent", " ");
    }

    private void buildPrepareForHearing() {
        prefix(PREPARE_FOR_HEARING, "-");
        blacklist(PREPARE_FOR_HEARING, GATEKEEPER);
        grant(PREPARE_FOR_HEARING, "CRU", HMCTS_ADMIN);
        addHearingBookingDetails(PREPARE_FOR_HEARING);
        buildSharedEvents( PREPARE_FOR_HEARING);

        event("uploadOtherCourtAdminDocuments-PREPARE_FOR_HEARING")
            .forState(PREPARE_FOR_HEARING)
            .name("Documents")
            .description("Upload documents")
            .grant("CRU", HMCTS_ADMIN)
            .fields()
            .field("otherCourtAdminDocuments", DisplayContext.Optional, null, "Collection", "CourtAdminDocument", "Other documents");

        buildLimitedUploadDocuments(PREPARE_FOR_HEARING, 8)
            .grant("CRU", CCD_SOLICITOR);

        buildUploadC2(PREPARE_FOR_HEARING, false);
        buildNoticeOfProceedings( PREPARE_FOR_HEARING);

        buildCreateOrderEvent(PREPARE_FOR_HEARING, false);

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
                    .field("cmoDraftInReviewHeading").context(DisplayContext.ReadOnly).done()
                    .field("cmoDraftInReviewHint").context(DisplayContext.ReadOnly).done()
                    .field("cmoToAction").context(DisplayContext.ReadOnly).showCondition("cmoDraftInReviewHeading=\"DO NOT SHOW\"").done()
                .page("hearingDate")
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                    .field("cmoHearingDateList", DisplayContext.Mandatory, null, "DynamicList", null, "Which hearing is this order for?")
                .page("allPartiesDirections")
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                    .label("allPartiesLabelCMO", "## For all parties")
                .field("allPartiesPrecedentLabelCMO").context(DisplayContext.ReadOnly).fieldTypeParameter("Direction").label("Add completed directions from the precedent library or your own template.").readOnly().done()
                .field(CaseData::getAllPartiesCustomCMO).mutable().complex(Direction.class, this::renderDirection)
                .page("localAuthorityDirections")
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                     .label("localAuthorityDirectionsLabelCMO", "## For the local authority")
                     .field(CaseData::getLocalAuthorityDirectionsCustomCMO).mutable().complex(Direction.class, this::renderDirection)
                .page(2)
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                     .label("respondentsDirectionLabelCMO", "## For the parents or respondents")
                     .field("respondents_label", DisplayContext.ReadOnly, null, "TextArea", null, " ")
                     .field(CaseData::getRespondentDirectionsCustomCMO).mutable().complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getParentsAndRespondentsAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("cafcassDirections")
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                     .label("cafcassDirectionsLabelCMO", "## For Cafcass")
                     .field(CaseData::getCafcassDirectionsCustomCMO).mutable().complex(Direction.class, this::renderDirection)
                .page(3)
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                     .label("otherPartiesDirectionLabelCMO", "## For other parties")
                     .field("others_label", DisplayContext.ReadOnly, null, "TextArea", null, " ")
                     .field(CaseData::getOtherPartiesDirectionsCustomCMO).mutable().complex(Direction.class)
                    .done()
                .page("courtDirections")
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                     .label("courtDirectionsLabelCMO", "## For the court")
                     .field(CaseData::getCourtDirectionsCustomCMO).mutable().complex(Direction.class, this::renderDirection)
                .page(5)
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                     .label("orderBasisLabel", "## Basis of order")
                     .label("addRecitalLabel", "### Add recital")
                     .field("recitals").context(DisplayContext.Optional).type("Collection").fieldTypeParameter("Recitals").label("Recitals").mutable().done()
                .page("schedule")
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"") .field("schedule", DisplayContext.Mandatory, null, "Schedule", null, "Schedule")
                    .midEventWebhook()
                    .field("schedule", DisplayContext.Mandatory, null, "Schedule", null, "Schedule")
            .page("documentReview")
                .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                .field(CaseData::getCaseManagementOrder).context(DisplayContext.Complex).done();


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
                    .showCondition("cmoToAction=\"\" OR cmoToAction.status!=\"SEND_TO_JUDGE\"")
                    .field("actionCMOPlaceholderHeading").context(DisplayContext.ReadOnly).done()
                    .field("actionCMOPlaceholderHint").context(DisplayContext.ReadOnly).done()
                .page("NOT_SHOWN").previousPage()
                    .showCondition("actionCMOPlaceholderHeading=\"DO_NOT_SHOW\"")
                    .field("cmoToAction").context(DisplayContext.Optional).done()
                .page("allPartiesDirections")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"")
                    .label("allPartiesLabelCMO", "## For all parties")
                    .field("allPartiesPrecedentLabelCMO").context(DisplayContext.ReadOnly).fieldTypeParameter("Direction").label("Add completed directions from the precedent library or your own template.").readOnly().done()
                    .field(CaseData::getAllPartiesCustomCMO).mutable().complex(Direction.class, this::renderDirection)
                .page("localAuthorityDirections")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"")
                     .label("localAuthorityDirectionsLabelCMO", "## For the local authority")
                     .field(CaseData::getLocalAuthorityDirectionsCustomCMO).mutable().complex(Direction.class, this::renderDirection)
                .page("respondentsDirections")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"")
                     .label("respondentsDirectionLabelCMO", "## For the parents or respondents")
                     .field("respondents_label", DisplayContext.ReadOnly, null, "TextArea", null, " ")
                     .field(CaseData::getRespondentDirectionsCustomCMO).mutable().complex(Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getParentsAndRespondentsAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("cafcassDirections")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"")
                     .label("cafcassDirectionsLabelCMO", "## For Cafcass")
                     .field(CaseData::getCafcassDirectionsCustomCMO).mutable().complex(Direction.class, this::renderDirection)
                .page("otherPartiesDirections")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"")
                     .label("otherPartiesDirectionLabelCMO", "## For other parties")
                     .field("others_label", DisplayContext.ReadOnly, null, "TextArea", null, " ")
                     .field(CaseData::getOtherPartiesDirectionsCustomCMO).mutable().complex(Direction.class)
                    .done()
                .page("courtDirections")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"")
                     .label("courtDirectionsLabelCMO", "## For the court")
                     .field(CaseData::getCourtDirectionsCustomCMO).mutable().complex(Direction.class, this::renderDirection)
                .page("recitals")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"")
                     .label("orderBasisLabel", "## Basis of order")
                     .label("addRecitalLabel", "### Add recital")
                     .field("recitals").context(DisplayContext.Optional).type("Collection").fieldTypeParameter("Recitals").label("Recitals").mutable().done()
                .page("schedule")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"") .field("schedule", DisplayContext.Mandatory, null, "Schedule", null, "Schedule")
                    .midEventWebhook()
                    .field("schedule", DisplayContext.Mandatory, null, "Schedule", null, "Schedule")
                .page("OrderActionDocumentReview")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\"")
                    .field(CaseData::getOrderAction).context(DisplayContext.Complex).done()
                .page("nextHearing")
                    .showCondition("cmoToAction.status=\"SEND_TO_JUDGE\" AND orderAction.type=\"SEND_TO_ALL_PARTIES\"")
                    .label("nextHearingDateHeading", "## Basis of order")
                    .label("nextHearingDateHintText", "### Add recital")
                    .field(CaseData::getNextHearingDateList).context(DisplayContext.Mandatory).done();

        addStatementOfService(PREPARE_FOR_HEARING);
        buildManageRepresentatives(PREPARE_FOR_HEARING, true, false);

        renderComply( "COMPLY_LOCAL_AUTHORITY", LOCAL_AUTHORITY, CaseData::getLocalAuthorityDirections, DisplayContext.Mandatory, "Allows Local Authority user access to comply with their directions as well as ones for all parties");
        renderComply( "COMPLY_CAFCASS", UserRole.CAFCASS, CaseData::getCafcassDirections, DisplayContext.Optional, "Allows Cafcass user access to comply with their directions as well as ones for all parties");
        renderComply( "COMPLY_COURT", HMCTS_ADMIN, CaseData::getCourtDirectionsCustom, DisplayContext.Optional, "Event gives Court user access to comply with their directions as well as all parties");

        // TODO: duplicate of renderComply
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
                .field(CaseData::getRespondentDirectionsCustom).done()
            .page(2)
                .label("others_label", "label")
                .field(CaseData::getOtherPartiesDirectionsCustom);

        // TODO: duplicate of renderComply
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
                .field("localAuthorityDirectionsLabelCMO").type("Label").label("## For the local authority").context(DisplayContext.ReadOnly).blacklist(HMCTS_ADMIN).done()
                .label("respondents_label", "## For the local authority")
                .field(CaseData::getRespondentDirectionsCustom).caseEventFieldLabel("Direction").complex().done()
            .page("Other party directions")
                .field("otherPartiesDirectionLabelCMO").type("Label").label("## For the local authority").context(DisplayContext.ReadOnly).blacklist(HMCTS_ADMIN).done()
                .label("others_label", "## For the local authority")
                .field(CaseData::getOtherPartiesDirectionsCustom).caseEventFieldLabel("Direction").complex().done()
            .page("Cafcass directions")
                .field("cafcassDirectionsLabelCMO").type("Label").label("## For the local authority").context(DisplayContext.ReadOnly).blacklist(HMCTS_ADMIN).done()
                .field(CaseData::getCafcassDirectionsCustom).caseEventFieldLabel("Direction").complex().done();

        buildPlacement(PREPARE_FOR_HEARING);
        event("internal-change:CMO_PROGRESSION")
            .forState(PREPARE_FOR_HEARING)
            .name("-")
            .endButtonLabel("")
            .explicitGrants()
            .grant("CRU", SYSTEM_UPDATE)
            .aboutToSubmitWebhook("cmo-progression");
    }

    @SuppressWarnings("unchecked")
    private EventBuilder<CaseData, UserRole, State> buildLimitedUploadDocuments(State state, int displayOrder) {
        return (EventBuilder<CaseData, UserRole, State>) event("limitedUploadDocuments")
            .forState(state)
            .name("Documents")
            .description("Upload documents")
            .displayOrder(displayOrder)
            .explicitGrants()
            .grant("CRU", CCD_SOLICITOR)
            .fields()
            .field("otherCourtAdminDocuments", DisplayContext.Optional, null, "Collection",
                "CourtAdminDocument", "Other documents")
            .eventBuilder();
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
                .field("singleChild").showCondition("childrenList=\"DO NOT SHOW\"").context(DisplayContext.ReadOnly).done()
                .field("childrenList").context(DisplayContext.Mandatory).done()
            .page("placement")
                .pageLabel("Application and supporting documents")
                .field("placementChildName").showCondition("childrenList=\"DO NOT SHOW\"").context(DisplayContext.Mandatory).done()
                .field("placementLabel").context(DisplayContext.ReadOnly).done()
                .field("placement").done();

    }

    private void buildManageRepresentatives(State state, boolean submittedWebhook,
        boolean showSummaryChange) {
        event("manageRepresentatives")
            .forState(state)
            .name("Manage representatives")
            .showSummaryChangeOption(showSummaryChange)
            .aboutToStartWebhook()
            .aboutToSubmitWebhook()
            .submittedWebhook(submittedWebhook)
            .explicitGrants()
            .grant("CRU", HMCTS_ADMIN)
            .fields()
                .midEventWebhook()
                .label("respondents_label", "label")
                .label("others_label", "others")
                .optional(CaseData::getRepresentatives);
    }

    private void renderComply(String eventId, UserRole role, TypedPropertyGetter<CaseData, ?> getter, DisplayContext reasonContext, String description) {
        event(eventId)
                .forState(PREPARE_FOR_HEARING)
                .explicitGrants()
                .grant("CRU", role)
                .name("Comply with directions")
                .description(description)
                .displayOrder(11)
                .aboutToStartWebhook("comply-with-directions")
                .aboutToSubmitWebhook()
                .fields()
                .field(getter).caseEventFieldLabel("Direction").mutable().complex(Direction.class)
                    .readonly(Direction::getDirectionType)
                    .readonly(Direction::getDirectionNeeded, "directionText = \"DO_NOT_SHOW\"")
                    .field(Direction::getDateToBeCompletedBy).context(DisplayContext.ReadOnly).label("Deadline").done()
                    .complex(Direction::getResponse)
                        .optional(DirectionResponse::getComplied)
                        .optional(DirectionResponse::getDocumentDetails)
                        .optional(DirectionResponse::getFile)
                        .field("cannotComplyTitle").context(DisplayContext.Optional).done()
                        .field(DirectionResponse::getCannotComplyReason, reasonContext)
                        .optional(DirectionResponse::getC2Uploaded)
                        .optional(DirectionResponse::getCannotComplyFile);
    }

    private void renderDirection(FieldCollection.FieldCollectionBuilder<Direction, ?> f) {
        f.optional(Direction::getDirectionType)
                .mandatory(Direction::getDirectionText)
                .optional(Direction::getDateToBeCompletedBy);
    }

    @SuppressWarnings("unchecked")
    private EventBuilder<CaseData, UserRole, State> addHearingBookingDetails(State state) {
        return (EventBuilder<CaseData, UserRole, State>) event( "hearingBookingDetails")
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
            .eventBuilder();
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
            .field("proceedingLabel",  DisplayContext.ReadOnly, null, "Text", null, " ")
            .complex(CaseData::getNoticeOfProceedings)
                .complex(NoticeOfProceedings::getJudgeAndLegalAdvisor)
                    .optional(JudgeAndLegalAdvisor::getJudgeTitle)
                    .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                    .optional(JudgeAndLegalAdvisor::getJudgeFullName)
                    .optional(JudgeAndLegalAdvisor::getLegalAdvisorName)
                .done()
                .mandatory(NoticeOfProceedings::getProceedingTypes);
    }

    private void buildUploadC2(State state, boolean withSubmittedWebhook) {
        event("uploadC2")
        .forState(state)
        .explicitGrants()
        .grant("CRU", UserRole.CAFCASS, HMCTS_ADMIN, CCD_LASOLICITOR, CCD_SOLICITOR)
        .grantHistoryOnly(LOCAL_AUTHORITY)
        .name("Upload a C2")
        .description("Upload a c2 to the case")
        .aboutToSubmitWebhook()
        .submittedWebhook(withSubmittedWebhook)
        .fields()
            .page(2)
            .field(CaseData::getTemporaryC2Document).complex()
                .mandatory(C2DocumentBundle::getDocument)
                .mandatory(C2DocumentBundle::getDescription);
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
                    .field(CaseData::getChildren1).context(DisplayContext.Optional).mutable();

        event("enterRespondents").forState(Open)
                .name("Respondents")
                .description("Entering the respondents for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .midEventWebhook()
                    .field(CaseData::getRespondents1).context(DisplayContext.Optional).mutable();

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
                    .field("EPO_REASONING_SHOW", DisplayContext.Optional, "groundsForEPO CONTAINS \"Workaround to show groundsForEPO. Needs to be hidden from UI\"", "MultiSelectList", "ShowHide", "EPO Reason show or hide")
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
                    .field(CaseData::getAllocationProposal).context(DisplayContext.Complex);

        event("attendingHearing").forState(Open)
                .name("Attending the hearing")
                .description("Enter extra support needed for anyone to take part in hearing")
                .displayOrder(13)
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getHearingPreferences);

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
                    .field("[STATE]", DisplayContext.ReadOnly, "courtBundle = \"DO_NOT_SHOW\"")
                    .field("courtBundle", DisplayContext.Optional, "[STATE] != \"Open\"", "CourtBundle", null, "8. Court bundle")
                    .label("documents_socialWorkOther_border_top", "-------------------------------------------------------------------------------------------------------------")
                    .field(CaseData::getOtherSocialWorkDocuments).context(DisplayContext.Optional).mutable().done()
                    .label("documents_socialWorkOther_border_bottom", "-------------------------------------------------------------------------------------------------------------");

        event("changeCaseName").forState(Open)
                .name("Change case name")
                .description("Change case name")
                .displayOrder(15)
                .grant("CRU", CCD_LASOLICITOR)
                .fields()
                    .optional(CaseData::getCaseName);
    }
}
