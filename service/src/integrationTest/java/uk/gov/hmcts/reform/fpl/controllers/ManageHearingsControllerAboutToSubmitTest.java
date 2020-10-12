package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerAboutToSubmitTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    ManageHearingsControllerAboutToSubmitTest() {
        super("manage-hearings");
    }

    @Test
    void shouldAddNewHearingToHearingDetailsListWhenAddHearingSelected() {
        HearingBooking newHearing = hearing(LocalDateTime.of(2020, 2, 2, 20, 20), "96");
        CaseData initialData = CaseData.builder()
            .hearingType(newHearing.getType())
            .hearingVenue(newHearing.getVenue())
            .hearingVenueCustom(newHearing.getVenueCustomAddress())
            .hearingStartDate(newHearing.getStartDate())
            .hearingEndDate(newHearing.getEndDate())
            .judgeAndLegalAdvisor(newHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(newHearing.getAdditionalNotes())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(asCaseDetails(initialData));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(unwrapElements(caseData.getHearingDetails())).containsExactly(newHearing);
        assertThat(caseData.getFirstHearingFlag()).isEqualTo("No");
    }

    @Test
    void shouldIncludeNoticeOfHearingWhenSendNoticeOfHearingSelected() {
        Document document = document();

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT));
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document);

        HearingBooking newHearing = hearing(LocalDateTime.of(2020, 2, 2, 20, 20), "96");
        CaseData initialData = CaseData.builder()
            .children1(createPopulatedChildren(now().toLocalDate()))
            .caseLocalAuthority("example")
            .sendNoticeOfHearing("Yes")
            .hearingType(newHearing.getType())
            .hearingVenue(newHearing.getVenue())
            .hearingVenueCustom(newHearing.getVenueCustomAddress())
            .hearingStartDate(newHearing.getStartDate())
            .hearingEndDate(newHearing.getEndDate())
            .judgeAndLegalAdvisor(newHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(newHearing.getAdditionalNotes())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(asCaseDetails(initialData));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        HearingBooking hearingAfterCallback = newHearing.toBuilder().noticeOfHearing(
            DocumentReference.buildFromDocument(document)).build();

        assertThat(unwrapElements(caseData.getHearingDetails())).containsExactly(hearingAfterCallback);
        assertThat(caseData.getFirstHearingFlag()).isEqualTo("No");
    }

    //TODO
    @Test
    void shouldUpdateExistingHearingInHearingDetailsListWhenEditHearingSelected() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
    }

    private List<Element<HearingBooking>> hearings() {
        return List.of(
            element(hearing(LocalDateTime.of(2099, 6, 25, 20, 20), "162")),
            element(hearing(LocalDateTime.of(2020, 2, 2, 20, 20), "96"))
        );
    }

    private HearingBooking hearing(LocalDateTime startDate, String venue) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .venueCustomAddress(Address.builder().build())
            .venue(venue)
            .build();
    }
}
