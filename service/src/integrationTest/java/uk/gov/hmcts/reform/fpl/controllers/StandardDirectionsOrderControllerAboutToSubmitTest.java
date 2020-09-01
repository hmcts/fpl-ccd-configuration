package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerAboutToSubmitTest extends AbstractControllerTest {
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String SEALED_ORDER_FILE_NAME = "standard-directions-order.pdf";
    private static final Document DOCUMENT = document();
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.of(2020, 1, 20, 11, 11, 11);
    private static final LocalDateTime HEARING_END_DATE = LocalDateTime.of(2020, 2, 20, 11, 11, 11);
    private static final String DIRECTION_TYPE = "Identify alternative carers";
    private static final String DIRECTION_TEXT = "Contact the parents to make sure there is a complete family tree "
        + "showing family members who could be alternative carers.";

    @MockBean
    private DocmosisDocumentGeneratorService docmosisService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Captor
    private ArgumentCaptor<String> fileName;

    StandardDirectionsOrderControllerAboutToSubmitTest() {
        super("draft-standard-directions");
    }

    @BeforeEach
    void setup() {
        DocmosisDocument docmosisDocument = new DocmosisDocument(SEALED_ORDER_FILE_NAME, PDF);

        given(docmosisService.generateDocmosisDocument(any(DocmosisData.class), any())).willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(eq(PDF), fileName.capture())).willReturn(DOCUMENT);
    }

    @Test
    void shouldPopulateHiddenCCDFieldsInStandardDirectionOrderToPersistData() {
        JudgeAndLegalAdvisor legalAdvisorWithAllocatedJudge = JudgeAndLegalAdvisor.builder()
            .useAllocatedJudge("Yes")
            .legalAdvisorName("Chris Newport")
            .build();

        Judge allocatedJudge = Judge.builder().judgeTitle(MAGISTRATES).judgeFullName("John Walker").build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(directionsWithShowHideValuesRemoved()
                .put("dateOfIssue", dateNow())
                .put("standardDirectionOrder", StandardDirectionOrder.builder().orderStatus(SEALED).build())
                .put("judgeAndLegalAdvisor", legalAdvisorWithAllocatedJudge)
                .put("allocatedJudge", allocatedJudge)
                .put(HEARING_DETAILS_KEY, wrapElements(HearingBooking.builder()
                    .startDate(HEARING_START_DATE)
                    .endDate(HEARING_END_DATE)
                    .venue("EXAMPLE")
                    .build()))
                .put("caseLocalAuthority", "example")
                .put("dateSubmitted", dateNow())
                .put("applicants", getApplicant())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getStandardDirectionOrder()).isEqualToComparingFieldByField(expectedOrder());
        assertThat(caseData.getJudgeAndLegalAdvisor()).isNull();
        assertThatDirectionsArePlacedBackIntoCaseDetailsWithValues(caseData);
        assertThat(fileName.getValue()).isEqualTo(SEALED_ORDER_FILE_NAME);
    }

    @Test
    void shouldReturnErrorsWhenNoHearingDetailsExistsForSealedOrder() {
        ImmutableMap<String, Object> build = mapWithDirections()
            .put("standardDirectionOrder", StandardDirectionOrder.builder().orderStatus(SEALED).build())
            .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
            .put("allocatedJudge", Judge.builder().build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder().data(build).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getErrors()).containsOnly("You need to enter a hearing date.");
    }

    @Test
    void shouldReturnErrorsWhenNoAllocatedJudgeExistsForSealedOrder() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails());

        assertThat(response.getErrors()).containsOnly("You need to enter the allocated judge.");
    }

    private ImmutableMap.Builder<String, Object> directionsWithShowHideValuesRemoved() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        Stream.of(DirectionAssignee.values())
            .forEach(assignee -> builder.put(assignee.getValue(), buildDirection(assignee)));

        return builder;
    }

    private List<Element<Direction>> buildDirection(DirectionAssignee assignee) {
        return wrapElements(Direction.builder()
            .directionType(DIRECTION_TYPE)
            .directionText(DIRECTION_TEXT)
            .assignee(assignee)
            .readOnly("Yes")
            .directionRemovable("Yes")
            .directionNeeded("Yes")
            .build());
    }

    private StandardDirectionOrder expectedOrder() {
        return StandardDirectionOrder.builder()
            .directions(fullyPopulatedDirections())
            .orderStatus(SEALED)
            .orderDoc(DocumentReference.builder()
                .url(DOCUMENT.links.self.href)
                .binaryUrl(DOCUMENT.links.binary.href)
                .filename("file.pdf")
                .build())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(MAGISTRATES)
                .judgeFullName("John Walker")
                .legalAdvisorName("Chris Newport")
                .build())
            .dateOfIssue(formatLocalDateToString(dateNow(), "d MMMM yyyy"))
            .build();
    }

    private void assertThatDirectionsArePlacedBackIntoCaseDetailsWithValues(CaseData caseData) {
        assertThat(unwrapElements(caseData.getAllParties())).containsOnly(fullyPopulatedDirection(ALL_PARTIES));

        List<Element<Direction>> localAuthorityDirections = caseData.getLocalAuthorityDirections();
        assertThat(unwrapElements(localAuthorityDirections)).containsOnly(fullyPopulatedDirection(LOCAL_AUTHORITY));

        List<Element<Direction>> respondentDirections = caseData.getRespondentDirections();
        assertThat(unwrapElements(respondentDirections)).containsOnly(fullyPopulatedDirection(PARENTS_AND_RESPONDENTS));

        assertThat(unwrapElements(caseData.getCafcassDirections())).containsOnly(fullyPopulatedDirection(CAFCASS));
        assertThat(unwrapElements(caseData.getOtherPartiesDirections())).containsOnly(fullyPopulatedDirection(OTHERS));
        assertThat(unwrapElements(caseData.getCourtDirections())).containsOnly(fullyPopulatedDirection(COURT));
    }

    private Direction fullyPopulatedDirection(DirectionAssignee assignee) {
        return Direction.builder()
            .directionType(DIRECTION_TYPE)
            .directionText(DIRECTION_TEXT)
            .assignee(assignee)
            .directionRemovable("Yes")
            .directionNeeded("Yes")
            .readOnly("Yes")
            .dateToBeCompletedBy(HEARING_START_DATE.toLocalDate().atStartOfDay())
            .build();
    }

    private List<Element<Direction>> fullyPopulatedDirections() {
        return Stream.of(DirectionAssignee.values())
            .map(assignee -> element(null, fullyPopulatedDirection(assignee)))
            .collect(toList());
    }

    private ImmutableMap.Builder<String, Object> mapWithDirections() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        return builder
            .put(LOCAL_AUTHORITY.getValue(), buildDirections(Direction.builder().assignee(LOCAL_AUTHORITY).build()))
            .put(ALL_PARTIES.getValue(), buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .put(PARENTS_AND_RESPONDENTS.getValue(),
                buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .put(CAFCASS.getValue(), buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .put(OTHERS.getValue(), buildDirections(Direction.builder().assignee(OTHERS).build()))
            .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()));
    }

    private CaseDetails caseDetails() {
        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, wrapElements(HearingBooking.builder()
                .startDate(HEARING_START_DATE)
                .endDate(HEARING_END_DATE)
                .build()),
            "respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .dateOfBirth(dateNow().plusDays(1))
                    .lastName("Moley")
                    .relationshipToChild("Uncle")
                    .build())
                .build()),
            "standardDirectionOrder", StandardDirectionOrder.builder()
                .orderStatus(SEALED)
                .orderDoc(DocumentReference.builder().build())
                .build(),
            "caseLocalAuthority", "example");

        return CaseDetails.builder()
            .id(1L)
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .data(data)
            .build();
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return wrapElements(direction.toBuilder().directionType("Direction").build());
    }

    private List<Element<Applicant>> getApplicant() {
        return wrapElements(Applicant.builder().party(ApplicantParty.builder().organisationName("").build()).build());
    }
}
