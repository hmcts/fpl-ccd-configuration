package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(RaiseQueryController.class)
@OverrideAutoConfiguration(enabled = true)
public class RaiseQueryControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private UserService userService;

    RaiseQueryControllerAboutToStartTest() {
        super("raise-query");
    }

    @Test
    void shouldPrePopulateCorrectCollectionForChildSolAOnly() {
        when(userService.getCaseRoles(any())).thenReturn(Set.of(CaseRole.CHILDSOLICITORA));
        Long caseId = 1L;

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).containsKey(
            "qmCaseQueriesCollectionChildSolOne"
        );
        assertThat(response.getData()).doesNotContainKey(
            "qmCaseQueriesCollectionChildSolTwo"
        );
    }

    @Test
    void shouldPrePopulateCorrectCollectionForChildSolBOnly() {
        when(userService.getCaseRoles(any())).thenReturn(Set.of(CaseRole.CHILDSOLICITORB));
        Long caseId = 1L;

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).containsKey(
            "qmCaseQueriesCollectionChildSolTwo"
        );
        assertThat(response.getData()).doesNotContainKey(
            "qmCaseQueriesCollectionChildSolOne"
        );
    }

    @Test
    void shouldNotInitialiseFieldIfAlreadyPopulated() {
        when(userService.getCaseRoles(any())).thenReturn(Set.of(CaseRole.CHILDSOLICITORA));
        Long caseId = 1L;

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.ofEntries(
                Map.entry("Id", caseId),
                Map.entry("qmCaseQueriesCollectionChildSolOne", "some data"),
                Map.entry("qmCaseQueriesCollectionChildSolTwo", "some more data")
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData()).containsKey(
            "qmCaseQueriesCollectionChildSolOne"
        );
        assertThat(response.getData()).containsKey(
            "qmCaseQueriesCollectionChildSolTwo"
        );
        assertThat(response.getData().get("qmCaseQueriesCollectionChildSolOne")).isEqualTo("some data");
        assertThat(response.getData().get("qmCaseQueriesCollectionChildSolTwo")).isEqualTo("some more data";
    }
}
