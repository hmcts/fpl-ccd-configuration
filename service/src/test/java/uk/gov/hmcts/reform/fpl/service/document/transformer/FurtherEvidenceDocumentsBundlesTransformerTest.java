package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.HMCTS;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.LA;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.NONCONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.buildFurtherEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class FurtherEvidenceDocumentsBundlesTransformerTest {

    private static final List<DocumentView> EXPERT_REPORTS_DOCUMENT_VIEWS = List.of(mock(DocumentView.class));
    private static final List<DocumentView> GUARDIAN_REPORTS_DOCUMENT_VIEWS = List.of(mock(DocumentView.class));
    private static final List<DocumentView> EXPERT_REPORTS_DOCUMENT_VIEWS_LA = List.of(mock(DocumentView.class));
    private static final List<DocumentView> GUARDIAN_REPORTS_DOCUMENT_VIEWS_LA = List.of(mock(DocumentView.class));
    private static final List<DocumentView> EXPERT_REPORTS_DOCUMENT_VIEWS_NC = List.of(mock(DocumentView.class));
    private static final List<DocumentView> GUARDIAN_REPORTS_DOCUMENT_VIEWS_NC = List.of(mock(DocumentView.class));

    public static final Element<SupportingEvidenceBundle> ADMIN_HEARING_CONFIDENTIAL_DOCUMENT =
        buildFurtherEvidenceBundle(
            "Admin hearing evidence1", "HMCTS", true, EXPERT_REPORTS, now());

    public static final Element<SupportingEvidenceBundle> ADMIN_HEARING_NON_CONFIDENTIAL_DOCUMENT =
        buildFurtherEvidenceBundle(
            "Admin hearing evidence2", "HMCTS", false, EXPERT_REPORTS, now().minusMinutes(1));

    public static final Element<SupportingEvidenceBundle> LA_HEARING_CONFIDENTIAL_DOCUMENT =
        buildFurtherEvidenceBundle(
            "LA uploaded evidence1", "Kurt LA", true, GUARDIAN_REPORTS, now().minusMinutes(2));

    public static final Element<SupportingEvidenceBundle> LA_HEARING_NON_CONFIDENTIAL_DOCUMENT =
        buildFurtherEvidenceBundle(
            "LA uploaded evidence2", "Kurt LA", false, GUARDIAN_REPORTS, now().minusMinutes(3));

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments
        = buildFurtherEvidenceDocuments();

    private final List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA
        = buildFurtherEvidenceDocumentsLA();

    private final List<Element<HearingFurtherEvidenceBundle>> hearingEvidenceDocuments
        = buildHearingFurtherEvidenceDocuments();

    private final CaseData caseData = CaseData.builder()
        .furtherEvidenceDocuments(furtherEvidenceDocuments)
        .furtherEvidenceDocumentsLA(furtherEvidenceDocumentsLA)
        .hearingFurtherEvidenceDocuments(hearingEvidenceDocuments)
        .build();

    @Mock
    private FurtherEvidenceDocumentsTransformer furtherEvidenceDocumentsTransformer;

    @InjectMocks
    private FurtherEvidenceDocumentsBundlesTransformer underTest;

    @Test
    void shouldGetFurtherEvidenceDocumentBundleForHmctsView() {

        List<Element<SupportingEvidenceBundle>> furtherEvidenceHMCTS = List.of(
            ADMIN_HEARING_CONFIDENTIAL_DOCUMENT, ADMIN_HEARING_NON_CONFIDENTIAL_DOCUMENT,
            LA_HEARING_CONFIDENTIAL_DOCUMENT, LA_HEARING_NON_CONFIDENTIAL_DOCUMENT,
            ADMIN_CONFIDENTIAL_DOCUMENT, ADMIN_NON_CONFIDENTIAL_DOCUMENT,
            LA_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT);

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            EXPERT_REPORTS, furtherEvidenceHMCTS, true)
        ).thenReturn(EXPERT_REPORTS_DOCUMENT_VIEWS);

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            GUARDIAN_REPORTS, furtherEvidenceHMCTS, true)
        ).thenReturn(GUARDIAN_REPORTS_DOCUMENT_VIEWS);

        List<DocumentBundleView> expectedBundleViewsHMCTS = List.of(DocumentBundleView.builder()
                .name(GUARDIAN_REPORTS.getLabel()).documents(GUARDIAN_REPORTS_DOCUMENT_VIEWS).build(),
            DocumentBundleView.builder().name(EXPERT_REPORTS.getLabel())
                .documents(EXPERT_REPORTS_DOCUMENT_VIEWS).build());

        List<DocumentBundleView> bundle = underTest.getFurtherEvidenceDocumentsBundleView(caseData, HMCTS);

        assertThat(bundle).isEqualTo(expectedBundleViewsHMCTS);
    }

    @Test
    void shouldGetFurtherEvidenceDocumentBundleForLA() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceLA = List.of(
            ADMIN_HEARING_NON_CONFIDENTIAL_DOCUMENT, LA_HEARING_CONFIDENTIAL_DOCUMENT,
            LA_HEARING_NON_CONFIDENTIAL_DOCUMENT, ADMIN_NON_CONFIDENTIAL_DOCUMENT,
            LA_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT);

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            GUARDIAN_REPORTS, furtherEvidenceLA, true)
        ).thenReturn(GUARDIAN_REPORTS_DOCUMENT_VIEWS_LA);

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            EXPERT_REPORTS, furtherEvidenceLA, true)
        ).thenReturn(EXPERT_REPORTS_DOCUMENT_VIEWS_LA);

        List<DocumentBundleView> expectedBundleViewsLA = List.of(DocumentBundleView.builder()
                .name(GUARDIAN_REPORTS.getLabel()).documents(GUARDIAN_REPORTS_DOCUMENT_VIEWS_LA).build(),
            DocumentBundleView.builder().name(EXPERT_REPORTS.getLabel())
                .documents(EXPERT_REPORTS_DOCUMENT_VIEWS_LA).build());

        List<DocumentBundleView> bundle = underTest.getFurtherEvidenceDocumentsBundleView(caseData, LA);

        assertThat(bundle).isEqualTo(expectedBundleViewsLA);
    }

    @Test
    void shouldGetFurtherEvidenceDocumentBundleForNonConfidentialView() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceNC = List.of(
            ADMIN_HEARING_NON_CONFIDENTIAL_DOCUMENT, LA_HEARING_NON_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT);

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            GUARDIAN_REPORTS, furtherEvidenceNC, true)
        ).thenReturn(GUARDIAN_REPORTS_DOCUMENT_VIEWS_NC);

        when(furtherEvidenceDocumentsTransformer.getFurtherEvidenceDocumentsView(
            EXPERT_REPORTS, furtherEvidenceNC, true)
        ).thenReturn(EXPERT_REPORTS_DOCUMENT_VIEWS_NC);

        List<DocumentBundleView> expectedBundleViewsNC = List.of(DocumentBundleView.builder()
                .name(GUARDIAN_REPORTS.getLabel()).documents(GUARDIAN_REPORTS_DOCUMENT_VIEWS_NC).build(),
            DocumentBundleView.builder().name(EXPERT_REPORTS.getLabel())
                .documents(EXPERT_REPORTS_DOCUMENT_VIEWS_NC).build());

        List<DocumentBundleView> bundle = underTest.getFurtherEvidenceDocumentsBundleView(caseData, NONCONFIDENTIAL);

        assertThat(bundle).isEqualTo(expectedBundleViewsNC);
    }

    @Test
    void shouldReturnEmptyBundlesWhenFurtherEvidenceContainsOnlyApplicationStatementDocuments() {
        CaseData caseDataWithApplicantStatements = CaseData.builder()
            .furtherEvidenceDocuments(List.of(ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT))
            .furtherEvidenceDocumentsLA(List.of(LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT))
            .build();

        List<DocumentBundleView> actual = underTest.getFurtherEvidenceDocumentsBundleView(
            caseDataWithApplicantStatements, NONCONFIDENTIAL);

        assertThat(actual).isEqualTo(Collections.emptyList());
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocuments() {
        return List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_DOCUMENT);
    }

    private List<Element<SupportingEvidenceBundle>> buildFurtherEvidenceDocumentsLA() {
        return List.of(
            LA_CONFIDENTIAL_DOCUMENT,
            LA_NON_CONFIDENTIAL_DOCUMENT);
    }

    private List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceDocuments() {
        List<Element<SupportingEvidenceBundle>> hearingEvidenceBundle = List.of(
            ADMIN_HEARING_CONFIDENTIAL_DOCUMENT, ADMIN_HEARING_NON_CONFIDENTIAL_DOCUMENT,
            LA_HEARING_CONFIDENTIAL_DOCUMENT, LA_HEARING_NON_CONFIDENTIAL_DOCUMENT);

        return List.of(element(UUID.randomUUID(), HearingFurtherEvidenceBundle.builder()
            .supportingEvidenceBundle(hearingEvidenceBundle)
            .build()));
    }

}
