package uk.gov.hmcts.reform.fpl.service.summary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaseSummaryServiceTest {

    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY0 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY1 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY2 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY3 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY4 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY5 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY6 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY7 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY8 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY9 = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY10 = mock(SyntheticCaseSummary.class);

    private final CaseSummaryCaseFlagGenerator caseSummaryCaseFlagGenerator = mock(
        CaseSummaryCaseFlagGenerator.class);
    private final CaseSummaryOrdersRequestedGenerator caseSummaryOrdersRequestedGenerator = mock(
        CaseSummaryOrdersRequestedGenerator.class);
    private final CaseSummaryDeadlineGenerator caseSummaryDeadlineGenerator = mock(CaseSummaryDeadlineGenerator.class);
    private final CaseSummaryJudgeInformationGenerator caseSummaryJudgeInformationGenerator = mock(
        CaseSummaryJudgeInformationGenerator.class);
    private final CaseSummaryMessagesGenerator caseSummaryMessagesGenerator = mock(CaseSummaryMessagesGenerator.class);
    private final CaseSummaryNextHearingGenerator caseSummaryNextHearingGenerator =
        mock(CaseSummaryNextHearingGenerator.class);
    private final CaseSummaryWelshFlagGenerator caseSummaryWelshFlagGenerator =
        mock(CaseSummaryWelshFlagGenerator.class);
    private final CaseSummaryPreviousHearingGenerator caseSummaryPreviousHearingGenerator = mock(
        CaseSummaryPreviousHearingGenerator.class);
    private final CaseSummaryFinalHearingGenerator caseSummaryFinalHearingGenerator = mock(
        CaseSummaryFinalHearingGenerator.class);
    private final CaseSummaryPeopleInCaseGenerator caseSummaryPeopleInCaseGenerator = mock(
        CaseSummaryPeopleInCaseGenerator.class);
    private final CaseSummaryCourtGenerator caseSummaryCourtGenerator = mock(
        CaseSummaryCourtGenerator.class);
    private final CaseSummaryHighCourtCaseFlagGenerator caseSummaryHighCourtCaseFlagGenerator = mock(
        CaseSummaryHighCourtCaseFlagGenerator.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);

    private CaseSummaryService underTest = new CaseSummaryService(
        caseSummaryCaseFlagGenerator,
        caseSummaryOrdersRequestedGenerator,
        caseSummaryDeadlineGenerator,
        caseSummaryJudgeInformationGenerator,
        caseSummaryMessagesGenerator,
        caseSummaryNextHearingGenerator,
        caseSummaryPreviousHearingGenerator,
        caseSummaryFinalHearingGenerator,
        caseSummaryWelshFlagGenerator,
        caseSummaryPeopleInCaseGenerator,
        caseSummaryCourtGenerator,
        caseSummaryHighCourtCaseFlagGenerator,
        objectMapper
    );

    @BeforeEach
    void setUp() {
        when(caseSummaryOrdersRequestedGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY0);
        when(caseSummaryDeadlineGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY1);
        when(caseSummaryJudgeInformationGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY2);
        when(caseSummaryMessagesGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY3);
        when(caseSummaryNextHearingGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY4);
        when(caseSummaryPreviousHearingGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY5);
        when(caseSummaryFinalHearingGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY6);
        when(caseSummaryPeopleInCaseGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY7);
        when(caseSummaryCourtGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY8);
        when(caseSummaryCaseFlagGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY9);
        when(caseSummaryWelshFlagGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY9);
        when(caseSummaryHighCourtCaseFlagGenerator.generate(CASE_DATA)).thenReturn(SYNTHETIC_CASE_SUMMARY10);

        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY0),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field0", "value0"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY1),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field1", "value1"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY2),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field2", "value2"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY3),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field3", "value3"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY4),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field4", "value4"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY5),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field5", "value5"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY6),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field6", "value6"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY7),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field7", "value7"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY8),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field8", "value8"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY9),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field9", "value9"));
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY10),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(Map.of("field10", "value10"));
    }

    @Test
    void testAutoCalculatedCaseSummaryLAHiddenWhenWelshFlagAndHighCourtFlagAreOff() {
        SyntheticCaseSummary welshFlagData = SyntheticCaseSummary.builder()
            .caseSummaryLALanguageRequirement("Yes")
            .caseSummaryLanguageRequirement("Yes")
            .build();
        SyntheticCaseSummary highCourtFlagData = SyntheticCaseSummary.builder()
            .caseSummaryLAHighCourtCase("Yes")
            .caseSummaryHighCourtCase("Yes")
            .build();
        when(caseSummaryWelshFlagGenerator.generate(CASE_DATA)).thenReturn(welshFlagData);
        when(caseSummaryHighCourtCaseFlagGenerator.generate(CASE_DATA)).thenReturn(highCourtFlagData);
        when(objectMapper.convertValue(eq(welshFlagData),
            Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(Map.of("caseSummaryLALanguageRequirement", "No"));
        when(objectMapper.convertValue(eq(highCourtFlagData),
            Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(Map.of("caseSummaryLAHighCourtCase", "No"));

        Map<String, Object> actual = underTest.generateSummaryFields(CASE_DATA);
        assertThat(actual).isEqualTo(Map.ofEntries(
            entry("field0", "value0"),
            entry("field1", "value1"),
            entry("field2", "value2"),
            entry("field3", "value3"),
            entry("field4", "value4"),
            entry("field5", "value5"),
            entry("field6", "value6"),
            entry("field7", "value7"),
            entry("field8", "value8"),
            entry("field9", "value9"),
            entry("caseSummaryLAHighCourtCase", "No"),
            entry("caseSummaryLALanguageRequirement", "No"),
            entry("caseSummaryLATabHidden", "Yes")
        ));
    }

    @Test
    void testAutoCalculatedCaseSummaryLAHiddenWhenWelshFlagAndHighCourtFlagAreOn() {
        SyntheticCaseSummary welshFlagData = SyntheticCaseSummary.builder()
            .caseSummaryLALanguageRequirement("Yes")
            .caseSummaryLanguageRequirement("Yes")
            .build();
        SyntheticCaseSummary highCourtFlagData = SyntheticCaseSummary.builder()
            .caseSummaryLAHighCourtCase("Yes")
            .caseSummaryHighCourtCase("Yes")
            .build();
        when(caseSummaryWelshFlagGenerator.generate(CASE_DATA)).thenReturn(welshFlagData);
        when(caseSummaryHighCourtCaseFlagGenerator.generate(CASE_DATA)).thenReturn(highCourtFlagData);
        when(objectMapper.convertValue(eq(welshFlagData),
            Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(Map.of("caseSummaryLALanguageRequirement", "Yes"));
        when(objectMapper.convertValue(eq(highCourtFlagData),
            Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(Map.of("caseSummaryLAHighCourtCase", "Yes"));

        Map<String, Object> actual = underTest.generateSummaryFields(CASE_DATA);
        assertThat(actual).isEqualTo(Map.ofEntries(
            entry("field0", "value0"),
            entry("field1", "value1"),
            entry("field2", "value2"),
            entry("field3", "value3"),
            entry("field4", "value4"),
            entry("field5", "value5"),
            entry("field6", "value6"),
            entry("field7", "value7"),
            entry("field8", "value8"),
            entry("field9", "value9"),
            entry("caseSummaryLAHighCourtCase", "Yes"),
            entry("caseSummaryLALanguageRequirement", "Yes"),
            entry("caseSummaryLATabHidden", "No")
        ));
    }

    @Test
    void testAutoCalculatedCaseSummaryLAHiddenWhenWelshFlagIsOn() {
        SyntheticCaseSummary welshFlagData = SyntheticCaseSummary.builder()
            .caseSummaryLALanguageRequirement("Yes")
            .caseSummaryLanguageRequirement("Yes")
            .build();
        SyntheticCaseSummary highCourtFlagData = SyntheticCaseSummary.builder()
            .caseSummaryLAHighCourtCase("Yes")
            .caseSummaryHighCourtCase("Yes")
            .build();
        when(caseSummaryWelshFlagGenerator.generate(CASE_DATA)).thenReturn(welshFlagData);
        when(caseSummaryHighCourtCaseFlagGenerator.generate(CASE_DATA)).thenReturn(highCourtFlagData);
        when(objectMapper.convertValue(eq(welshFlagData),
            Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(Map.of("caseSummaryLALanguageRequirement", "Yes"));
        when(objectMapper.convertValue(eq(highCourtFlagData),
            Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(Map.of("caseSummaryLAHighCourtCase", "No"));

        Map<String, Object> actual = underTest.generateSummaryFields(CASE_DATA);
        assertThat(actual).isEqualTo(Map.ofEntries(
            entry("field0", "value0"),
            entry("field1", "value1"),
            entry("field2", "value2"),
            entry("field3", "value3"),
            entry("field4", "value4"),
            entry("field5", "value5"),
            entry("field6", "value6"),
            entry("field7", "value7"),
            entry("field8", "value8"),
            entry("field9", "value9"),
            entry("caseSummaryLAHighCourtCase", "No"),
            entry("caseSummaryLALanguageRequirement", "Yes"),
            entry("caseSummaryLATabHidden", "No")
        ));
    }

    @Test
    void testAutoCalculatedCaseSummaryLAHiddenWhenHighCourtFlagIsOn() {
        SyntheticCaseSummary welshFlagData = SyntheticCaseSummary.builder()
            .caseSummaryLALanguageRequirement("Yes")
            .caseSummaryLanguageRequirement("Yes")
            .build();
        SyntheticCaseSummary highCourtFlagData = SyntheticCaseSummary.builder()
            .caseSummaryLAHighCourtCase("Yes")
            .caseSummaryHighCourtCase("Yes")
            .build();
        when(caseSummaryWelshFlagGenerator.generate(CASE_DATA)).thenReturn(welshFlagData);
        when(caseSummaryHighCourtCaseFlagGenerator.generate(CASE_DATA)).thenReturn(highCourtFlagData);
        when(objectMapper.convertValue(eq(welshFlagData),
            Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(Map.of("caseSummaryLALanguageRequirement", "No"));
        when(objectMapper.convertValue(eq(highCourtFlagData),
            Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(Map.of("caseSummaryLAHighCourtCase", "Yes"));

        Map<String, Object> actual = underTest.generateSummaryFields(CASE_DATA);
        assertThat(actual).isEqualTo(Map.ofEntries(
            entry("field0", "value0"),
            entry("field1", "value1"),
            entry("field2", "value2"),
            entry("field3", "value3"),
            entry("field4", "value4"),
            entry("field5", "value5"),
            entry("field6", "value6"),
            entry("field7", "value7"),
            entry("field8", "value8"),
            entry("field9", "value9"),
            entry("caseSummaryLAHighCourtCase", "Yes"),
            entry("caseSummaryLALanguageRequirement", "No"),
            entry("caseSummaryLATabHidden", "No")
        ));
    }

    @Test
    void testWhenAllGeneratedFieldsAreDisjointed() {

        Map<String, Object> actual = underTest.generateSummaryFields(CASE_DATA);

        assertThat(actual).isEqualTo(Map.ofEntries(
            entry("field0", "value0"),
            entry("field1", "value1"),
            entry("field2", "value2"),
            entry("field3", "value3"),
            entry("field4", "value4"),
            entry("field5", "value5"),
            entry("field6", "value6"),
            entry("field7", "value7"),
            entry("field8", "value8"),
            entry("field9", "value9"),
            entry("field10", "value10")
        ));
    }

    @Test
    void testWhenAllGeneratedFieldsWithNullValuesWillKeep() {

        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY0), Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(new HashMap<>() {
                {
                    this.put("field0", "value0");
                    this.put("fieldNull", null);
                }
            });


        Map<String, String> expected = new HashMap<>();
        expected.put("field0", "value0");
        expected.put("fieldNull", null);
        expected.put("field1", "value1");
        expected.put("field2", "value2");
        expected.put("field3", "value3");
        expected.put("field4", "value4");
        expected.put("field5", "value5");
        expected.put("field6", "value6");
        expected.put("field7", "value7");
        expected.put("field8", "value8");
        expected.put("field9", "value9");
        expected.put("field10", "value10");

        Map<String, Object> actual = underTest.generateSummaryFields(CASE_DATA);

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    void testWhenAllGeneratedFieldsWithNullValuesWillOverrideWhenOtherNonNull() {

        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY0), Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(new HashMap<>() {
                {
                    this.put("field0", "value0");
                    this.put("field1", null);
                }
            });

        Map<String, Object> actual = underTest.generateSummaryFields(CASE_DATA);

        assertThat(actual).isEqualTo(Map.ofEntries(
            entry("field0", "value0"),
            entry("field1", "value1"),
            entry("field2", "value2"),
            entry("field3", "value3"),
            entry("field4", "value4"),
            entry("field5", "value5"),
            entry("field6", "value6"),
            entry("field7", "value7"),
            entry("field8", "value8"),
            entry("field9", "value9"),
            entry("field10", "value10")
        ));

    }

    @Test
    void testWhenAllGeneratedFieldsOverrides() {

        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY0), Mockito.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(new HashMap<>() {
                {
                    this.put("field0", "value0");
                    this.put("field1", "FIELD_WILL_NOT_OVERRIDE");
                }
            });

        Map<String, Object> actual = underTest.generateSummaryFields(CASE_DATA);

        assertThat(actual).isEqualTo(Map.ofEntries(
                entry("field0", "value0"),
                entry("field1", "FIELD_WILL_NOT_OVERRIDE"),
                entry("field2", "value2"),
                entry("field3", "value3"),
                entry("field4", "value4"),
                entry("field5", "value5"),
                entry("field6", "value6"),
                entry("field7", "value7"),
                entry("field8", "value8"),
                entry("field9", "value9"),
                entry("field10", "value10")
        ));

    }
}
