package uk.gov.hmcts.reform.fpl.service.translations.provider.decorator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class SupportingEvidenceBundleTranslatorDecoratorTest {

    private static final UUID SELECTED_ORDER_ID = UUID.randomUUID();
    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final String NAME = "Name";
    private static final LocalDateTime NOW = LocalDateTime.of(2012, 1, 2, 3, 4);
    private static final UUID ANOTHER_ID = UUID.randomUUID();

    private final Time time = mock(Time.class);

    private final Function<Element<SupportingEvidenceBundle>, Element<SupportingEvidenceBundle>>
        underTest = new SupportingEvidenceBundleTranslatorDecorator(time)
        .translatedBundle(DOCUMENT_REFERENCE, SELECTED_ORDER_ID);

    @Test
    void testWithMatchingId() {

        when(time.now()).thenReturn(NOW);
        Element<? extends SupportingEvidenceBundle> actual = underTest.apply(
            element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder()
                .name(NAME)
                .build())
        );

        assertThat(actual).isEqualTo(
            element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder()
                .name(NAME)
                .translatedDocument(DOCUMENT_REFERENCE)
                .translationUploadDateTime(NOW)
                .build()));
    }

    @Test
    void testWithAnotherIdLeaveUntouched() {

        when(time.now()).thenReturn(NOW);
        Element<? extends SupportingEvidenceBundle> actual = underTest.apply(
            element(ANOTHER_ID, SupportingEvidenceBundle.builder()
                .name(NAME)
                .build())
        );

        assertThat(actual).isEqualTo(
            element(ANOTHER_ID, SupportingEvidenceBundle.builder()
                .name(NAME)
                .build()));

    }
}
