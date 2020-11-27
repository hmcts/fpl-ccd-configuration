package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    ManageDocumentLAService.class,
})
public class ManageDocumentsLAServiceTest {

    @Autowired
    private ManageDocumentLAService manageDocumentLAService;

    @Test
    void shouldReturnNewElementWhenDocumentNotRelatedToHearingSelectedAndFurtherEvidenceDocumentsNotPresent() {
        CaseData caseData = CaseData.builder().manageDocumentLA(ManageDocumentLA.builder().build()).build();

        assertThat(unwrapElements(manageDocumentLAService.getFurtherEvidenceCollection(caseData)))
            .containsExactly(SupportingEvidenceBundle.builder().build());
    }

    @Test
    void shouldReturnExistingGeneralEvidenceDocumentsWhenRelatedToHearingNotSelectedAndDocumentsPresent() {
        SupportingEvidenceBundle furtherEvidence = SupportingEvidenceBundle.builder().name("general evidence").build();

        CaseData caseData = CaseData.builder()
            .manageDocumentLA(ManageDocumentLA.builder().relatedToHearing("No").build())
            .furtherEvidenceDocumentsLA(wrapElements(furtherEvidence))
            .build();

        assertThat(unwrapElements(manageDocumentLAService.getFurtherEvidenceCollection(caseData)))
            .containsExactly(furtherEvidence);
    }

    @Test
    void shouldReturnExistingHearingEvidenceDocumentsWhenRelatedToHearingSelectedAndDocumentsPresent() {
        UUID selectedHearingId = randomUUID();

        List<Element<SupportingEvidenceBundle>> hearingEvidence = List.of(element(selectedHearingId,
            SupportingEvidenceBundle.builder().name("hearing related evidence").build()));

        HearingFurtherEvidenceBundle hearingBundle = HearingFurtherEvidenceBundle.builder()
            .hearingName("Test Hearing - Case Management 1st Jan")
            .supportingEvidenceBundle(hearingEvidence)
            .build();

        CaseData caseData = CaseData.builder()
            .manageDocumentLA(ManageDocumentLA.builder().relatedToHearing("Yes").build())
            .manageDocumentsHearingList(selectedHearingId.toString())
            .hearingFurtherEvidenceDocuments(List.of(element(selectedHearingId, hearingBundle)))
            .build();

        assertThat(manageDocumentLAService.getFurtherEvidenceCollection(caseData)).isEqualTo(hearingEvidence);
    }

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
