package uk.gov.hmcts.reform.fpl.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DocumentBundleTest {

    @Test
    void testC6aLabel() {
        DocumentBundle documentBundle = DocumentBundle.builder()
            .document(DocumentReference.builder()
                .filename("adfsjlkdsa_c6a.pdf")
                .build())
            .build();

        String actual = documentBundle.asLabel();

        assertThat(actual).isEqualTo("Notice of proceedings (C6A)");
    }

    @Test
    void testOtherLabel() {
        DocumentBundle documentBundle = DocumentBundle.builder()
            .document(DocumentReference.builder()
                .filename("adfsjlkdsa_c6.pdf")
                .build())
            .build();

        String actual = documentBundle.asLabel();

        assertThat(actual).isEqualTo("Notice of proceedings (C6)");
    }

    @Test
    void testLabelNoDocument() {
        DocumentBundle documentBundle = DocumentBundle.builder()
            .document(null)
            .build();

        String actual = documentBundle.asLabel();

        assertThat(actual).isEqualTo("");
    }
}
