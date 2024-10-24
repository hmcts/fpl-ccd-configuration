package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(RaiseQueryController.class)
@OverrideAutoConfiguration(enabled = true)
public class RaiseQueryControllerAboutToStartTest extends AbstractCallbackTest {

    RaiseQueryControllerAboutToStartTest() {
        super("raise-query");
    }

    @Test
    void shouldInitialiseAllQueryCollections() {
        Long caseId = 1L;

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).containsKeys(
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionLASol",
            "qmCaseQueriesCollectionEPSManaging",
            "qmCaseQueriesCollectionLAManaging",
            "qmCaseQueriesCollectionLABarrister",
            "qmCaseQueriesCollectionLAShared",
            "qmCaseQueriesCollectionBarrister",
            "qmCaseQueriesCollectionSolicitor",
            "qmCaseQueriesCollectionSolicitorA",
            "qmCaseQueriesCollectionSolicitorB",
            "qmCaseQueriesCollectionSolicitorC",
            "qmCaseQueriesCollectionSolicitorD",
            "qmCaseQueriesCollectionSolicitorE",
            "qmCaseQueriesCollectionSolicitorF",
            "qmCaseQueriesCollectionSolicitorG",
            "qmCaseQueriesCollectionSolicitorH",
            "qmCaseQueriesCollectionSolicitorI",
            "qmCaseQueriesCollectionSolicitorJ",
            "qmCaseQueriesCollectionChildSolA",
            "qmCaseQueriesCollectionChildSolB",
            "qmCaseQueriesCollectionChildSolC",
            "qmCaseQueriesCollectionChildSolD",
            "qmCaseQueriesCollectionChildSolE",
            "qmCaseQueriesCollectionChildSolF",
            "qmCaseQueriesCollectionChildSolG",
            "qmCaseQueriesCollectionChildSolH",
            "qmCaseQueriesCollectionChildSolI",
            "qmCaseQueriesCollectionChildSolJ",
            "qmCaseQueriesCollectionChildSolK",
            "qmCaseQueriesCollectionChildSolL",
            "qmCaseQueriesCollectionChildSolM",
            "qmCaseQueriesCollectionChildSolN",
            "qmCaseQueriesCollectionChildSolO",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcass",
            "qmCaseQueriesCollectionCafcassSol"
        );
    }

    @Test
    void shouldNotInitialiseFieldIfAlreadyPopulated() {
        Long caseId = 1L;

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.ofEntries(
                Map.entry("Id", caseId),
                Map.entry("qmCaseQueriesCollectionChildSolA", "some data"),
                Map.entry("qmCaseQueriesCollectionChildSolB", "some more data")
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getData()).containsKeys(
            "qmCaseQueriesCollectionChildSolA",
            "qmCaseQueriesCollectionChildSolB",
            "qmCaseQueriesCollectionChildSolC"
        );

        assertThat(response.getData().get("qmCaseQueriesCollectionChildSolA")).isEqualTo("some data");
        assertThat(response.getData().get("qmCaseQueriesCollectionChildSolB")).isEqualTo("some more data");
        assertThat(response.getData().get("qmCaseQueriesCollectionChildSolC")).isNull();
    }
}
