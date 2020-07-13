package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.ALLOCATED_JUDGE_KEY;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerAboutToSubmitTest extends AbstractControllerTest {

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Autowired
    private ObjectMapper mapper;

    HearingBookingDetailsControllerAboutToSubmitTest() {
        super("add-hearing-bookings");
    }

    @Test
    void shouldReturnEmptyHearingListWhenNoHearingInCase() {
        CallbackRequest callbackRequest = callbackRequest(hearingMapOf(emptyList()), hearingMapOf(emptyList()));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEmpty();
    }

    @Test
    void shouldReturnHearingsWhenNoHearingsExistInPast() {
        List<Element<HearingBooking>> hearingDetails = wrapElements(createHearingBooking(now().plusDays(5)));

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, hearingDetails,
            ALLOCATED_JUDGE_KEY, buildAllocatedJudge()
        );

        CallbackRequest callbackRequest = callbackRequest(data, hearingMapOf(hearingDetails));
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEqualTo(hearingDetails);
    }

    @Test
    void shouldReturnHearingsWhenNoHearingsExistInFuture() {
        List<Element<HearingBooking>> hearingDetails = wrapElements(createHearingBooking(now().minusDays(5)));

        CallbackRequest callbackRequest = callbackRequest(hearingMapOf(emptyList()), hearingMapOf(hearingDetails));
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).isEqualTo(hearingDetails);
    }

    @Test
    void shouldReturnHearingsWhenHearingsInPastAndFutureExist() {
        Element<HearingBooking> hearing = element(createHearingBooking(now().plusDays(5)));
        Element<HearingBooking> pastHearing = element(createHearingBooking(now().minusDays(5)));

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, newArrayList(hearing),
            ALLOCATED_JUDGE_KEY, buildAllocatedJudge()
        );

        CallbackRequest callbackRequest = callbackRequest(data, hearingMapOf(List.of(hearing, pastHearing)));
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);

        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).containsExactly(pastHearing, hearing);
    }

    @Test
    void shouldUpdateHearingBookingJudgeWhenHearingIsToUseAllocatedJudge() {
        HearingBooking hearingBooking = HearingBooking.builder()
            .startDate(now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .useAllocatedJudge(YES.getValue())
                .build())
            .build();

        List<Element<HearingBooking>> hearingBookings = wrapElements(hearingBooking);

        Map<String, Object> data = Map.of(
            HEARING_DETAILS_KEY, hearingBookings,
            ALLOCATED_JUDGE_KEY, buildAllocatedJudge()
        );

        CallbackRequest callbackRequest = callbackRequest(data, hearingMapOf(emptyList()));
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);
        HearingBooking returnedHearing = unwrapElements(caseData.getHearingDetails()).get(0);

        assertThat(returnedHearing.getJudgeAndLegalAdvisor().getJudgeTitle()).isEqualTo(HER_HONOUR_JUDGE);
        assertThat(returnedHearing.getJudgeAndLegalAdvisor().getJudgeLastName()).isEqualTo("Watson");
    }

    @Test
    void shouldOnlyAddCurrentHearingToListWhenSameIdForHearings() {
        UUID id = randomUUID();

        HearingBooking hearing = createHearingBooking(now());
        HearingBooking hearingInPast = createHearingBooking(now().minusMinutes(1));

        CallbackRequest callbackRequest = getCallbackRequest(hearing, hearingInPast, id);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getHearingDetails()).containsExactly(element(id, hearing));
    }

    @Nested
    class NoticeOfHearingTests {
        private Document document;

        @BeforeEach
        void setUp() {
            document = document();

            given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
                .willReturn(testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT));
            given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);
        }

        @Test
        void shouldAddNoticeOfHearingToNewHearingsWhenHearingHasBeenSelected() {
            CaseData caseData = CaseData.builder()
                .newHearingSelector(Selector.builder().selected(List.of(1)).build())
                .hearingDetails(wrapElements(
                    createHearingBooking(LocalDateTime.now()),
                    createHearingBooking(LocalDateTime.now().plusDays(3)),
                    createHearingBooking(LocalDateTime.now().plusDays(6))))
                .children1(createPopulatedChildren(now().toLocalDate()))
                .build();

            Map<String, Object> data = mapper.convertValue(caseData, new TypeReference<>() {});

            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .data(data)
                    .build())
                .caseDetailsBefore(CaseDetails.builder()
                    .data(Map.of("data", "some data")).build()).build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(callbackRequest);
            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
            DocumentReference noticeOfHearing = responseData.getHearingDetails().get(1).getValue().getNoticeOfHearing();

            assertThat(responseData.getHearingDetails().get(0).getValue().getNoticeOfHearing()).isNull();
            assertThat(noticeOfHearing).isEqualTo(DocumentReference.buildFromDocument(document));
            assertThat(responseData.getHearingDetails().get(2).getValue().getNoticeOfHearing()).isNull();
        }
    }

    private CallbackRequest getCallbackRequest(HearingBooking hearing, HearingBooking hearingInPast, UUID id) {
        return callbackRequest(
            hearingMapOf(List.of(element(id, hearing))),
            hearingMapOf(List.of(element(id, hearingInPast))));
    }

    private Map<String, Object> hearingMapOf(List<Element<HearingBooking>> hearings) {
        return Map.of(HEARING_DETAILS_KEY, hearings);
    }

    private HearingBooking createHearingBooking(LocalDateTime startDate) {
        return HearingBooking.builder()
            .type(HearingType.CASE_MANAGEMENT)
            .startDate(startDate)
            .venue("Venue")
            .endDate(startDate.plusHours(3))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Law")
                .legalAdvisorName("Peter Parker")
                .build())
            .build();
    }

    private CallbackRequest callbackRequest(Map<String, Object> data, Map<String, Object> dataBefore) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .data(dataBefore)
                .build())
            .build();
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Watson")
            .build();
    }
}
