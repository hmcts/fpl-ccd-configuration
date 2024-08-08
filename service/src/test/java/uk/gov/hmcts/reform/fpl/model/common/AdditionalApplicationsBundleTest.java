package uk.gov.hmcts.reform.fpl.model.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AdditionalApplicationsBundleTest {

    private static final String APPLICANT_NAME = "John Smith";

    @ParameterizedTest
    @MethodSource("getC2BundleSubfields")
    void shouldGetCorrectApplicantNameIfInAnyC2Bundle(String subfield) {
        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .build();

        C2DocumentBundle c2Bundle = C2DocumentBundle.builder().applicantName(APPLICANT_NAME).build();

        ReflectionTestUtils.setField(bundle, subfield, c2Bundle);
        assertThat(bundle.getApplicantName()).isEqualTo(APPLICANT_NAME);
    }

    @Test
    void shouldReturnDefaultIfNoC2OrOtherBundleInApplication() {
        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .build();

        assertThat(bundle.getApplicantName()).isEqualTo("Applicant");
    }

    @Test
    void shouldGetApplicantNameFromOtherBundle() {
        AdditionalApplicationsBundle bundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicantName(APPLICANT_NAME)
                .build())
            .build();

        assertThat(bundle.getApplicantName()).isEqualTo(APPLICANT_NAME);
    }

    private static Stream<Arguments> getC2BundleSubfields() {
        Stream.Builder<Arguments> stream = Stream.builder();

        stream.add(Arguments.of("c2DocumentBundle"));
        stream.add(Arguments.of("c2DocumentBundleLA"));
        stream.add(Arguments.of("c2DocumentBundleConfidential"));
        for (int i = 0; i < 9; i++) {
            stream.add(Arguments.of("c2DocumentBundleResp" + i));
        }
        for (int i = 0; i < 15; i++) {
            stream.add(Arguments.of("c2DocumentBundleChild" + i));
        }
        return stream.build();
    }

}
