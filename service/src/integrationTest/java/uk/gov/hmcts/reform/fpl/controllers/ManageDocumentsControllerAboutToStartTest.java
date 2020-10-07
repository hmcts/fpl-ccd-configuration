package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsControllerAboutToStartTest extends AbstractControllerTest {
    ManageDocumentsControllerAboutToStartTest() {
        super("manage-documents");
    }

    @Test
    void shouldBuildManageDocumentsHearingListAndSupportingC2DocumentsList() {
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(hearing(LocalDateTime.of(2020, 3, 15, 20, 20))),
            element(hearing(LocalDateTime.of(2020, 3, 16, 10, 10))));

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(LocalDateTime.now().plusDays(2))),
            element(buildC2DocumentBundle(LocalDateTime.now().plusDays(1))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "furtherEvidenceDocumentsTEMP", List.of(),
                "c2DocumentBundle", c2DocumentBundle,
                "hearingDetails", hearingBookings
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        DynamicList expectedHearingDynamicList = ElementUtils
            .asDynamicList(hearingBookings, null, hearingBooking -> hearingBooking.toLabel(DATE));

        IncrementalInteger i = new IncrementalInteger(1);
        DynamicList expectedC2DocumentsDynamicList = ElementUtils
            .asDynamicList(c2DocumentBundle, null, documentBundle ->
                documentBundle.toLabel(i.getAndIncrement()));

        DynamicList hearingDynamicList =
            mapper.convertValue(response.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY), DynamicList.class);

        DynamicList c2DocumentDynamicList =
            mapper.convertValue(response.getData().get(SUPPORTING_C2_LIST_KEY), DynamicList.class);

        ManageDocument actualManageDocument =
            mapper.convertValue(response.getData().get(MANAGE_DOCUMENT_KEY), ManageDocument.class);

        ManageDocument expectedManageDocument = ManageDocument.builder().hasHearings(YES.getValue()).build();

        assertThat(hearingDynamicList).isEqualTo(expectedHearingDynamicList);
        assertThat(c2DocumentDynamicList).isEqualTo(expectedC2DocumentsDynamicList);
        assertThat(actualManageDocument).isEqualTo(expectedManageDocument);
        assertThat(response.getData().get("furtherEvidenceDocumentsTEMP")).isNull();
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
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }
}
