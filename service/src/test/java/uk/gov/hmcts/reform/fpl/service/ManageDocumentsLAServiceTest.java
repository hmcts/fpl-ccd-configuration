package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
class ManageDocumentsLAServiceTest {

    private final ManageDocumentLAService manageDocumentLAService = new ManageDocumentLAService(new ObjectMapper());

    @Test
    void shouldReturnNewCourtBundleListWithCourtBundleWhenNoExistingCourtBundlesPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(CourtBundle.builder().hearing("Test hearing").build())
            .courtBundleHearingList(selectedHearingId.toString())
            .build();

        assertThat(manageDocumentLAService.buildCourtBundleList(caseData))
            .isEqualTo(List.of(element(selectedHearingId, caseData.getManageDocumentsCourtBundle())));
    }

    @Test
    void shouldReturnEditedCourtBundleListWithCourtBundleWhenExistingCourtBundlePresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        List<Element<CourtBundle>> courtBundleList = new ArrayList<>();
        courtBundleList.add(element(selectedHearingId, CourtBundle.builder().hearing("Test hearing").build()));

        CourtBundle editedBundle = CourtBundle.builder().hearing("Edited hearing").build();
        CaseData caseData = CaseData.builder()
            .courtBundleList(courtBundleList)
            .manageDocumentsCourtBundle(editedBundle)
            .courtBundleHearingList(selectedHearingId.toString())
            .build();

        assertThat(unwrapElements(manageDocumentLAService.buildCourtBundleList(caseData)))
            .containsExactly(editedBundle);
    }
}
