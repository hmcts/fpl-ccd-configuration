package uk.gov.hmcts.reform.fpl.service.children;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class ChildrenDataFixerTest {

    private static final String SUBMITTED_STATE = "Submitted";
    private static final String OPEN_STATE = "Open";
    private static final String RETURNED_STATE = "RETURNED";
    private static final Child CHILD = mock(Child.class);
    private static final Object CHILDREN_DATA_OBJECT = List.of(new LinkedHashMap<>());
    private final ObjectMapper mapper = mock(ObjectMapper.class);

    private final ChildrenDataFixer underTest = new ChildrenDataFixer(mapper);
    private Map<String, Object> data;

    @BeforeEach
    void setUp() {
        data = new HashMap<>();
        data.put("children1", CHILDREN_DATA_OBJECT);
    }

    @DisplayName("Will add to data map when there is only 1 child and the case is in a post submitted state")
    @Test
    void fix() {
        List<Element<Child>> children = wrapElements(CHILD);

        when(mapper.convertValue(eq(CHILDREN_DATA_OBJECT), Mockito.<TypeReference<List<Element<Child>>>>any()))
            .thenReturn(children);

        CaseDetails caseDetails = CaseDetails.builder()
            .state(SUBMITTED_STATE)
            .data(data)
            .build();

        underTest.fix(caseDetails);

        assertThat(data).containsExactly(
            Map.entry("children1", CHILDREN_DATA_OBJECT),
            Map.entry("childrenHaveSameRepresentation", "Yes")
        );
    }

    @DisplayName("Will not add to data map when there is more than 1 child and the case is in a post submitted state")
    @Test
    void fixIgnoreDueToChildSize() {
        List<Element<Child>> children = wrapElements(CHILD, CHILD);

        when(mapper.convertValue(eq(CHILDREN_DATA_OBJECT), Mockito.<TypeReference<List<Element<Child>>>>any()))
            .thenReturn(children);

        CaseDetails caseDetails = CaseDetails.builder()
            .state(SUBMITTED_STATE)
            .data(data)
            .build();

        underTest.fix(caseDetails);

        assertThat(data).containsOnly(Map.entry("children1", CHILDREN_DATA_OBJECT));
    }

    @DisplayName("Will not add to data map when the case is in a pre submitted state")
    @ParameterizedTest(name = "when state = {0}")
    @ValueSource(strings = {OPEN_STATE, RETURNED_STATE})
    void fixIgnoreDueToCaseState(String state) {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(state)
            .data(data)
            .build();

        underTest.fix(caseDetails);

        assertThat(data).containsOnly(Map.entry("children1", CHILDREN_DATA_OBJECT));

        verifyNoInteractions(mapper);
    }
}
