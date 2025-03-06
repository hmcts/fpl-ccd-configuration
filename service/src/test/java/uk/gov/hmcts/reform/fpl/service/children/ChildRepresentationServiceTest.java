package uk.gov.hmcts.reform.fpl.service.children;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;


class ChildRepresentationServiceTest {

    private static final String CODED_OPTION_COUNT = "0";
    private static final Map<String, Object> SERIALISED_REP_CHILDREN = Map.of(
        "someKey1", "someValue1",
        "someKey2", "someValue2"
    );
    private static final RespondentSolicitor CHILD_REPRESENTATIVE = mock(RespondentSolicitor.class);
    private static final RespondentSolicitor MAIN_CHILD_REPRESENTATIVE = mock(RespondentSolicitor.class);

    private static final UUID CHILD_UUID_1 = UUID.randomUUID();
    private static final UUID CHILD_UUID_2 = UUID.randomUUID();

    private final RespondentSolicitor mainRepresentative = mock(RespondentSolicitor.class);

    private final OptionCountBuilder optionCountBuilder = mock(OptionCountBuilder.class);
    private final ChildRepresentationDetailsFlattener flattener = mock(ChildRepresentationDetailsFlattener.class);

    private final ChildRepresentationService underTest = new ChildRepresentationService(optionCountBuilder, flattener);

    @Nested
    class PopulateRepresentationDetails {

        @Test
        void testWhenChildrenIfHaveRepresentation() {

            List<Element<Child>> children = wrapElements(Child.builder().build());
            when(optionCountBuilder.generateCode(children)).thenReturn(CODED_OPTION_COUNT);
            when(flattener.serialise(children, mainRepresentative)).thenReturn(SERIALISED_REP_CHILDREN);

            Map<String, Object> actual = underTest.populateRepresentationDetails(CaseData.builder()
                .childrenEventData(ChildrenEventData.builder()
                    .childrenMainRepresentative(mainRepresentative)
                    .childrenHaveRepresentation(YES.getValue())
                    .build())
                .children1(children)
                .build());

            Map<String, Object> expected = new HashMap<>();
            expected.put("optionCount", CODED_OPTION_COUNT);
            expected.putAll(SERIALISED_REP_CHILDREN);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void testWhenChildrenIfDoesNotHaveRepresentation() {

            when(optionCountBuilder.generateCode(null)).thenReturn(CODED_OPTION_COUNT);
            when(flattener.serialise(null, null)).thenReturn(SERIALISED_REP_CHILDREN);

            Map<String, Object> actual = underTest.populateRepresentationDetails(CaseData.builder()
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(NO.getValue())
                    .build())
                .children1(wrapElements(Child.builder().build()))
                .build());

            Map<String, Object> expected = new HashMap<>();
            expected.put("optionCount", null);
            expected.putAll(SERIALISED_REP_CHILDREN);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class FinaliseRepresentationDetails {

        @Test
        void testIfMainSolicitorNotPresent() {
            Map<String, Object> actual = underTest.finaliseChildrenAndRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(CHILD_REPRESENTATIVE)
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(NO.getValue())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(null)
                    .build()))
            ));
        }

        @Test
        void testIfMainSolicitorIsPresentAndAllChildrenUseMainSolicitor() {
            Map<String, Object> actual = underTest.finaliseChildrenAndRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(CHILD_REPRESENTATIVE)
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(YES.getValue())
                    .childrenMainRepresentative(MAIN_CHILD_REPRESENTATIVE)
                    .childrenHaveSameRepresentation(YES.getValue())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(MAIN_CHILD_REPRESENTATIVE)
                    .build()))
            ));
        }

        @Test
        void testIfMainSolicitorIsPresentAndChildDoNotUseMainSolicitor() {
            Map<String, Object> actual = underTest.finaliseChildrenAndRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(YES.getValue())
                    .childrenMainRepresentative(MAIN_CHILD_REPRESENTATIVE)
                    .childrenHaveSameRepresentation(NO.getValue())
                    .childRepresentationDetails0(ChildRepresentationDetails.builder()
                        .useMainSolicitor(NO.getValue())
                        .solicitor(CHILD_REPRESENTATIVE)
                        .build())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(CHILD_REPRESENTATIVE)
                    .build()))
            ));
        }

        @Test
        void testIfMainSolicitorIsPresentAndChildUseMainSolicitor() {
            Map<String, Object> actual = underTest.finaliseChildrenAndRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(YES.getValue())
                    .childrenMainRepresentative(MAIN_CHILD_REPRESENTATIVE)
                    .childrenHaveSameRepresentation(NO.getValue())
                    .childRepresentationDetails0(ChildRepresentationDetails.builder()
                        .useMainSolicitor(YES.getValue())
                        .build())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .solicitor(MAIN_CHILD_REPRESENTATIVE)
                    .build()))
            ));
        }

        @Test
        void testIfMainSolicitorIsPresentAndMultipleChildrenUseMixedSolicitors() {
            Map<String, Object> actual = underTest.finaliseChildrenAndRepresentationDetails(CaseData.builder()
                .children1(List.of(
                    element(CHILD_UUID_1, Child.builder().build()),
                    element(CHILD_UUID_2, Child.builder().build())
                ))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(YES.getValue())
                    .childrenMainRepresentative(MAIN_CHILD_REPRESENTATIVE)
                    .childrenHaveSameRepresentation(NO.getValue())
                    .childRepresentationDetails0(ChildRepresentationDetails.builder()
                        .useMainSolicitor(YES.getValue())
                        .build())
                    .childRepresentationDetails1(ChildRepresentationDetails.builder()
                        .useMainSolicitor(NO.getValue())
                        .solicitor(CHILD_REPRESENTATIVE)
                        .build())

                    .build())

                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(
                    element(CHILD_UUID_1, Child.builder()
                        .solicitor(MAIN_CHILD_REPRESENTATIVE)
                        .build()),
                    element(CHILD_UUID_2, Child.builder()
                        .solicitor(CHILD_REPRESENTATIVE)
                        .build())
                )
            ));
        }

        @Test
        void shouldMarkAsConfidentialIfRefugeeAddressIsProvided() {
            Map<String, Object> actual = underTest.finaliseChildrenAndRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(ChildLivingSituation.LIVE_IN_REFUGE.getValue())
                        .address(Address.builder().addressLine1("Refugee address").build())
                        .build())
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(NO.getValue())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(ChildLivingSituation.LIVE_IN_REFUGE.getValue())
                        .address(Address.builder().addressLine1("Refugee address").build())
                        .isAddressConfidential(YES.getValue())
                        .build())
                    .build()))
            ));
        }

        @Test
        void shouldMarkAsConfidentialIfAddressIsMarkedAsConfidential() {
            Map<String, Object> actual = underTest.finaliseChildrenAndRepresentationDetails(CaseData.builder()
                .children1(List.of(element(CHILD_UUID_1, Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(ChildLivingSituation.NOT_SPECIFIED.getValue())
                        .address(Address.builder().addressLine1("N/A address").build())
                        .isAddressConfidential(YES.getValue())
                        .build())
                    .build())))
                .childrenEventData(ChildrenEventData.builder()
                    .childrenHaveRepresentation(NO.getValue())
                    .build())
                .build());

            assertThat(actual).isEqualTo(Map.of(
                "children1", List.of(element(CHILD_UUID_1, Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(ChildLivingSituation.NOT_SPECIFIED.getValue())
                        .address(Address.builder().addressLine1("N/A address").build())
                        .isAddressConfidential(YES.getValue())
                        .build())
                    .build()))
            ));
        }
    }

}
