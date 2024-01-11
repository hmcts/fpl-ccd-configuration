package uk.gov.hmcts.reform.fpl.service.document.aggregator;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentContainerView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.document.transformer.ApplicationDocumentBundleTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.FurtherEvidenceDocumentsBundlesTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.OtherDocumentsTransformer;
import uk.gov.hmcts.reform.fpl.service.document.transformer.RespondentStatementsTransformer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.CTSC;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.LA;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType.HMCTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BundleViewAggregatorTest {

    private static final DocumentViewType DOCUMENT_VIEW_TYPE = DocumentViewType.HMCTS;
    private static final List<Element<HearingFurtherEvidenceBundle>> HEARING_FURTHER_EVIDENCE_DOCUMENTS = List.of(
        element(UUID.randomUUID(), mock(HearingFurtherEvidenceBundle.class)));
    private static final CaseData CASE_DATA = CaseData.builder()
        .hearingFurtherEvidenceDocuments(HEARING_FURTHER_EVIDENCE_DOCUMENTS)
        .build();
    private static final DocumentContainerView APPLICATION_STATEMENT_BUNDLE_VIEWS = mock(DocumentContainerView.class);
    private static final List<DocumentContainerView> FURTHER_EVIDENCE_BUNDLE_VIEWS =
        List.of(mock(DocumentContainerView.class));
    private static final List<DocumentContainerView> RESPONDENT_STATEMENT_BUNDLE_VIEWS =
        List.of(mock(DocumentContainerView.class));
    private static final List<DocumentContainerView> OTHER_DOCUMENTS_BUNDLE_VIEWS =
        List.of(mock(DocumentContainerView.class));

    @Mock
    private ApplicationDocumentBundleTransformer applicationDocumentsTransformer;

    @Mock
    private FurtherEvidenceDocumentsBundlesTransformer furtherEvidenceTransformer;

    @Mock
    private RespondentStatementsTransformer respondentStatementsTransformer;

    @Mock
    private OtherDocumentsTransformer otherDocumentsTransformer;

    @InjectMocks
    private BundleViewAggregator underTest;

    @Mock
    private ManageDocumentService manageDocumentService;

    @Test
    void testGetDocumentBundleViews() {
        when(applicationDocumentsTransformer.getApplicationStatementAndDocumentBundle(CASE_DATA, DOCUMENT_VIEW_TYPE))
            .thenReturn(APPLICATION_STATEMENT_BUNDLE_VIEWS);

        when(furtherEvidenceTransformer.getFurtherEvidenceDocumentsBundleView(CASE_DATA, DOCUMENT_VIEW_TYPE))
            .thenReturn(FURTHER_EVIDENCE_BUNDLE_VIEWS);

        when(respondentStatementsTransformer.getRespondentStatementsBundle(CASE_DATA, DOCUMENT_VIEW_TYPE))
            .thenReturn(RESPONDENT_STATEMENT_BUNDLE_VIEWS);

        when(otherDocumentsTransformer.getOtherDocumentsView(CASE_DATA, DOCUMENT_VIEW_TYPE))
            .thenReturn(OTHER_DOCUMENTS_BUNDLE_VIEWS);

        List<DocumentContainerView> actual = underTest.getDocumentBundleViews(CASE_DATA, DOCUMENT_VIEW_TYPE);

        assertThat(actual).isEqualTo(Stream.of(
            List.of(APPLICATION_STATEMENT_BUNDLE_VIEWS),
            FURTHER_EVIDENCE_BUNDLE_VIEWS,
            RESPONDENT_STATEMENT_BUNDLE_VIEWS,
            OTHER_DOCUMENTS_BUNDLE_VIEWS
        ).flatMap(Collection::stream).collect(toList()));
    }

    @Test
    void testGetDocumentBundleViewsIfEmpty() {
        when(applicationDocumentsTransformer.getApplicationStatementAndDocumentBundle(CASE_DATA, DOCUMENT_VIEW_TYPE))
            .thenReturn(null);

        when(furtherEvidenceTransformer.getFurtherEvidenceDocumentsBundleView(CASE_DATA, DOCUMENT_VIEW_TYPE))
            .thenReturn(Collections.emptyList());

        when(respondentStatementsTransformer.getRespondentStatementsBundle(CASE_DATA, DOCUMENT_VIEW_TYPE))
            .thenReturn(Collections.emptyList());

        when(otherDocumentsTransformer.getOtherDocumentsView(CASE_DATA, DOCUMENT_VIEW_TYPE))
            .thenReturn(Collections.emptyList());

        List<DocumentContainerView> actual = underTest.getDocumentBundleViews(CASE_DATA, DOCUMENT_VIEW_TYPE);

        assertThat(actual).isEqualTo(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @EnumSource(value = DocumentType.class, names = {
        "CASE_SUMMARY", "POSITION_STATEMENTS", "POSITION_STATEMENTS_CHILD",
        "POSITION_STATEMENTS_RESPONDENT", "THRESHOLD", "SKELETON_ARGUMENTS", "JUDGEMENTS", "TRANSCRIPTS",
        "DOCUMENTS_FILED_ON_ISSUE", "APPLICANTS_WITNESS_STATEMENTS", "CARE_PLAN", "PARENT_ASSESSMENTS",
        "FAMILY_AND_VIABILITY_ASSESSMENTS", "APPLICANTS_OTHER_DOCUMENTS", "MEETING_NOTES", "CONTACT_NOTES",
        "C1_APPLICATION_DOCUMENTS", "C2_APPLICATION_DOCUMENTS", "RESPONDENTS_STATEMENTS",
        "RESPONDENTS_WITNESS_STATEMENTS", "GUARDIAN_EVIDENCE", "EXPERT_REPORTS", "DRUG_AND_ALCOHOL_REPORTS",
        "LETTER_OF_INSTRUCTION", "POLICE_DISCLOSURE", "MEDICAL_RECORDS", "COURT_CORRESPONDENCE",
        "NOTICE_OF_ACTING_OR_ISSUE"
    })
    void testGetConfidentialDocumentBundleViews(DocumentType documentType) {
        String filename1 = "FILE_NAME1.pdf";
        String filename2 = "FILE_NAME2.pdf";

        Element<ManagedDocument> confDoc1 = element(ManagedDocument.builder()
            .uploaderType(DESIGNATED_LOCAL_AUTHORITY)
            .document(DocumentReference.builder().filename(filename1).build())
            .build());
        Element<ManagedDocument> confDoc2 = element(ManagedDocument.builder()
            .document(DocumentReference.builder().filename(filename2).build())
            .build());

        when(manageDocumentService.retrieveDocuments(CASE_DATA, documentType, LA))
            .thenReturn((List<Element<WithDocument>>) (List<?>) List.of(confDoc1));
        when(manageDocumentService.retrieveDocuments(CASE_DATA, documentType, CTSC))
            .thenReturn((List<Element<WithDocument>>) (List<?>) List.of(confDoc2));

        List<DocumentView> actual = underTest.getConfidentialDocumentBundleViews(CASE_DATA, HMCTS);
        assertThat(actual).hasSize(2).contains(
            DocumentView.builder()
                .title(filename1)
                .fileName(filename1)
                .documentType(StringUtils.replace(documentType.getDescription(), "└─ ",  ""))
                .confidentialLevel("Restrict to the LA, Cafcass and HMCTS staff")
                .uploaderType(DESIGNATED_LOCAL_AUTHORITY.name())
                .build(),
            DocumentView.builder()
                .title(filename2)
                .fileName(filename2)
                .documentType(StringUtils.replace(documentType.getDescription(), "└─ ",  ""))
                .confidentialLevel("Restrict to HMCTS staff")
                .uploaderType("unknown")
                .build());

        actual = underTest.getConfidentialDocumentBundleViews(CASE_DATA, DocumentViewType.LA);
        assertThat(actual).hasSize(1).contains(
            DocumentView.builder()
                .title(filename1)
                .fileName(filename1)
                .documentType(StringUtils.replace(documentType.getDescription(), "└─ ",  ""))
                .confidentialLevel("Restrict to the LA, Cafcass and HMCTS staff")
                .uploaderType(DESIGNATED_LOCAL_AUTHORITY.name())
                .build());
    }

    @ParameterizedTest
    @EnumSource(value = DocumentType.class, names = {
        "CASE_SUMMARY", "POSITION_STATEMENTS", "POSITION_STATEMENTS_CHILD",
        "POSITION_STATEMENTS_RESPONDENT", "THRESHOLD", "SKELETON_ARGUMENTS", "JUDGEMENTS", "TRANSCRIPTS",
        "DOCUMENTS_FILED_ON_ISSUE", "APPLICANTS_WITNESS_STATEMENTS", "CARE_PLAN", "PARENT_ASSESSMENTS",
        "FAMILY_AND_VIABILITY_ASSESSMENTS", "APPLICANTS_OTHER_DOCUMENTS", "MEETING_NOTES", "CONTACT_NOTES",
        "C1_APPLICATION_DOCUMENTS", "C2_APPLICATION_DOCUMENTS", "RESPONDENTS_STATEMENTS",
        "RESPONDENTS_WITNESS_STATEMENTS", "GUARDIAN_EVIDENCE", "EXPERT_REPORTS", "DRUG_AND_ALCOHOL_REPORTS",
        "LETTER_OF_INSTRUCTION", "POLICE_DISCLOSURE", "MEDICAL_RECORDS", "COURT_CORRESPONDENCE",
        "NOTICE_OF_ACTING_OR_ISSUE"
    })
    void testGetEmptyConfidentialDocumentBundleViews(DocumentType documentType) {
        when(manageDocumentService.retrieveDocuments(CASE_DATA, documentType, LA)).thenReturn(List.of());
        when(manageDocumentService.retrieveDocuments(CASE_DATA, documentType, CTSC)).thenReturn(List.of());

        List<DocumentView> actual = underTest.getConfidentialDocumentBundleViews(CASE_DATA, HMCTS);
        assertThat(actual).isEmpty();
        actual = underTest.getConfidentialDocumentBundleViews(CASE_DATA, DocumentViewType.LA);
        assertThat(actual).isEmpty();
    }
}
