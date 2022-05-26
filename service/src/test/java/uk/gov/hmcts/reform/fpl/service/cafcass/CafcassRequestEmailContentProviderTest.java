package uk.gov.hmcts.reform.fpl.service.cafcass;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
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
                CaseData.builder().build(),
                OrderCafcassData.builder().build())
        ).isEmpty();
    }

    @Test
    void shouldReturnEmptySubjectWhenCourtBundleIsNotified() {
        assertThat(COURT_BUNDLE.getType().apply(
                CaseData.builder().build(),
                CourtBundleData.builder().build())
        ).isEmpty();
    }

    @Test
    void shouldReturnEmptySubjectWhenNewApplicationIsNotified() {
        assertThat(NEW_APPLICATION.getType().apply(
                CaseData.builder().build(),
                NewApplicationCafcassData.builder().build())
        ).isEmpty();
    }

    @Test
    void shouldReturnEmptySubjectWhenNewDocumentIsNotified() {
        assertThat(NEW_DOCUMENT.getType().apply(
                CaseData.builder().build(),
                NewDocumentData.builder().build())
        ).isEmpty();
    }

    @Test
    void shouldReturnEmptySubjectWhenAdditionalDocumentIsNotified() {
        assertThat(ADDITIONAL_DOCUMENT.getType().apply(
                CaseData.builder().build(),
                NewDocumentData.builder().build())
        ).isEmpty();
    }

    @Test
    void shouldReturnEmptySubjectWhenNoticeOfHearingIsNotified() {
        assertThat(NOTICE_OF_HEARING.getType().apply(
                CaseData.builder().build(),
                NewDocumentData.builder().build())
        ).isEmpty();
    }
}