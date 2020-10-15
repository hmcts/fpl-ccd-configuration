package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerAboutToSubmitTest extends AbstractControllerTest {

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
        assertThat(caseData.getSelectedHearingId()).isNotNull();
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

    @Test
    void shouldUpdateExistingHearingInHearingDetailsListWhenEditHearingSelected() {
        HearingBooking existingHearing = hearing(LocalDateTime.of(2020, 2, 2, 20, 20), "96");

        Element<HearingBooking> hearingElement = element(existingHearing);

        CaseData initialData = CaseData.builder()
            .hearingOption(EDIT_HEARING)
            .hearingDateList(dynamicList(List.of(hearingElement)))
            .hearingDetails(List.of(hearingElement))
            .hearingType(ISSUE_RESOLUTION)
            .hearingVenue(existingHearing.getVenue())
            .hearingVenueCustom(existingHearing.getVenueCustomAddress())
            .hearingStartDate(existingHearing.getStartDate())
            .hearingEndDate(existingHearing.getEndDate())
            .judgeAndLegalAdvisor(existingHearing.getJudgeAndLegalAdvisor())
            .noticeOfHearingNotes(existingHearing.getAdditionalNotes())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(asCaseDetails(initialData));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        HearingBooking expectedHearing = existingHearing.toBuilder().type(ISSUE_RESOLUTION).build();
        assertThat(unwrapElements(caseData.getHearingDetails())).containsExactly(expectedHearing);
        assertThat(caseData.getSelectedHearingId()).isEqualTo(hearingElement.getId());

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

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(hearings.get(0).getId())
                .label("Case management hearing, 25 June 2099")
                .build())
            .listItems(List.of(
                DynamicListElement.builder()
                    .code(hearings.get(0).getId())
                    .label("Case management hearing, 25 June 2099")
                    .build()
            ))
            .build();
    }
}
