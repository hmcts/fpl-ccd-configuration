package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.FieldsGroup;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.TempNullify;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.addFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInGatekeepingState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInOpenState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInReturnedState;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.nullifyTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

class CaseDetailsHelperTest {
    private static final String EXCEPTION_MESSAGE = "CCD Case number must be 16 digits long";

    @Nested
    class FormatCCDCaseNumber {

        @Test
        void shouldFormatCaseNumberWhen16DigitsProvided() {
            String formattedCaseNumber = formatCCDCaseNumber(1234123412341234L);
            assertThat(formattedCaseNumber).isEqualTo("1234-1234-1234-1234");
        }

        @Test
        void shouldThrowAnExceptionIfCaseNumberExceeds16Digits() {
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> formatCCDCaseNumber(12341234123412341L));

            assertThat(exception.getMessage()).isEqualTo(EXCEPTION_MESSAGE);
        }

        @Test
        void shouldThrowAnExceptionIfCaseNumberIsLessThan16Digits() {
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> formatCCDCaseNumber(123412341234123L));

            assertThat(exception.getMessage()).isEqualTo(EXCEPTION_MESSAGE);
        }

        @Test
        void shouldThrowAnExceptionIfCaseNumberIsNull() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> formatCCDCaseNumber(null));

            assertThat(exception.getMessage()).isEqualTo(EXCEPTION_MESSAGE);
        }
    }

    @Nested
    class IsCaseNumber {

        @Test
        void shouldReturnTrueWhenStringIs16DigitNumber() {
            assertThat(isCaseNumber("1234567898765432")).isTrue();
        }

        @Test
        void shouldReturnFalseIfStringIsNotANumber() {
            assertThat(isCaseNumber("111-111-111-111")).isFalse();
        }

        @Test
        void shouldReturnTrueWhenStringIsNot16DigitNumber() {
            assertThat(isCaseNumber("123456789")).isFalse();
        }
    }

    @Nested
    class RemoveTemporaryFields {
        private Map<String, Object> data = new HashMap<>();
        private CaseDetails caseDetails = CaseDetails.builder().data(data).build();

        @BeforeEach
        void populateMap() {
            data.put("key1", "some value 1");
            data.put("key2", "some value 2");
            data.put("key3", 3);
            data.put("key4", "4");
        }

        @AfterEach
        void clearMap() {
            data.clear();
        }

        @Test
        void shouldRemoveFieldsFromCaseDataMapWhenPresent() {
            removeTemporaryFields(caseDetails, "key1", "key2", "key3", "key4");

            assertThat(caseDetails.getData()).isEmpty();
        }

        @Test
        void shouldNotRemoveFieldsThatArePresentInMapWhenNotPassed() {
            removeTemporaryFields(caseDetails, "key1", "key3", "key4");

            assertThat(caseDetails.getData()).containsOnly(Map.entry("key2", "some value 2"));
        }

        @Test
        void shouldRemoveTempFieldsBasedOfGivenClass() {
            removeTemporaryFields(caseDetails, TestClass.class);
            assertThat(caseDetails.getData()).containsOnly(Map.entry("key2", "some value 2"), Map.entry("key4", "4"));
        }

        @Test
        void shouldRemoveTempFieldsBasedOfGivenClass2() {

            final Map<String, Object> actualMap = nullifyTemporaryFields(data, TestClass.class);

            final Map<String, Object> expectedMap = new HashMap<>();
            expectedMap.put("key1", "some value 1");
            expectedMap.put("key2", "some value 2");
            expectedMap.put("key3", 3);
            expectedMap.put("key4", null);

            assertThat(actualMap).isEqualTo(expectedMap);
        }
    }

    @Nested
    class AddFields {

        private Map<String, Object> data = new HashMap<>();

        private CaseDetailsMap caseDetails;

        @BeforeEach
        void populateMap() {
            data.put("key1", "some value 1");
            data.put("key2", "some value 2");

            caseDetails = caseDetailsMap(CaseDetails.builder().data(data).build());
        }

        @Test
        void shouldNotUpdateAnyFieldsWhenFieldsGroupIsNotSpecified() {

            final TestClass target = new TestClass("one", "two", "three", "four");

            final CaseDetailsMap actualCaseDetails = addFields(caseDetails, target);

            assertThat(actualCaseDetails).containsExactly(
                entry("key1", "some value 1"),
                entry("key2", "some value 2"));
        }

        @Test
        void shouldNotUpdateAnyFieldsWhenFieldsGroupDoesNotExistsInTargetObject() {

            final TestClass target = new TestClass("one", "two", "three", "four");

            final CaseDetailsMap actualCaseDetails = addFields(caseDetails, target, "Non existing");

            assertThat(actualCaseDetails).containsExactly(
                entry("key1", "some value 1"),
                entry("key2", "some value 2"));
        }

        @Test
        void shouldAddFieldsFromSingleGroupInTargetObject() {

            final TestClass target = new TestClass("one", "two", "three", "four");

            final CaseDetailsMap actualCaseDetails = addFields(caseDetails, target, "Group1");

            assertThat(actualCaseDetails).containsExactly(
                entry("key1", "one"),
                entry("key2", "some value 2"),
                entry("key3", "three")
            );
        }

        @Test
        void shouldAddFieldsFromMultipleGroupsInTargetObject() {

            final TestClass target = new TestClass("one", "two", "three", "four");

            final CaseDetailsMap actualCaseDetails = addFields(caseDetails, target, "Group1", "Group2");

            assertThat(actualCaseDetails).containsExactly(
                entry("key1", "one"),
                entry("key2", "two"),
                entry("key3", "three"));
        }

        @Test
        void shouldRemoveFieldsFromSingleGroupWhenNullInTargetObjects() {

            final TestClass target = new TestClass(null, null, null, null);

            final CaseDetailsMap caseDetailsMap = addFields(caseDetails, target, "Group2");

            assertThat(caseDetailsMap).containsExactly(entry("key1", "some value 1"));
        }

        @Test
        void shouldRemoveFieldsFromMultipleGroupsWhenNullInTargetObjects() {

            final TestClass target = new TestClass(null, null, null, null);

            final CaseDetailsMap actualCaseDetails = addFields(caseDetails, target, "Group1", "Group2");

            assertThat(actualCaseDetails).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenTargetObjectIsNull() {

            assertThatThrownBy(() -> addFields(caseDetails, null, "Group1", "Group2"))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class CaseState {
        @ParameterizedTest
        @EnumSource(value = State.class, mode = EXCLUDE, names = {"OPEN"})
        void shouldReturnFalseForCasesNotInOpenState(State state) {
            CaseDetails caseDetails = CaseDetails.builder().state(state.getValue()).build();

            assertThat(isInOpenState(caseDetails)).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = State.class, mode = EXCLUDE, names = {"RETURNED"})
        void shouldReturnFalseForCasesNotInReturnedState(State state) {
            CaseDetails caseDetails = CaseDetails.builder().state(state.getValue()).build();

            assertThat(isInReturnedState(caseDetails)).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = State.class, mode = EXCLUDE, names = {"GATEKEEPING"})
        void shouldReturnFalseForCasesNotInGatekeepingState(State state) {
            CaseDetails caseDetails = CaseDetails.builder().state(state.getValue()).build();

            assertThat(isInGatekeepingState(caseDetails)).isFalse();
        }

        @Test
        void shouldReturnTrueForCasesInOpenState() {
            CaseDetails caseDetails = CaseDetails.builder().state(OPEN.getValue()).build();

            assertThat(isInOpenState(caseDetails)).isTrue();
        }

        @Test
        void shouldReturnTrueForCasesInReturnedState() {
            CaseDetails caseDetails = CaseDetails.builder().state(RETURNED.getValue()).build();

            assertThat(isInReturnedState(caseDetails)).isTrue();
        }

        @Test
        void shouldReturnTrueForCasesInGatekeepingState() {
            CaseDetails caseDetails = CaseDetails.builder().state(GATEKEEPING.getValue()).build();

            assertThat(isInGatekeepingState(caseDetails)).isTrue();
        }
    }

    private static class TestClass {

        TestClass(String key1, String key2, String key3, String key4) {
            this.key1 = key1;
            this.key2 = key2;
            this.key3 = key3;
            this.key4 = key4;
        }

        @Temp
        @FieldsGroup("Group1")
        private String key1;

        @FieldsGroup("Group2")
        private String key2;

        @Temp
        @FieldsGroup(value = {"Group1", "Group2"})
        private String key3;

        @TempNullify
        private String key4;
    }

}
