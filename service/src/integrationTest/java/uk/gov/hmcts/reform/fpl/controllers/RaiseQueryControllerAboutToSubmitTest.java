package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(RaiseQueryController.class)
@OverrideAutoConfiguration(enabled = true)
public class RaiseQueryControllerAboutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private UserService userService;

    RaiseQueryControllerAboutToSubmitTest() {
        super("raise-query");
    }

    @Test
    void shouldPopulateLatestQueryIDCorrectly() {
        when(userService.getCaseRoles(any())).thenReturn(Set.of(CaseRole.LASOLICITOR));
        Long caseId = 1L;
        UUID caseMessageID = UUID.randomUUID();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.ofEntries(
                Map.entry("Id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", caseMessageID,
                                "body", "test",
                                "subject", "test",
                                "createdOn", "2025-04-08T12:00:00.000Z"
                            )
                        )
                    )
                ))
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsKey(
            "latestQueryID"
        );

        assertThat(response.getData().get("latestQueryID")).isEqualTo(caseMessageID.toString());
    }

    @Test
    void shouldPopulateLatestQueryIDCorrectlyWithMultipleQueries() {
        when(userService.getCaseRoles(any())).thenReturn(Set.of(CaseRole.LASOLICITOR));
        Long caseId = 1L;
        UUID caseMessageID = UUID.randomUUID();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.ofEntries(
                Map.entry("Id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", caseMessageID,
                                "body", "newquery",
                                "subject", "newquery",
                                "createdOn", "2025-04-08T12:00:00.000Z"
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID(),
                                "body", "oldquery",
                                "subject", "oldquery",
                                "createdOn", "2025-04-01T12:00:00.000Z"
                            )
                        )
                    )
                ))
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsKey(
            "latestQueryID"
        );

        assertThat(response.getData().get("latestQueryID")).isEqualTo(caseMessageID.toString());
    }

    @Test
    void shouldPopulateLatestQueryIDCorrectlyWithMultipleQueriesAndMultipleCollections() {
        when(userService.getCaseRoles(any())).thenReturn(Set.of(CaseRole.LASOLICITOR));
        Long caseId = 1L;
        UUID caseMessageID = UUID.randomUUID();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.ofEntries(
                Map.entry("Id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", caseMessageID,
                                "body", "newquery",
                                "subject", "newquery",
                                "createdOn", "2025-04-08T12:00:00.000Z"
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID(),
                                "body", "oldquery",
                                "subject", "oldquery",
                                "createdOn", "2025-04-01T12:00:00.000Z"
                            )
                        )
                    )
                )),
                Map.entry("qmCaseQueriesCollectionEPSManaging", Map.of(
                    "partyName", "EPS Managing",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", UUID.randomUUID(),
                                "body", "newquery",
                                "subject", "newquery",
                                "createdOn", "2025-04-08T12:00:00.000Z"
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID(),
                                "body", "oldquery",
                                "subject", "oldquery",
                                "createdOn", "2025-04-01T12:00:00.000Z"
                            )
                        )
                    )
                ))
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsKey(
            "latestQueryID"
        );

        assertThat(response.getData().get("latestQueryID")).isEqualTo(caseMessageID.toString());
    }
}
