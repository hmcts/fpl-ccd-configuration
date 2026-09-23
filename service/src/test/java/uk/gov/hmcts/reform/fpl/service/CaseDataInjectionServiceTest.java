package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cdi.CdiMetadataField;
import uk.gov.hmcts.reform.fpl.model.cdi.CaseDataInjectionResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDataInjectionServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-11T00:00:00Z"), ZoneOffset.UTC);
    private final CaseDataInjectionService underTest = new CaseDataInjectionService(clock);

    @Test
    void shouldGenerateGreenBandWhenCaseAgeIsUnder26Weeks() {
        CaseData caseData = CaseData.builder()
            .dateSubmitted(LocalDate.of(2026, 4, 1))
            .build();

        Map<String, CdiMetadataField> fieldsById = toMap(underTest.generate(caseData));

        assertThat(fieldsById.get("caseSummaryCaseAgeInjected").getValue()).isEqualTo("23");
        assertThat(fieldsById.get("caseSummaryCaseAgeBandInjected").getValue()).isEqualTo("GREEN");
        assertThat(fieldsById.get("caseSummaryCaseAgeStatusLabel").getValue()).isEqualTo("Under 26 weeks");
    }

    @Test
    void shouldGenerateAmberBandWhenCaseAgeIsBetween26And52Weeks() {
        CaseData caseData = CaseData.builder()
            .dateSubmitted(LocalDate.of(2026, 2, 27))
            .build();

        Map<String, CdiMetadataField> fieldsById = toMap(underTest.generate(caseData));

        assertThat(fieldsById.get("caseSummaryCaseAgeInjected").getValue()).isEqualTo("28");
        assertThat(fieldsById.get("caseSummaryCaseAgeBandInjected").getValue()).isEqualTo("AMBER");
        assertThat(fieldsById.get("caseSummaryCaseAgeStatusLabel").getValue()).isEqualTo("26 to 52 weeks");
    }

    @Test
    void shouldGenerateRedBandWhenCaseAgeIsOver52Weeks() {
        CaseData caseData = CaseData.builder()
            .dateSubmitted(LocalDate.of(2025, 8, 31))
            .build();

        Map<String, CdiMetadataField> fieldsById = toMap(underTest.generate(caseData));

        assertThat(fieldsById.get("caseSummaryCaseAgeInjected").getValue()).isEqualTo("53");
        assertThat(fieldsById.get("caseSummaryCaseAgeBandInjected").getValue()).isEqualTo("RED");
        assertThat(fieldsById.get("caseSummaryCaseAgeStatusLabel").getValue()).isEqualTo("Over 52 weeks");
    }

    @Test
    void shouldReturnEmptyBandWhenDateSubmittedIsMissing() {
        Map<String, CdiMetadataField> fieldsById = toMap(underTest.generate(CaseData.builder().build()));

        assertThat(fieldsById.get("caseSummaryCaseAgeInjected").getValue()).isEqualTo("");
        assertThat(fieldsById.get("caseSummaryCaseAgeBandInjected").getValue()).isEqualTo("");
        assertThat(fieldsById.get("caseSummaryCaseAgeStatusLabel").getValue()).isEqualTo("");
    }

    private Map<String, CdiMetadataField> toMap(CaseDataInjectionResponse response) {
        return response.getMetadataFields().stream().collect(Collectors.toMap(CdiMetadataField::getId, f -> f));
    }
}

