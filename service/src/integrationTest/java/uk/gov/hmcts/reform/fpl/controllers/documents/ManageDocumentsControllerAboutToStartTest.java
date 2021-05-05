package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerAboutToStartTest extends AbstractCallbackTest {
    ManageDocumentsControllerAboutToStartTest() {
        super("manage-documents");
    }

    @Test
    void shouldBuildManageDocumentsHearingListAndAdditionalApplicationsSupportingDocumentsList() {
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(hearing(LocalDateTime.of(2020, 3, 15, 20, 20))),
            element(hearing(LocalDateTime.of(2020, 3, 16, 10, 10))));

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(LocalDateTime.now().plusDays(1))),
            element(buildC2DocumentBundle(LocalDateTime.now().plusDays(2))));

        C2DocumentBundle c2Application = buildC2DocumentBundle(LocalDateTime.now());

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(UUID.randomUUID()).applicationType(OtherApplicationType.C17A_EXTENSION_OF_ESO)
            .uploadedDateTime(LocalDateTime.now().plusDays(1).toString())
            .build();

        AdditionalApplicationsBundle applicationsBundle = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(c2Application)
            .otherApplicationsBundle(otherApplicationsBundle)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "furtherEvidenceDocumentsTEMP", List.of(),
                "additionalApplicationsBundle", wrapElements(applicationsBundle),
                "c2DocumentBundle", c2DocumentBundle,
                "hearingDetails", hearingBookings
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        DynamicList expectedHearingDynamicList = ElementUtils
            .asDynamicList(hearingBookings, null, HearingBooking::toLabel);

        DynamicList expectedC2DocumentsDynamicList = TestDataHelper.buildDynamicList(
            Pair.of(
                c2DocumentBundle.get(1).getId(), "C2, " + c2DocumentBundle.get(1).getValue().getUploadedDateTime()),
            Pair.of(
                c2DocumentBundle.get(0).getId(), "C2, " + c2DocumentBundle.get(0).getValue().getUploadedDateTime()),
            Pair.of(c2Application.getId(), "C2, " + c2Application.getUploadedDateTime()),
            Pair.of(otherApplicationsBundle.getId(), "C17a, " + otherApplicationsBundle.getUploadedDateTime())
        );

        DynamicList hearingDynamicList =
            mapper.convertValue(response.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY), DynamicList.class);

        DynamicList c2DocumentDynamicList =
            mapper.convertValue(response.getData().get(SUPPORTING_C2_LIST_KEY), DynamicList.class);

        ManageDocument actualManageDocument =
            mapper.convertValue(response.getData().get(MANAGE_DOCUMENT_KEY), ManageDocument.class);

        ManageDocument expectedManageDocument = ManageDocument.builder()
            .hasHearings(YES.getValue())
            .hasC2s(YES.getValue())
            .build();

        assertThat(hearingDynamicList).isEqualTo(expectedHearingDynamicList);
        assertThat(c2DocumentDynamicList).isEqualTo(expectedC2DocumentsDynamicList);
        assertThat(actualManageDocument).isEqualTo(expectedManageDocument);
        assertThat(response.getData()).doesNotContainKeys("furtherEvidenceDocumentsTEMP");
    }

    private HearingBooking hearing(LocalDateTime startDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .build();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder()
            .id(UUID.randomUUID())
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME))
            .build();
    }
}
