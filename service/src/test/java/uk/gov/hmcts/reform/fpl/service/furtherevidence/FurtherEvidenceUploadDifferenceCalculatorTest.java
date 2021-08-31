package uk.gov.hmcts.reform.fpl.service.furtherevidence;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class FurtherEvidenceUploadDifferenceCalculatorTest {

    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final UUID UUID_3 = UUID.randomUUID();
    private static final DocumentReference DOCUMENT_1 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_2 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_3 = mock(DocumentReference.class);
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS = ENGLISH_TO_WELSH;
    private static final Element<SupportingEvidenceBundle> ELEMENT_WITH_TRANSLATION_REQUEST = element(UUID_1,
        SupportingEvidenceBundle.builder()
            .document(DOCUMENT_1)
            .translationRequirements(TRANSLATION_REQUIREMENTS)
            .build());

    private static final Element<SupportingEvidenceBundle> ELEMENT_WITH_TRANSLATION_REQUEST_NEW_DOC = element(UUID_1,
        SupportingEvidenceBundle.builder()
            .document(DOCUMENT_2)
            .translationRequirements(TRANSLATION_REQUIREMENTS)
            .build());

    private static final Element<SupportingEvidenceBundle> ELEMENT_WITHOUT_TRANSLATION_REQUEST = element(UUID_1,
        SupportingEvidenceBundle.builder()
            .document(DOCUMENT_1)
            .build());

    private final FurtherEvidenceUploadDifferenceCalculator underTest = new FurtherEvidenceUploadDifferenceCalculator();

    @Test
    void testNoDocuments() {
        List<Element<SupportingEvidenceBundle>> actual = underTest.calculate(CaseData.builder().build(),
            CaseData.builder().build());

        assertThat(actual).isEqualTo(List.of());

    }

    @Test
    void testAddedFurtherEvidenceDocumentWithNoTranslationRequirement() {
        List<Element<SupportingEvidenceBundle>> actual = underTest.calculate(CaseData.builder()
                .furtherEvidenceDocuments(List.of(ELEMENT_WITHOUT_TRANSLATION_REQUEST))
                .build(),
            CaseData.builder().build());

        assertThat(actual).isEqualTo(List.of());

    }

    @Test
    void testAddedFurtherEvidenceDocumentWithTranslationRequirementsButNotChanged() {
        List<Element<SupportingEvidenceBundle>> actual = underTest.calculate(CaseData.builder()
                .furtherEvidenceDocuments(List.of(ELEMENT_WITHOUT_TRANSLATION_REQUEST))
                .build(),
            CaseData.builder()
                .furtherEvidenceDocuments(List.of(ELEMENT_WITHOUT_TRANSLATION_REQUEST))
                .build()
        );

        assertThat(actual).isEqualTo(List.of());

    }

    @Test
    void testAddedFurtherEvidenceDocumentWithTranslationRequirement() {
        List<Element<SupportingEvidenceBundle>> actual = underTest.calculate(CaseData.builder()
                .furtherEvidenceDocuments(List.of(
                    ELEMENT_WITH_TRANSLATION_REQUEST
                )).build(),
            CaseData.builder().build());

        assertThat(actual).isEqualTo(List.of(
            ELEMENT_WITH_TRANSLATION_REQUEST
        ));

    }

    @Test
    void testAddedFurtherEvidenceDocumentWithTranslationRequirementChangedToRequested() {
        List<Element<SupportingEvidenceBundle>> actual = underTest.calculate(CaseData.builder()
                .furtherEvidenceDocuments(List.of(
                    ELEMENT_WITH_TRANSLATION_REQUEST
                )).build(),
            CaseData.builder()
                .furtherEvidenceDocuments(List.of(
                    ELEMENT_WITHOUT_TRANSLATION_REQUEST
                )).build());

        assertThat(actual).isEqualTo(List.of(
            ELEMENT_WITH_TRANSLATION_REQUEST
        ));

    }

    @Test
    void testAddedFurtherEvidenceDocumentWithTranslationRequirementFileSubstituted() {
        List<Element<SupportingEvidenceBundle>> actual = underTest.calculate(CaseData.builder()
                .furtherEvidenceDocuments(List.of(
                    ELEMENT_WITH_TRANSLATION_REQUEST_NEW_DOC
                )).build(),
            CaseData.builder()
                .furtherEvidenceDocuments(List.of(
                    ELEMENT_WITH_TRANSLATION_REQUEST
                )).build());

        assertThat(actual).isEqualTo(List.of(
            ELEMENT_WITH_TRANSLATION_REQUEST_NEW_DOC
        ));

    }

    @Test
    void testAddedAllNewResourcesWithTranslationRequirement() {
        List<Element<SupportingEvidenceBundle>> actual = underTest.calculate(CaseData.builder()
                .furtherEvidenceDocuments(List.of(
                    element(UUID_1,
                        SupportingEvidenceBundle.builder()
                            .document(DOCUMENT_1)
                            .translationRequirements(TRANSLATION_REQUIREMENTS)
                            .build())
                )).respondentStatements(List.of(element(
                        RespondentStatement.builder()
                            .supportingEvidenceBundle(List.of(
                                element(UUID_2,
                                    SupportingEvidenceBundle.builder()
                                        .document(DOCUMENT_2)
                                        .translationRequirements(TRANSLATION_REQUIREMENTS)
                                        .build())
                            )).build()
                    ))
                ).hearingFurtherEvidenceDocuments(
                    List.of(element(HearingFurtherEvidenceBundle.builder()
                        .supportingEvidenceBundle(List.of(
                            element(UUID_3,
                                SupportingEvidenceBundle.builder()
                                    .document(DOCUMENT_3)
                                    .translationRequirements(TRANSLATION_REQUIREMENTS)
                                    .build())
                        )).build()))
                ).build(),
            CaseData.builder().build());

        assertThat(actual).isEqualTo(List.of(
            element(UUID_3,
                SupportingEvidenceBundle.builder()
                    .document(DOCUMENT_3)
                    .translationRequirements(TRANSLATION_REQUIREMENTS)
                    .build()),
            element(UUID_2,
                SupportingEvidenceBundle.builder()
                    .document(DOCUMENT_2)
                    .translationRequirements(TRANSLATION_REQUIREMENTS)
                    .build()),
            element(UUID_1,
                SupportingEvidenceBundle.builder()
                    .document(DOCUMENT_1)
                    .translationRequirements(TRANSLATION_REQUIREMENTS)
                    .build())


        ));

    }
}
