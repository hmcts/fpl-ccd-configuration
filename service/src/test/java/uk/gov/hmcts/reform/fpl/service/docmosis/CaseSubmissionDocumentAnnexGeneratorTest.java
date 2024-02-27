package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisAnnexDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisAnnexDocuments;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class CaseSubmissionDocumentAnnexGeneratorTest {

    private static final ApplicationDocumentType DOCUMENT_TYPE = ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
    private static final ApplicationDocumentType ANOTHER_DOCUMENT_TYPE = ApplicationDocumentType.CHECKLIST_DOCUMENT;
    private static final String ATTACHED = "Attached";
    private static final String OTHER_DOC_TITLE_1 = "AnotherDoc1";
    private static final String OTHER_DOC_TITLE_2 = "AnotherDoc2";
    private static final String FOLLOW_REASON = "FollowReason";
    private static final Language LANGUAGE = Language.ENGLISH;

    private final CaseSubmissionDocumentAnnexGenerator underTest = new CaseSubmissionDocumentAnnexGenerator();

    @Test
    void testIfNoDocuments() {
        DocmosisAnnexDocuments actual = underTest.generate(CaseData.builder()
            .temporaryApplicationDocuments(null)
            .build(),
            LANGUAGE);

        assertThat(actual).isEqualTo(DocmosisAnnexDocuments.builder()
            .documents(Collections.emptyList())
            .build());
    }

    @Test
    void testIfToFollowPresent() {
        DocmosisAnnexDocuments actual = underTest.generate(CaseData.builder()
            .applicationDocumentsToFollowReason(FOLLOW_REASON)
            .build(),
            LANGUAGE);

        assertThat(actual).isEqualTo(DocmosisAnnexDocuments.builder()
            .documents(Collections.emptyList())
            .toFollowReason(FOLLOW_REASON)
            .build());
    }

    @Test
    void testIfSinglePopulated() {
        DocmosisAnnexDocuments actual = underTest.generate(CaseData.builder()
            .temporaryApplicationDocuments(List.of(
                documentWithType(DOCUMENT_TYPE)
            )).build(),
            LANGUAGE);

        assertThat(actual).isEqualTo(DocmosisAnnexDocuments.builder()
            .documents(List.of(
                annexWith(DOCUMENT_TYPE.getLabel(), ATTACHED)
            ))
            .build());
    }

    @Test
    void testIfSingleOtherDocumentPopulated() {
        DocmosisAnnexDocuments actual = underTest.generate(CaseData.builder()
            .temporaryApplicationDocuments(List.of(
                otherDocument(OTHER_DOC_TITLE_1),
                otherDocument(OTHER_DOC_TITLE_2)
            )).build(),
            LANGUAGE);

        assertThat(actual).isEqualTo(DocmosisAnnexDocuments.builder()
            .documents(List.of(
                annexWith(OTHER_DOC_TITLE_1, ATTACHED),
                annexWith(OTHER_DOC_TITLE_2, ATTACHED)
            ))
            .build());
    }

    @Test
    void testIfMultiplePopulated() {
        DocmosisAnnexDocuments actual = underTest.generate(CaseData.builder()
            .temporaryApplicationDocuments(List.of(
                documentWithType(DOCUMENT_TYPE),
                documentWithType(ANOTHER_DOCUMENT_TYPE)
            )).build(),
            LANGUAGE);

        assertThat(actual).isEqualTo(DocmosisAnnexDocuments.builder()
            .documents(List.of(
                annexWith(DOCUMENT_TYPE.getLabel(), "Attached"),
                annexWith(ANOTHER_DOCUMENT_TYPE.getLabel(), "Attached")
            ))
            .build());
    }

    @Test
    void testIfMultiplePopulatedWelsh() {
        DocmosisAnnexDocuments actual = underTest.generate(CaseData.builder()
                .temporaryApplicationDocuments(List.of(
                    documentWithType(DOCUMENT_TYPE),
                    documentWithType(ANOTHER_DOCUMENT_TYPE)
                )).build(),
            Language.WELSH);

        assertThat(actual).isEqualTo(DocmosisAnnexDocuments.builder()
            .documents(List.of(
                annexWith(DOCUMENT_TYPE.getLabel(Language.WELSH), "Ynghlwm"),
                annexWith(ANOTHER_DOCUMENT_TYPE.getLabel(Language.WELSH), "Ynghlwm")
            ))
            .build());
    }

    @Test
    void testIfMultipleOfSamePopulated() {
        DocmosisAnnexDocuments actual = underTest.generate(CaseData.builder()
            .temporaryApplicationDocuments(List.of(
                documentWithType(DOCUMENT_TYPE),
                documentWithType(DOCUMENT_TYPE)
            )).build(),
            LANGUAGE);

        assertThat(actual).isEqualTo(DocmosisAnnexDocuments.builder()
            .documents(List.of(
                annexWith(DOCUMENT_TYPE.getLabel(), "2 attached")
            ))
            .build());
    }

    @Test
    void testIfMultipleOfSamePopulatedWelsh() {
        DocmosisAnnexDocuments actual = underTest.generate(CaseData.builder()
                .temporaryApplicationDocuments(List.of(
                    documentWithType(DOCUMENT_TYPE),
                    documentWithType(DOCUMENT_TYPE)
                )).build(),
            Language.WELSH);

        assertThat(actual).isEqualTo(DocmosisAnnexDocuments.builder()
            .documents(List.of(
                annexWith(DOCUMENT_TYPE.getLabel(Language.WELSH), "2 ynghlwm")
            ))
            .build());
    }

    private DocmosisAnnexDocument annexWith(String title, String description) {
        return DocmosisAnnexDocument.builder()
            .title(title)
            .description(description)
            .build();
    }

    private Element<ApplicationDocument> documentWithType(ApplicationDocumentType documentType) {
        return element(ApplicationDocument.builder()
            .documentType(documentType)
            .build());
    }

    private Element<ApplicationDocument> otherDocument(String documentName) {
        return element(ApplicationDocument.builder()
            .documentType(ApplicationDocumentType.OTHER)
            .documentName(documentName)
            .build());
    }
}
