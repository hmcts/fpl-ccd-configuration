package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith({MockitoExtension.class})
class QueryManagementServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private QueryManagementService underTest;

    @Test
    void shouldReturnCorrectCollectionForUserRole() {
        when(userService.getCaseRoles(any())).thenReturn(Set.of(CaseRole.LASOLICITOR));
        Long caseId = 1L;

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .build();

        String collection = underTest.getCurrentCollectionByLoggedInUserRole(caseData);

        assertThat(collection).isEqualTo("qmCaseQueriesCollectionLASol");
    }

    @Test
    void shouldReturnAllCaseMessagesCorrectlyFromMultipleCollections() {
        Long caseId = 1L;
        String laUserId = UUID.randomUUID().toString();
        String respSolUserId = UUID.randomUUID().toString();
        String queryId = UUID.randomUUID().toString();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query1",
                                "subject", "query1",
                                "name", "Local Authority",
                                "createdOn", "2025-06-02T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        )
                    )
                )),
                Map.entry("qmCaseQueriesCollectionSolicitorA", Map.of(
                    "partyName", "Respondent Solicitor",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query2",
                                "subject", "query2",
                                "name", "Respondent Solicitor",
                                "createdOn", "2025-06-03T12:00:00.000Z",
                                "createdBy", respSolUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query3",
                                "subject", "query3",
                                "name", "Respondent Solicitor",
                                "createdOn", "2025-06-04T12:00:00.000Z",
                                "createdBy", respSolUserId
                            )
                        )
                    )
                ))

            ))
            .build();

        List<Map<String, Object>> expectedCaseMessageList = List.of(
            Map.of(
                "id", queryId,
                "body", "query",
                "subject", "query",
                "name", "Local Authority",
                "createdOn", "2025-06-01T12:00:00.000Z",
                "createdBy", laUserId
            ),
            Map.of(
                "id", queryId,
                "body", "query1",
                "subject", "query1",
                "name", "Local Authority",
                "createdOn", "2025-06-02T12:00:00.000Z",
                "createdBy", laUserId
            ),
            Map.of(
                "id", queryId,
                "body", "query2",
                "subject", "query2",
                "name", "Respondent Solicitor",
                "createdOn", "2025-06-03T12:00:00.000Z",
                "createdBy", respSolUserId
            ),
            Map.of(
                "id", queryId,
                "body", "query3",
                "subject", "query3",
                "name", "Respondent Solicitor",
                "createdOn", "2025-06-04T12:00:00.000Z",
                "createdBy", respSolUserId
            )
        );

        List<Map<String, Object>> actualCaseMessageList = underTest.getAllCaseMessages(caseDetails);

        assertThat(actualCaseMessageList).isEqualTo(expectedCaseMessageList);
    }

    @Test
    void shouldReturnCorrectQueryMatchingQueryId() {
        Long caseId = 1L;
        String queryId = UUID.randomUUID().toString();
        String respSolUserId = UUID.randomUUID().toString();
        String laUserId = UUID.randomUUID().toString();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "query1",
                                "subject", "query1",
                                "name", "Local Authority",
                                "createdOn", "2025-06-02T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        )
                    )
                )),
                Map.entry("qmCaseQueriesCollectionSolicitorA", Map.of(
                    "partyName", "Respondent Solicitor",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "query2",
                                "subject", "query2",
                                "name", "Respondent Solicitor",
                                "createdOn", "2025-06-03T12:00:00.000Z",
                                "createdBy", respSolUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "query3",
                                "subject", "query3",
                                "name", "Respondent Solicitor",
                                "createdOn", "2025-06-04T12:00:00.000Z",
                                "createdBy", respSolUserId
                            )
                        )
                    )
                ))
            ))
            .build();

        Map<String, Object> expectedQuery = Map.of(
            "id", queryId,
            "body", "query",
            "subject", "query",
            "name", "Local Authority",
            "createdOn", "2025-06-01T12:00:00.000Z",
            "createdBy", laUserId
        );

        Map<String, Object> actualQuery = underTest.getQueryByQueryId(caseDetails, queryId);

        assertThat(actualQuery).isEqualTo(expectedQuery);
    }

    @Test
    void shouldThrowExceptionWhenNoQueriesMatchQueryId() {
        Long caseId = 1L;
        String queryId = UUID.randomUUID().toString();
        String respSolUserId = UUID.randomUUID().toString();
        String laUserId = UUID.randomUUID().toString();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "query1",
                                "subject", "query1",
                                "name", "Local Authority",
                                "createdOn", "2025-06-02T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        )
                    )
                )),
                Map.entry("qmCaseQueriesCollectionSolicitorA", Map.of(
                    "partyName", "Respondent Solicitor",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "query2",
                                "subject", "query2",
                                "name", "Respondent Solicitor",
                                "createdOn", "2025-06-03T12:00:00.000Z",
                                "createdBy", respSolUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "query3",
                                "subject", "query3",
                                "name", "Respondent Solicitor",
                                "createdOn", "2025-06-04T12:00:00.000Z",
                                "createdBy", respSolUserId
                            )
                        )
                    )
                ))
            ))
            .build();

        assertThrows(IllegalArgumentException.class, () ->
            underTest.getQueryByQueryId(caseDetails, queryId));
    }

    @Test
    void shouldReturnCorrectCaseMessageFromCaseDetailsWhenTryingToGetQueryResponse() {
        Long caseId = 1L;
        String laUserId = UUID.randomUUID().toString();
        String adminUserId = UUID.randomUUID().toString();
        String queryId = UUID.randomUUID().toString();
        String responseId = UUID.randomUUID().toString();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        )
                    )
                ))
            ))
            .build();

        CaseDetails caseDetailsAfter = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", responseId,
                                "body", "response",
                                "subject", "response",
                                "name", "CTSC Admin",
                                "createdOn", "2025-06-02T12:00:00.000Z",
                                "createdBy", adminUserId,
                                "parentId", queryId
                            )
                        )
                    )
                ))
            ))
            .build();

        Map<String, Object> expectedCaseMessage = Map.of(
            "id", responseId,
            "body", "response",
            "subject", "response",
            "name", "CTSC Admin",
            "createdOn", "2025-06-02T12:00:00.000Z",
            "createdBy", adminUserId,
            "parentId", queryId
        );

        Map<String, Object> actualCaseMessage =
            underTest.getQueryResponseFromCaseDetails(caseDetailsBefore, caseDetailsAfter);

        assertThat(actualCaseMessage).isEqualTo(expectedCaseMessage);
    }

    @Test
    void shouldReturnCorrectCaseMessageWhenFetchingParentQueryFromQueryResponse() {
        Long caseId = 1L;
        String laUserId = UUID.randomUUID().toString();
        String adminUserId = UUID.randomUUID().toString();
        String queryId = UUID.randomUUID().toString();
        String responseId = UUID.randomUUID().toString();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", responseId,
                                "body", "response",
                                "subject", "response",
                                "name", "CTSC Admin",
                                "createdOn", "2025-06-02T12:00:00.000Z",
                                "createdBy", adminUserId,
                                "parentId", queryId
                            )
                        )
                    )
                ))
            ))
            .build();

        Map<String, Object> queryResponse = Map.of(
            "id", responseId,
            "body", "response",
            "subject", "response",
            "name", "CTSC Admin",
            "createdOn", "2025-06-02T12:00:00.000Z",
            "createdBy", adminUserId,
            "parentId", queryId
        );

        Map<String, Object> expectedCaseMessage = Map.of(
            "id", queryId,
            "body", "query",
            "subject", "query",
            "name", "Local Authority",
            "createdOn", "2025-06-01T12:00:00.000Z",
            "createdBy", laUserId
        );

        Map<String, Object> actualCaseMessage = underTest.getParentQueryFromResponse(caseDetails, queryResponse);

        assertThat(actualCaseMessage).isEqualTo(expectedCaseMessage);
    }

    @Test
    void shouldReturnUserIdFromCaseMessage() {
        String userId = UUID.randomUUID().toString();

        Map<String, Object> caseMessage = Map.of(
            "id", UUID.randomUUID().toString(),
            "body", "query",
            "subject", "query",
            "name", "Local Authority",
            "createdOn", "2025-06-01T12:00:00.000Z",
            "createdBy", userId
        );

        String actualUserId = underTest.getUserIdFromQuery(caseMessage);

        assertThat(actualUserId).isEqualTo(userId);
    }

    @Test
    void shouldReturnCreatedOnDateFromCaseMessage() {
        String createdOnDate = "2025-06-01T12:00:00.000Z";

        Map<String, Object> caseMessage = Map.of(
            "id", UUID.randomUUID().toString(),
            "body", "query",
            "subject", "query",
            "name", "Local Authority",
            "createdOn", createdOnDate,
            "createdBy", UUID.randomUUID().toString()
        );

        String expectedQueryDate = "2025-06-01";

        String actualQueryDate = underTest.getQueryDateFromQuery(caseMessage);

        assertThat(actualQueryDate).isEqualTo(expectedQueryDate);
    }

    @Test
    void shouldReturnLatestCaseMessageInCollectionWhichIsNotAResponse() {
        Long caseId = 1L;
        String laUserId = UUID.randomUUID().toString();
        String respSolUserId = UUID.randomUUID().toString();
        String queryId = UUID.randomUUID().toString();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseId)
            .data(Map.ofEntries(
                Map.entry("id", caseId),
                Map.entry("qmCaseQueriesCollectionLASol", Map.of(
                    "partyName", "Local Authority",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query",
                                "subject", "query",
                                "name", "Local Authority",
                                "createdOn", "2025-06-01T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query1",
                                "subject", "query1",
                                "name", "Local Authority",
                                "createdOn", "2025-06-07T12:00:00.000Z",
                                "createdBy", laUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", UUID.randomUUID().toString(),
                                "body", "response",
                                "subject", "response",
                                "name", "CTSC Admin",
                                "createdOn", "2025-06-12T12:00:00.000Z",
                                "createdBy", UUID.randomUUID().toString(),
                                "parentId", queryId
                            )
                        )
                    )
                )),
                Map.entry("qmCaseQueriesCollectionSolicitorA", Map.of(
                    "partyName", "Respondent Solicitor",
                    "caseMessages", List.of(
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query2",
                                "subject", "query2",
                                "name", "Respondent Solicitor",
                                "createdOn", "2025-06-03T12:00:00.000Z",
                                "createdBy", respSolUserId
                            )
                        ),
                        element(
                            Map.of(
                                "id", queryId,
                                "body", "query3",
                                "subject", "query3",
                                "name", "Respondent Solicitor",
                                "createdOn", "2025-06-04T12:00:00.000Z",
                                "createdBy", respSolUserId
                            )
                        )
                    )
                ))
            ))
            .build();

        Map<String, Object> expectedCaseMessage = Map.of(
            "id", queryId,
            "body", "query1",
            "subject", "query1",
            "name", "Local Authority",
            "createdOn", "2025-06-07T12:00:00.000Z",
            "createdBy", laUserId
        );

        Map<String, Object> actualCaseMessage =
            underTest.getLatestQueryInCollection(caseDetails, "qmCaseQueriesCollectionLASol");

        assertThat(actualCaseMessage).isEqualTo(expectedCaseMessage);
    }
}
