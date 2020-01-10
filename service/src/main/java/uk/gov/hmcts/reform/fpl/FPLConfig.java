package uk.gov.hmcts.reform.fpl;


import ccd.sdk.types.*;
import de.cronn.reflection.util.TypedPropertyGetter;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.*;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;


import static uk.gov.hmcts.reform.fpl.enums.State.*;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.*;

// Found and invoked by the config generator.
// The CaseData type parameter tells the generator which class represents your case model.
public class FPLConfig extends BaseCCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure() {
        caseType("CARE_SUPERVISION_EPO");

        buildOpenEvents();
        buildSubmittedEvents();
        buildPrepareForHearingEvents();
        buildGatekeepingEvents();
        buildStateTransitions();
    }

    private void buildSharedEvents(State forState) {
        event("amendChildren")
            .forState(forState)
            .name("Children")
            .description("Amending the children for the case")
            .aboutToStartWebhook("enter-children")
            .aboutToSubmitWebhook()
            .showEventNotes()
            .fields()
            .optional(CaseData::getChildren1);
        event("amendRespondents")
            .forState(forState)
            .name("Respondents")
            .description("Amending the respondents for the case")
            .aboutToStartWebhook("enter-respondents")
            .aboutToSubmitWebhook()
            .showEventNotes()
            .fields()
            .optional(CaseData::getRespondents1);
        event("amendOthers")
            .forState(forState)
            .name("Others to be given notice")
            .description("Amending others for the case")
            .showEventNotes()
            .fields()
            .optional(CaseData::getOthers);
        event("amendInternationalElement")
            .forState(forState)
            .name("International element")
            .description("Amending the international element")
            .showEventNotes()
            .fields()
            .optional(CaseData::getInternationalElement);
        event("amendOtherProceedings")
            .forState(forState)
            .name("Other proceedings")
            .description("Amending other proceedings and allocation proposals")
            .midEventURL("/enter-other-proceedings/mid-event")
            .showEventNotes()
            .fields()
            .optional(CaseData::getProceeding);
        event("amendAttendingHearing")
            .forState(forState)
            .name("Attending the hearing")
            .description("Amend extra support needed for anyone to take part in hearing")
            .showEventNotes()
            .fields()
            .optional(CaseData::getHearingPreferences);
    }

    private void buildStateTransitions() {
        event("submitApplication")
            .forStateTransition(Open, Submitted)
            .name("Submit application")
            .displayOrder(17) // TODO - necessary?
                .explicitGrants()
                .grant("R", HMCTS_ADMIN, UserRole.CAFCASS, UserRole.JUDICIARY, UserRole.GATEKEEPER)
                .grant("CRU", LOCAL_AUTHORITY)
                .endButtonLabel("Submit")
                .allWebhooks("case-submission")
                .midEventWebhook()
                .retries(1,2,3,4,5)
                .fields()
                    .field("submissionConsentLabel", DisplayContext.ReadOnly, null, "Text", null, " ")
                    .field("submissionConsent", DisplayContext.Mandatory, null, "MultiSelectList", "Consent", " ");

        event("populateSDO")
                .forStateTransition(Submitted, Gatekeeping)
                .name("Populate standard directions")
                .displayOrder(14) // TODO - necessary?
                .explicitGrants()
                .grant("C", UserRole.SYSTEM_UPDATE)
                .fields()
                    .pageLabel(" ")
                    .optional(CaseData::getAllParties)
                    .optional(CaseData::getLocalAuthorityDirections)
                    .optional(CaseData::getRespondentDirections)
                    .optional(CaseData::getCafcassDirections)
                    .optional(CaseData::getOtherPartiesDirections)
                    .optional(CaseData::getCourtDirections);

        event("deleteApplication")
                .forStateTransition(Open, Deleted)
                .displayOrder(18) // TODO - necessary?
                .grant("CRU", LOCAL_AUTHORITY)
                .name("Delete an application")
                .aboutToSubmitURL("/case-deletion/about-to-submit")
                .endButtonLabel("Delete application")
                .fields()
                    .field("deletionConsent", DisplayContext.Mandatory, null, "MultiSelectList", "DeletionConsent", " ");
    }

    private void buildGatekeepingEvents() {
        grant(Gatekeeping, "CRU", GATEKEEPER);
        event("otherAllocationDecision")
                .forState(Gatekeeping)
                .name("Allocation decision")
                .description("Entering other proceedings and allocation proposals")
                .showSummary()
                .aboutToStartWebhook("allocation-decision")
                .aboutToSubmitWebhook()
                .retries(1,2,3,4,5)
                .fields()
                    .field(CaseData::getAllocationDecision, DisplayContext.Mandatory, true);

        addHearingBookingDetails( Gatekeeping);
        buildSharedEvents( Gatekeeping);
        buildNoticeOfProceedings( Gatekeeping);

        event("draftSDO")
                .forState(Gatekeeping)
                .name("Draft standard directions")
                .allWebhooks("draft-standard-directions")
                .midEventWebhook()
                .fields()
                    .page("judgeAndLegalAdvisor")
                        .optional(CaseData::getJudgeAndLegalAdvisor)
                    .page("allPartiesDirections")
                        .readonly(CaseData::getHearingDetails, "hearingDate=\"DO_NOT_SHOW\"")
                        .field("hearingDate", DisplayContext.ReadOnly, "hearingDetails.startDate=\"\"", "Label", null, "The next hearing is on ${hearingDetails.startDate}.")
                        .complex(CaseData::getAllParties, Direction.class, this::renderSDODirection)
                        .complex(CaseData::getAllPartiesCustom, Direction.class, this::renderSDODirectionsCustom)
                    .page("localAuthorityDirections")
                        .complex(CaseData::getLocalAuthorityDirections, Direction.class, this::renderSDODirection)
                        .complex(CaseData::getLocalAuthorityDirectionsCustom, Direction.class, this::renderSDODirectionsCustom)
                    .page("parentsAndRespondentsDirections")
                        .complex(CaseData::getRespondentDirections, Direction.class, this::renderSDODirection)
                        .complex(CaseData::getRespondentDirectionsCustom, Direction.class, this::renderSDODirectionsCustom)
                    .page("cafcassDirections")
                        .complex(CaseData::getCafcassDirections, Direction.class, this::renderSDODirection)
                        .complex(CaseData::getCafcassDirectionsCustom, Direction.class, this::renderSDODirectionsCustom)
                    .page("otherPartiesDirections")
                        .complex(CaseData::getOtherPartiesDirections, Direction.class, this::renderSDODirection)
                        .complex(CaseData::getOtherPartiesDirectionsCustom, Direction.class, this::renderSDODirectionsCustom)
                    .page("courtDirections")
                        .complex(CaseData::getCourtDirections, Direction.class, this::renderSDODirection)
                        .complex(CaseData::getCourtDirectionsCustom, Direction.class, this::renderSDODirectionsCustom)
                    .page("documentReview")
                        .complex(CaseData::getStandardDirectionOrder)
                            .field().id(Order::getOrderDoc).context(DisplayContext.ReadOnly).label("Check the order").done()
                            .mandatory(Order::getOrderStatus);

        buildStandardDirections( Gatekeeping, "AfterGatekeeping");
        buildUploadC2( Gatekeeping);
        buildC21Event( Gatekeeping);
        event("uploadDocumentsAfterGatekeeping")
                .forState(Gatekeeping)
                .name("Documents")
                .description("Only here for backwards compatibility with case history")
                .explicitGrants()
                .grant("R", LOCAL_AUTHORITY);
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

    private void buildStandardDirections(State state, String suffix) {
        event("uploadStandardDirections" + suffix)
                .forState(state)
                .name("Documents")
                .description("Upload standard directions")
                .explicitGrants()
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
                .fields()
                    .optional(CaseData::getFamilyManCaseNumber);

        addHearingBookingDetails( Submitted);
        this.buildStandardDirections( Submitted, "");
        buildUploadC2( Submitted);

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
        buildSharedEvents( Submitted);
        buildNoticeOfProceedings( Submitted);

        event("addStatementOfService")
                .forState(Submitted)
                .explicitGrants()
                .grant("CRU", LOCAL_AUTHORITY)
                .name("Add statement of service (c9)")
                .description("Add statement of service")
                .showSummary()
                .aboutToStartWebhook("statement-of-service")
                .fields()
                    .label("c9Declaration", "If you send documents to a party's solicitor or a children's guardian, give their details")
                    .field(CaseData::getStatementOfService, DisplayContext.Mandatory, true)
                    .field("serviceDeclarationLabel", DisplayContext.ReadOnly, null, "Text", null, "Declaration" )
                    .field("serviceConsent", DisplayContext.Mandatory, null, "MultiSelectList", "Consent", " ");

        buildC21Event( Submitted);

        event("uploadDocumentsAfterSubmission")
                .forState(Submitted)
                .explicitGrants()
                .grant("R", LOCAL_AUTHORITY)
                .name("Documents")
                .description("Only here for backwards compatibility with case history");

    }

    private void buildPrepareForHearingEvents() {
        prefix(PREPARE_FOR_HEARING, "-");
        blacklist(PREPARE_FOR_HEARING, GATEKEEPER);
        grant(PREPARE_FOR_HEARING, "CRU", HMCTS_ADMIN);
        addHearingBookingDetails(PREPARE_FOR_HEARING);
        buildSharedEvents(PREPARE_FOR_HEARING);

        event("uploadOtherCourtAdminDocuments-PREPARE_FOR_HEARING")
                .forState(PREPARE_FOR_HEARING)
                .name("Documents")
                .description("Upload documents")
                .grant("CRU", HMCTS_ADMIN)
                .fields()
                    .field("otherCourtAdminDocuments", DisplayContext.Optional, null, "Collection", "CourtAdminDocument", "Other documents");
        buildUploadC2( PREPARE_FOR_HEARING);
        buildNoticeOfProceedings( PREPARE_FOR_HEARING);

        event("draftCMO")
                .forState(PREPARE_FOR_HEARING)
                .explicitGrants()
                .grant("CRU", LOCAL_AUTHORITY)
                .name("Draft CMO")
                .description("Draft Case Management Order")
                .displayOrder(1)
                .aboutToStartWebhook("draft-cmo")
                .aboutToSubmitWebhook()
                .fields()
                .page("hearingDate")
                    .field("cmoHearingDateList", DisplayContext.Mandatory, null, "DynamicList", null, "Which hearing is this order for?")
                .page("allPartiesDirections")
                    .label("allPartiesLabelCMO", "## For all parties")
                    .field("allPartiesPrecedentLabelCMO", DisplayContext.ReadOnly, null, null, "Direction", "Add completed directions from the precedent library or your own template.")
                    .complex(CaseData::getAllPartiesCustom, Direction.class, this::renderDirection)
                .page("localAuthorityDirections")
                     .label("localAuthorityDirectionsLabelCMO", "## For the local authority")
                     .complex(CaseData::getLocalAuthorityDirectionsCustom, Direction.class, this::renderDirection)
                .page(2)
                     .label("respondentsDirectionLabelCMO", "## For the parents or respondents")
                     .field("respondentsDropdownLabelCMO", DisplayContext.ReadOnly, null, "TextArea", null, " ")
                     .complex(CaseData::getRespondentDirectionsCustom, Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getParentsAndRespondentsAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("cafcassDirections")
                     .label("cafcassDirectionsLabelCMO", "## For Cafcass")
                     .complex(CaseData::getCafcassDirectionsCustom, Direction.class, this::renderDirection)
                    .complex(CaseData::getCaseManagementOrder)
                        .readonly(CaseManagementOrder::getHearingDate)
                    .done()
                .page(3)
                     .label("otherPartiesDirectionLabelCMO", "## For other parties")
                     .field("otherPartiesDropdownLabelCMO", DisplayContext.ReadOnly, null, "TextArea", null, " ")
                     .complex(CaseData::getOtherPartiesDirectionsCustom, Direction.class)
                        .optional(Direction::getDirectionType)
                        .mandatory(Direction::getDirectionText)
                        .mandatory(Direction::getOtherPartiesAssignee)
                        .optional(Direction::getDateToBeCompletedBy)
                    .done()
                .page("courtDirections")
                     .label("courtDirectionsLabelCMO", "## For the court")
                     .complex(CaseData::getCourtDirectionsCustom, Direction.class, this::renderDirection)
                .page(5)
                     .label("orderBasisLabel", "## Basis of order")
                     .label("addRecitalLabel", "### Add recital")
                     .field("recitals", DisplayContext.Optional, null, "Collection", "Recitals", "Recitals")
                .page("schedule")
                     .field("schedule", DisplayContext.Mandatory, null, "Schedule", null, "Schedule");

        renderComply( "COMPLY_LOCAL_AUTHORITY", LOCAL_AUTHORITY, CaseData::getLocalAuthorityDirections, DisplayContext.Mandatory, "Allows Local Authority user access to comply with their directions as well as ones for all parties");
        renderComply( "COMPLY_CAFCASS", UserRole.CAFCASS, CaseData::getCafcassDirections, DisplayContext.Optional, "Allows Cafcass user access to comply with their directions as well as ones for all parties");
        renderComply( "COMPLY_COURT", HMCTS_ADMIN, CaseData::getCourtDirectionsCustom, DisplayContext.Optional, "Event gives Court user access to comply with their directions as well as all parties");
        explicitState("uploadC2-PREPARE_FOR_HEARING", LOCAL_AUTHORITY, "");
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
                .pageLabel(" ")
                .complex(getter, Direction.class)
                    .readonly(Direction::getDirectionType)
                    .readonly(Direction::getDirectionNeeded, "directionText = \"DO_NOT_SHOW\"")
                    .readonly(Direction::getDateToBeCompletedBy)
                    .complex(Direction::getResponse)
                        .optional(DirectionResponse::getComplied)
                        .optional(DirectionResponse::getDocumentDetails)
                        .optional(DirectionResponse::getFile)
                        .label("cannotComplyTitle", "TODO")
                        .field(DirectionResponse::getCannotComplyReason, reasonContext)
                        .optional(DirectionResponse::getC2Uploaded)
                        .optional(DirectionResponse::getCannotComplyFile);
    }

    private void renderDirection(FieldCollection.FieldCollectionBuilder<Direction, ?> f) {
        f.optional(Direction::getDirectionType)
                .mandatory(Direction::getDirectionText)
                .optional(Direction::getDateToBeCompletedBy);
    }

    private void addHearingBookingDetails(State state) {
        event("hearingBookingDetails")
                .forState(state)
                .grant("CRU", UserRole.GATEKEEPER)
                .name("Add hearing details")
                .description("Add hearing booking details to a case")
                .aboutToStartWebhook("add-hearing-bookings")
                .midEventURL("/add-hearing-bookings/mid-event")
                .showSummary()
                .fields()
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
                        .optional(JudgeAndLegalAdvisor::getLegalAdvisorName);
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

    private void buildUploadC2(State state) {
        event("uploadC2")
        .forState(state)
        .explicitGrants()
        .grant("CRU", UserRole.LOCAL_AUTHORITY, UserRole.CAFCASS, HMCTS_ADMIN)
        .name("Upload a C2")
        .description("Upload a c2 to the case")
        .aboutToSubmitWebhook()
        .submittedWebhook()
        .midEventWebhook()
        .fields()
            .complex(CaseData::getTemporaryC2Document)
            .mandatory(C2DocumentBundle::getDocument)
            .mandatory(C2DocumentBundle::getDescription);
    }

    private void buildOpenEvents() {
        grant(Open, "CRU", LOCAL_AUTHORITY);
        event("openCase")
                .initialState(Open)
                .name("Start application")
                .description("Create a new case – add a title")
                .aboutToSubmitURL("/case-initiation/about-to-submit")
                .submittedURL("/case-initiation/submitted")
                .retries(1,2,3,4,5)
                .fields()
                    .optional(CaseData::getCaseName);

        event("ordersNeeded").forState(Open)
                .name("Orders and directions needed")
                .description("Selecting the orders needed for application")
                .aboutToSubmitURL("/orders-needed/about-to-submit")
                .fields()
                    .optional(CaseData::getOrders);

        event("hearingNeeded").forState(Open)
                .name("Hearing needed")
                .description("Selecting the hearing needed for application")
                .fields()
                    .optional(CaseData::getHearing);

        event("enterChildren").forState(Open)
                .name("Children")
                .description("Entering the children for the case")
                .aboutToStartURL("/enter-children/about-to-start")
                .aboutToSubmitURL("/enter-children/about-to-submit")
                .fields()
                    .optional(CaseData::getChildren1);

        event("enterRespondents").forState(Open)
                .name("Respondents")
                .description("Entering the respondents for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .midEventWebhook()
                .fields()
                    .optional(CaseData::getRespondents1);

        event("enterApplicant").forState(Open)
                .name("Applicant")
                .description("Entering the applicant for the case")
                .aboutToStartWebhook()
                .aboutToSubmitWebhook()
                .midEventWebhook()
                .fields()
                    .optional(CaseData::getApplicants)
                    .optional(CaseData::getSolicitor);

        event("enterOthers").forState(Open)
                .name("Others to be given notice")
                .description("Entering others for the case")
                .fields()
                    .optional(CaseData::getOthers);

        event("enterGrounds").forState(Open)
                .name("Grounds for the application")
                .description("Entering the grounds for the application")
                .fields()
                    .field("EPO_REASONING_SHOW", DisplayContext.Optional, "groundsForEPO CONTAINS \"Workaround to show groundsForEPO. Needs to be hidden from UI\"", "MultiSelectList", "ShowHide", "EPO Reason show or hide")
                    .optional(CaseData::getGroundsForEPO, "EPO_REASONING_SHOW CONTAINS \"SHOW_FIELD\"")
                    .optional(CaseData::getGrounds);

        event("enterRiskHarm").forState(Open)
                .name("Risk and harm to children")
                .description("Entering opinion on risk and harm to children")
                .fields()
                    .optional(CaseData::getRisks);

        event("enterParentingFactors").forState(Open)
                .name("Factors affecting parenting")
                .description("Entering the factors affecting parenting")
                .grant("CRU", LOCAL_AUTHORITY)
                .fields()
                    .optional(CaseData::getFactorsParenting);

        event("enterInternationalElement").forState(Open)
                .name("International element")
                .description("Entering the international element")
                .fields()
                    .optional(CaseData::getInternationalElement);

        event("otherProceedings").forState(Open)
                .name("Other proceedings")
                .description("Entering other proceedings and proposals")
                .midEventURL("/enter-other-proceedings/mid-event")
                .fields()
                    .optional(CaseData::getProceeding);

        event("otherProposal").forState(Open)
                .name("Allocation proposal")
                .grant("CRU", GATEKEEPER)
                .description("Entering other proceedings and allocation proposals")
                .fields()
                    .label("allocationProposal_label", "This should be completed by a solicitor with good knowledge of the case. Use the [President's Guidance](https://www.judiciary.uk/wp-content/uploads/2013/03/President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf) and [schedule](https://www.judiciary.uk/wp-content/uploads/2013/03/Schedule-to-the-President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf) on allocation and gatekeeping to make your recommendation.")
                    .optional(CaseData::getAllocationProposal);

        event("attendingHearing").forState(Open)
                .name("Attending the hearing")
                .description("Enter extra support needed for anyone to take part in hearing")
                .displayOrder(13)
                .fields()
                    .optional(CaseData::getHearingPreferences);

        event("uploadDocuments")
                .forAllStates()
                .explicitGrants()
                .grant("CRU", LOCAL_AUTHORITY)
                .name("Documents")
                .description("Upload documents")
                .displayOrder(14)
                .midEventWebhook()
                .fields()
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
                    .optional(CaseData::getOtherSocialWorkDocuments)
                    .label("documents_socialWorkOther_border_bottom", "-------------------------------------------------------------------------------------------------------------");

        event("changeCaseName").forState(Open)
                .name("Change case name")
                .description("Change case name")
                .displayOrder(15)
                .fields()
                    .optional(CaseData::getCaseName);

        event("addCaseIDReference").forState(Open)
                .name("Add case ID")
                .description("Add case ID")
                .explicitGrants() // Do not inherit State level role permissions
                .displayOrder(16)
                .fields()
                    .pageLabel("Add Case ID")
                    .field("caseIDReference", DisplayContext.Optional, null, "Text", null, "Case ID");
    }

    private void buildC21Event(State state) {
        event("createOrder")
            .forState(state)
            .explicitGrants()
            .grant("CRU", HMCTS_ADMIN, JUDICIARY)
            .name("Create an order")
            .showSummary()
            .allWebhooks("create-order")
            .midEventWebhook()
            .fields()
            .page("OrderInformation")
                .complex(CaseData::getOrderTypeAndDocument)
                    .optional(OrderTypeAndDocument::getType)
                    .mandatory(OrderTypeAndDocument::getDocument)
                    .done()
                .complex(CaseData::getOrder)
                    .readonly(GeneratedOrder::getTitle)
                    .readonly(GeneratedOrder::getDetails)
                    .readonly(GeneratedOrder::getDate)
                    .done()
            .page("JudgeInformation")
                .complex(CaseData::getJudgeAndLegalAdvisor)
                    .optional(JudgeAndLegalAdvisor::getJudgeTitle)
                    .mandatory(JudgeAndLegalAdvisor::getJudgeLastName)
                    .optional(JudgeAndLegalAdvisor::getJudgeFullName)
                    .optional(JudgeAndLegalAdvisor::getLegalAdvisorName);
    }
}
