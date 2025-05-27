package uk.gov.hmcts.reform.fpl.service.children;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class ChildrenEventDataFixerTest {

    private static final String SUBMITTED_STATE = "Submitted";
    private static final String OPEN_STATE = "Open";
    private static final String RETURNED_STATE = "RETURNED";
    private static final LocalDate NOW = LocalDate.now();
    private static final UUID CHILD_ID = UUID.randomUUID();

    private final CaseData caseData = mock(CaseData.class);
    private final ChildrenEventData eventData = mock(ChildrenEventData.class);
    private final Child child = mock(Child.class);

    private final Map<String, Object> data = new HashMap<>();

    private final CaseConverter converter = mock(CaseConverter.class);

    private final ChildrenEventDataFixer underTest = new ChildrenEventDataFixer(converter);

    @BeforeEach
    void setUp() {
        data.clear();
    }

    @DisplayName("Will add to data map when there is only 1 child, there is a main representative, and the case is in"
                 + " a post submitted state")
    @Test
    void fixRepresentationDetails() {
        List<Element<Child>> children = wrapElements(child);

        CaseDetails caseDetails = CaseDetails.builder()
            .state(SUBMITTED_STATE)
            .data(data)
            .build();

        when(converter.convert(caseDetails)).thenReturn(caseData);
        when(caseData.getAllChildren()).thenReturn(children);
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(eventData.getChildrenHaveRepresentation()).thenReturn("Yes");

        underTest.fixRepresentationDetails(caseDetails);

        assertThat(data).containsExactly(Map.entry("childrenHaveSameRepresentation", "Yes"));
    }

    @DisplayName("Will not add to data map when there is only 1 child, there is no main representative, and the case "
                 + "is in a post submitted state")
    @Test
    void fixRepresentationDetailsIgnoreDueToNoMainRepresentative() {
        List<Element<Child>> children = wrapElements(child);

        CaseDetails caseDetails = CaseDetails.builder()
            .state(SUBMITTED_STATE)
            .data(data)
            .build();

        when(converter.convert(caseDetails)).thenReturn(caseData);
        when(caseData.getAllChildren()).thenReturn(children);
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(eventData.getChildrenHaveRepresentation()).thenReturn("No");

        underTest.fixRepresentationDetails(caseDetails);

        assertThat(data).isEmpty();
    }

    @DisplayName("Will not add to data map when there is more than 1 child, there is a main representative, and the "
                 + "case is in a post submitted state")
    @Test
    void fixRepresentationDetailsIgnoreDueToChildSize() {
        List<Element<Child>> children = wrapElements(child, child);

        CaseDetails caseDetails = CaseDetails.builder()
            .state(SUBMITTED_STATE)
            .data(data)
            .build();

        when(converter.convert(caseDetails)).thenReturn(caseData);
        when(caseData.getAllChildren()).thenReturn(children);
        when(caseData.getChildrenEventData()).thenReturn(eventData);
        when(eventData.getChildrenHaveRepresentation()).thenReturn("Yes");

        underTest.fixRepresentationDetails(caseDetails);

        assertThat(data).isEmpty();
    }

    @DisplayName("Will not add to data map when the case is in a pre submitted state")
    @ParameterizedTest(name = "when state = {0}")
    @ValueSource(strings = {OPEN_STATE, RETURNED_STATE})
    void fixRepresentationDetailsIgnoreDueToCaseState(String state) {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(state)
            .data(data)
            .build();

        underTest.fixRepresentationDetails(caseDetails);

        assertThat(data).isEmpty();

        verifyNoInteractions(converter);
    }

    @Test
    void fixPersistentChildDetailsTest() {
        Element<Child> child1 = element(CHILD_ID, Child.builder()
            .party(ChildParty.builder()
                .address(Address.builder()
                    .addressLine1("11 Test")
                    .addressLine2("Test Rd")
                    .country("United Kingdom")
                    .county("Testershire")
                    .postcode("TE1 2ES")
                    .build())
                .dischargeDate(NOW)
                .datePowersEnd(NOW)
                .careStartDate(NOW)
                .livingSituationDetails("Details for Other")
                .addressChangeDate(NOW)
                .livingWithDetails("Uncle Test")
                .livingSituation("Living with other family or friends")
                .build())
            .build());

        Element<Child> expectedChild = element(CHILD_ID, Child.builder()
            .party(ChildParty.builder()
                .address(Address.builder()
                    .addressLine1("11 Test")
                    .addressLine2("Test Rd")
                    .country("United Kingdom")
                    .county("Testershire")
                    .postcode("TE1 2ES")
                    .build())
                .dischargeDate(null)
                .datePowersEnd(null)
                .careStartDate(null)
                .livingSituationDetails(null)
                .addressChangeDate(NOW)
                .livingWithDetails("Uncle Test")
                .livingSituation("Living with other family or friends")
                .build())
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .state(SUBMITTED_STATE)
            .data(data)
            .build();

        final CaseData caseDataChild = CaseData.builder()
                .children1(List.of(child1))
                    .build();

        when(converter.convert(caseDetails)).thenReturn(caseDataChild);

        underTest.fixPersistentChildDetails(caseDetails);

        assertThat(caseDetails.getData()).contains(Map.entry("children1", List.of(expectedChild)));
    }
}
