package uk.gov.hmcts.reform.fpl.service.cafcass;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NoticeOfHearingCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ADDITIONAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_APPLICATION;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;

class CafcassRequestEmailContentProviderTest {

    @Test
    void shouldReturnEmptySubjectWhenOrderIsNotified() {
        assertThat(ORDER.getType().apply(
                CaseData.builder()
                        .familyManCaseNumber("123")
                        .build(),
                OrderCafcassData.builder().build())
        ).isEqualTo("Court Ref. 123.- new order");
    }

    @Test
    void shouldReturnEmptySubjectWhenCourtBundleIsNotified() {
        assertThat(COURT_BUNDLE.getType().apply(
                CaseData.builder()
                        .familyManCaseNumber("123")
                        .build(),
                CourtBundleData.builder().build())
        ).isEqualTo("Court Ref. 123.- new court bundle");
    }

    @Test
    void shouldReturnEmptySubjectWhenNewApplicationIsNotified() {
        assertThat(NEW_APPLICATION.getType().apply(
                CaseData.builder()
                        .build(),
                NewApplicationCafcassData.builder()
                        .timeFrameValue("12:30")
                        .eldestChildLastName("Bright")
                        .build())
        ).isEqualTo("Application received – hearing 12:30, Bright");
    }

    @Test
    void shouldReturnEmptySubjectWhenNewDocumentIsNotified() {
        assertThat(NEW_DOCUMENT.getType().apply(
                CaseData.builder()
                        .familyManCaseNumber("123")
                        .build(),
                NewDocumentData.builder()
                        .emailSubjectInfo("bundle")
                        .build())
        ).isEqualTo("Court Ref. 123.- bundle");
    }

    @Test
    void shouldReturnEmptySubjectWhenAdditionalDocumentIsNotified() {
        assertThat(ADDITIONAL_DOCUMENT.getType().apply(
                CaseData.builder()
                        .familyManCaseNumber("123")
                        .build(),
                NewDocumentData.builder()
                        .emailSubjectInfo("additional")
                        .build())
        ).isEqualTo("Court Ref. 123.- additional");
    }

    @Test
    void shouldReturnEmptySubjectWhenNoticeOfHearingIsNotified() {
        assertThat(NOTICE_OF_HEARING.getType().apply(
                CaseData.builder()
                        .familyManCaseNumber("123")
                        .build(),
                NoticeOfHearingCafcassData.builder()
                        .hearingType("new")
                        .eldestChildLastName("Wright")
                        .build())
        ).isEqualTo("Court Ref. 123.- New new hearing Wright - notice of hearing");
    }
}