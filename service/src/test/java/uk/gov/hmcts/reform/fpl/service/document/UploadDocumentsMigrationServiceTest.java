package uk.gov.hmcts.reform.fpl.service.document;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CHECKLIST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class UploadDocumentsMigrationServiceTest {

    private static final Document SOCIAL_WORK_CHRONOLOGY_DOCUMENT = documentWithUploadedFile();
    private static final Document SOCIAL_WORK_STATEMENT_DOCUMENT = documentWithUploadedFile();
    private static final Document SOCIAL_WORK_CARE_PLAN_DOCUMENT = documentWithUploadedFile();
    private static final Document SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT = documentWithUploadedFile();
    private static final Document THRESHOLD_DOCUMENT = documentWithUploadedFile();
    private static final Document CHECKLIST__DOCUMENT = documentWithUploadedFile();
    private static final Document SOCIAL_WORK_ASSESSMENT_DOCUMENT = documentWithUploadedFile();

    private static final Document SOCIAL_WORK_CHRONOLOGY_DOCUMENT_TO_FOLLOW = documentWithToFollow();
    private static final Document SOCIAL_WORK_STATEMENT_DOCUMENT_TO_FOLLOW = documentWithToFollow();
    private static final Document SOCIAL_WORK_CARE_PLAN_DOCUMENT_TO_FOLLOW = documentWithToFollow();
    private static final Document SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT_TO_FOLLOW = documentWithToFollow();
    private static final Document THRESHOLD_DOCUMENT_TO_FOLLOW = documentWithToFollow();
    private static final Document CHECKLIST__DOCUMENT_TO_FOLLOW = documentWithToFollow();
    private static final Document SOCIAL_WORK_ASSESSMENT_DOCUMENT_TO_FOLLOW = documentWithToFollow();

    private static final Document SOCIAL_WORK_CHRONOLOGY_DOCUMENT_TO_FOLLOW_WITH_FILE = documentWithToFollowWithFile();
    private static final Document SOCIAL_WORK_STATEMENT_DOCUMENT_TO_FOLLOW_WITH_FILE = documentWithToFollowWithFile();
    private static final Document SOCIAL_WORK_CARE_PLAN_DOCUMENT_TO_FOLLOW_WITH_FILE = documentWithToFollowWithFile();
    private static final Document SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT_TO_FOLLOW_WITH_FILE =
        documentWithToFollowWithFile();
    private static final Document THRESHOLD_DOCUMENT_TO_FOLLOW_WITH_FILE = documentWithToFollowWithFile();
    private static final Document CHECKLIST__DOCUMENT_TO_FOLLOW_WITH_FILE = documentWithToFollowWithFile();
    private static final Document SOCIAL_WORK_ASSESSMENT_DOCUMENT_TO_FOLLOW_WITH_FILE = documentWithToFollowWithFile();

    private static final Element<ApplicationDocument> CONVERTED_SOCIAL_WORK_CHRONOLOGY_DOCUMENT = element(mock(
        ApplicationDocument.class));
    private static final Element<ApplicationDocument> CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT = element(mock(
        ApplicationDocument.class));
    private static final Element<ApplicationDocument> CONVERTED_SOCIAL_WORK_CARE_PLAN_DOCUMENT = element(mock(
        ApplicationDocument.class));
    private static final Element<ApplicationDocument> CONVERTED_SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT = element(mock(
        ApplicationDocument.class));
    private static final Element<ApplicationDocument> CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT_2 = element(mock(
        ApplicationDocument.class));

    private static final Element<ApplicationDocument> CONVERTED_THRESHOLD_DOCUMENT =
        element(mock(ApplicationDocument.class));
    private static final Element<ApplicationDocument> CONVERTED_CHECKLIST__DOCUMENT =
        element(mock(ApplicationDocument.class));
    private static final List<Element<DocumentSocialWorkOther>> OTHER_SOCIAL_WORK_DOCUMENTS = wrapElements(mock(
        DocumentSocialWorkOther.class));
    private static final Element<ApplicationDocument> CONVERTED_OTHER_DOCUMENT_1 = element(mock(
        ApplicationDocument.class));
    private static final Element<ApplicationDocument> CONVERTED_OTHER_DOCUMENT_2 = element(mock(
        ApplicationDocument.class));

    private static final CourtBundle COURT_BUNDLE = CourtBundle.builder()
        .document(mock(DocumentReference.class))
        .build();
    private static final Element<CourtBundle> CONVERTED_COURT_BUNDLE = element(COURT_BUNDLE);

    @Mock
    private UploadDocumentTransformer transformer;

    @InjectMocks
    private UploadDocumentsMigrationService underTest;

    @Test
    void shouldTransformIfEmpty() {
        Map<String, Object> actual = underTest.transformFromOldCaseData(CaseData.builder().build());

        assertThat(actual).isEqualTo(Map.of(
            "applicationDocuments", Collections.emptyList(),
            "applicationDocumentsToFollowReason", ""
            )
        );
    }

    @Test
    void shouldTransformAllDocuments() {

        when(transformer.convert(SOCIAL_WORK_CHRONOLOGY_DOCUMENT, SOCIAL_WORK_CHRONOLOGY))
            .thenReturn(CONVERTED_SOCIAL_WORK_CHRONOLOGY_DOCUMENT);
        when(transformer.convert(SOCIAL_WORK_STATEMENT_DOCUMENT, SOCIAL_WORK_STATEMENT))
            .thenReturn(CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT);
        when(transformer.convert(SOCIAL_WORK_CARE_PLAN_DOCUMENT, CARE_PLAN))
            .thenReturn(CONVERTED_SOCIAL_WORK_CARE_PLAN_DOCUMENT);
        when(transformer.convert(SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT, SWET))
            .thenReturn(CONVERTED_SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT);
        // this is mapped to SOCIAL_WORK_STATEMENT
        when(transformer.convert(SOCIAL_WORK_ASSESSMENT_DOCUMENT, SOCIAL_WORK_STATEMENT))
            .thenReturn(CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT_2);
        when(transformer.convert(THRESHOLD_DOCUMENT, THRESHOLD))
            .thenReturn(CONVERTED_THRESHOLD_DOCUMENT);
        when(transformer.convert(CHECKLIST__DOCUMENT, CHECKLIST_DOCUMENT))
            .thenReturn(CONVERTED_CHECKLIST__DOCUMENT);
        when(transformer.convert(OTHER_SOCIAL_WORK_DOCUMENTS)).thenReturn(List.of(
            CONVERTED_OTHER_DOCUMENT_1, CONVERTED_OTHER_DOCUMENT_2));

        when(transformer.convert(COURT_BUNDLE)).thenReturn(CONVERTED_COURT_BUNDLE);

        Map<String, Object> actual = underTest.transformFromOldCaseData(CaseData.builder()
            .socialWorkChronologyDocument(SOCIAL_WORK_CHRONOLOGY_DOCUMENT)
            .socialWorkStatementDocument(SOCIAL_WORK_STATEMENT_DOCUMENT)
            .socialWorkCarePlanDocument(SOCIAL_WORK_CARE_PLAN_DOCUMENT)
            .socialWorkEvidenceTemplateDocument(SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT)
            .socialWorkAssessmentDocument(SOCIAL_WORK_ASSESSMENT_DOCUMENT)
            .thresholdDocument(THRESHOLD_DOCUMENT)
            .checklistDocument(CHECKLIST__DOCUMENT)
            .otherSocialWorkDocuments(OTHER_SOCIAL_WORK_DOCUMENTS)
            .courtBundle(COURT_BUNDLE)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "applicationDocuments", List.of(
                CONVERTED_SOCIAL_WORK_CHRONOLOGY_DOCUMENT,
                CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT,
                CONVERTED_SOCIAL_WORK_CARE_PLAN_DOCUMENT,
                CONVERTED_SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT,
                CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT_2,
                CONVERTED_THRESHOLD_DOCUMENT,
                CONVERTED_CHECKLIST__DOCUMENT,
                CONVERTED_OTHER_DOCUMENT_1,
                CONVERTED_OTHER_DOCUMENT_2
            ),
            "applicationDocumentsToFollowReason", "",
            "courtBundleList", List.of(CONVERTED_COURT_BUNDLE)
        ));
    }

    @Test
    void shouldTransformAllDocumentsWithFollow() {

        when(transformer.convert(OTHER_SOCIAL_WORK_DOCUMENTS)).thenReturn(List.of(
            CONVERTED_OTHER_DOCUMENT_1, CONVERTED_OTHER_DOCUMENT_2));

        Map<String, Object> actual = underTest.transformFromOldCaseData(CaseData.builder()
            .socialWorkChronologyDocument(SOCIAL_WORK_CHRONOLOGY_DOCUMENT_TO_FOLLOW)
            .socialWorkStatementDocument(SOCIAL_WORK_STATEMENT_DOCUMENT_TO_FOLLOW)
            .socialWorkCarePlanDocument(SOCIAL_WORK_CARE_PLAN_DOCUMENT_TO_FOLLOW)
            .socialWorkEvidenceTemplateDocument(SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT_TO_FOLLOW)
            .socialWorkAssessmentDocument(SOCIAL_WORK_ASSESSMENT_DOCUMENT_TO_FOLLOW)
            .thresholdDocument(THRESHOLD_DOCUMENT_TO_FOLLOW)
            .checklistDocument(CHECKLIST__DOCUMENT_TO_FOLLOW)
            .otherSocialWorkDocuments(OTHER_SOCIAL_WORK_DOCUMENTS)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "applicationDocuments", List.of(
                CONVERTED_OTHER_DOCUMENT_1,
                CONVERTED_OTHER_DOCUMENT_2
            ),
            "applicationDocumentsToFollowReason", "Social work chronology to follow,"
                + " Social work statement to follow,"
                + " Care plan to follow,"
                + " SWET to follow,"
                + " Social work statement to follow,"
                + " Threshold to follow,"
                + " Checklist document to follow"
        ));
    }

    @Test
    void shouldTransformAllDocumentsWithFileMarkedWithFollow() {

        when(transformer.convert(SOCIAL_WORK_CHRONOLOGY_DOCUMENT_TO_FOLLOW_WITH_FILE, SOCIAL_WORK_CHRONOLOGY))
            .thenReturn(CONVERTED_SOCIAL_WORK_CHRONOLOGY_DOCUMENT);
        when(transformer.convert(SOCIAL_WORK_STATEMENT_DOCUMENT_TO_FOLLOW_WITH_FILE, SOCIAL_WORK_STATEMENT))
            .thenReturn(CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT);
        when(transformer.convert(SOCIAL_WORK_CARE_PLAN_DOCUMENT_TO_FOLLOW_WITH_FILE, CARE_PLAN))
            .thenReturn(CONVERTED_SOCIAL_WORK_CARE_PLAN_DOCUMENT);
        when(transformer.convert(SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT_TO_FOLLOW_WITH_FILE, SWET))
            .thenReturn(CONVERTED_SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT);
        // this is mapped to SOCIAL_WORK_STATEMENT
        when(transformer.convert(SOCIAL_WORK_ASSESSMENT_DOCUMENT_TO_FOLLOW_WITH_FILE, SOCIAL_WORK_STATEMENT))
            .thenReturn(CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT_2);
        when(transformer.convert(THRESHOLD_DOCUMENT_TO_FOLLOW_WITH_FILE, THRESHOLD))
            .thenReturn(CONVERTED_THRESHOLD_DOCUMENT);
        when(transformer.convert(CHECKLIST__DOCUMENT_TO_FOLLOW_WITH_FILE, CHECKLIST_DOCUMENT))
            .thenReturn(CONVERTED_CHECKLIST__DOCUMENT);
        when(transformer.convert(OTHER_SOCIAL_WORK_DOCUMENTS)).thenReturn(List.of(
            CONVERTED_OTHER_DOCUMENT_1, CONVERTED_OTHER_DOCUMENT_2));

        Map<String, Object> actual = underTest.transformFromOldCaseData(CaseData.builder()
            .socialWorkChronologyDocument(SOCIAL_WORK_CHRONOLOGY_DOCUMENT_TO_FOLLOW_WITH_FILE)
            .socialWorkStatementDocument(SOCIAL_WORK_STATEMENT_DOCUMENT_TO_FOLLOW_WITH_FILE)
            .socialWorkCarePlanDocument(SOCIAL_WORK_CARE_PLAN_DOCUMENT_TO_FOLLOW_WITH_FILE)
            .socialWorkEvidenceTemplateDocument(SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT_TO_FOLLOW_WITH_FILE)
            .socialWorkAssessmentDocument(SOCIAL_WORK_ASSESSMENT_DOCUMENT_TO_FOLLOW_WITH_FILE)
            .thresholdDocument(THRESHOLD_DOCUMENT_TO_FOLLOW_WITH_FILE)
            .checklistDocument(CHECKLIST__DOCUMENT_TO_FOLLOW_WITH_FILE)
            .otherSocialWorkDocuments(OTHER_SOCIAL_WORK_DOCUMENTS)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "applicationDocuments", List.of(
                CONVERTED_SOCIAL_WORK_CHRONOLOGY_DOCUMENT,
                CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT,
                CONVERTED_SOCIAL_WORK_CARE_PLAN_DOCUMENT,
                CONVERTED_SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT,
                CONVERTED_SOCIAL_WORK_STATEMENT_DOCUMENT_2,
                CONVERTED_THRESHOLD_DOCUMENT,
                CONVERTED_CHECKLIST__DOCUMENT,
                CONVERTED_OTHER_DOCUMENT_1,
                CONVERTED_OTHER_DOCUMENT_2
            ),
            "applicationDocumentsToFollowReason", ""
        ));
    }

    @Test
    void shouldTransformMixedSelection() {

        when(transformer.convert(SOCIAL_WORK_CHRONOLOGY_DOCUMENT, SOCIAL_WORK_CHRONOLOGY))
            .thenReturn(CONVERTED_SOCIAL_WORK_CHRONOLOGY_DOCUMENT);
        when(transformer.convert(SOCIAL_WORK_CARE_PLAN_DOCUMENT_TO_FOLLOW_WITH_FILE, CARE_PLAN))
            .thenReturn(CONVERTED_SOCIAL_WORK_CARE_PLAN_DOCUMENT);

        Map<String, Object> actual = underTest.transformFromOldCaseData(CaseData.builder()
            .socialWorkChronologyDocument(SOCIAL_WORK_CHRONOLOGY_DOCUMENT)
            .socialWorkStatementDocument(SOCIAL_WORK_STATEMENT_DOCUMENT_TO_FOLLOW)
            .socialWorkCarePlanDocument(SOCIAL_WORK_CARE_PLAN_DOCUMENT_TO_FOLLOW_WITH_FILE)
            .socialWorkEvidenceTemplateDocument(SOCIAL_WORK_EVIDENCE_TEMPLATE_DOCUMENT_TO_FOLLOW)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "applicationDocuments", List.of(
                CONVERTED_SOCIAL_WORK_CHRONOLOGY_DOCUMENT,
                CONVERTED_SOCIAL_WORK_CARE_PLAN_DOCUMENT
            ),
            "applicationDocumentsToFollowReason", "Social work statement to follow, SWET to follow"
        ));
    }

    @Test
    void shouldTransformOneDocToFollow() {

        Map<String, Object> actual = underTest.transformFromOldCaseData(CaseData.builder()
            .socialWorkChronologyDocument(SOCIAL_WORK_CHRONOLOGY_DOCUMENT_TO_FOLLOW)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "applicationDocuments", Lists.emptyList(),
            "applicationDocumentsToFollowReason", "Social work chronology to follow"
        ));
    }

    private static Document documentWithUploadedFile() {
        return Document.builder()
            .typeOfDocument(mock(DocumentReference.class))
            .build();
    }

    private static Document documentWithToFollow() {
        return Document.builder()
            .documentStatus(DocumentStatus.TO_FOLLOW.getLabel())
            .build();
    }

    private static Document documentWithToFollowWithFile() {
        return Document.builder()
            .typeOfDocument(mock(DocumentReference.class))
            .documentStatus(DocumentStatus.TO_FOLLOW.getLabel())
            .build();
    }
}
