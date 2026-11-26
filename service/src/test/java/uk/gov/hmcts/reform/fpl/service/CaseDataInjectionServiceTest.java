package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cdi.CdiMetadataField;
import uk.gov.hmcts.reform.fpl.model.cdi.CaseDataInjectionResponse;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDataInjectionServiceTest {

    private final CaseDataInjectionService underTest = new CaseDataInjectionService();

    @Test
    void shouldGenerateGreenBandWhenCaseAgeIsUnder26Weeks() {
        CaseData caseData = CaseData.builder()
            .dateSubmitted(LocalDate.of(2026, 4, 1))
            .build();

        Map<String, CdiMetadataField> fieldsById = toMap(underTest.generate(caseData));

        assertThat(fieldsById.get("caseSummaryCaseAgeBandInjected").getValue()).isEqualTo("GREEN");
    }

    @Test
    void shouldGenerateAmberBandWhenCaseAgeIsBetween26And52Weeks() {
        CaseData caseData = CaseData.builder()
            .dateSubmitted(LocalDate.of(2026, 2, 27))
            .build();

        Map<String, CdiMetadataField> fieldsById = toMap(underTest.generate(caseData));

        assertThat(fieldsById.get("caseSummaryCaseAgeBandInjected").getValue()).isEqualTo("AMBER");
    }

    @Test
    void shouldGenerateRedBandWhenCaseAgeIsOver52Weeks() {
        CaseData caseData = CaseData.builder()
            .dateSubmitted(LocalDate.of(2025, 8, 31))
            .build();

        Map<String, CdiMetadataField> fieldsById = toMap(underTest.generate(caseData));

        assertThat(fieldsById.get("caseSummaryCaseAgeBandInjected").getValue()).isEqualTo("RED");
    }

    private Map<String, CdiMetadataField> toMap(CaseDataInjectionResponse response) {
        return response.getMetadataFields().stream().collect(Collectors.toMap(CdiMetadataField::getId, f -> f));
    }
}

