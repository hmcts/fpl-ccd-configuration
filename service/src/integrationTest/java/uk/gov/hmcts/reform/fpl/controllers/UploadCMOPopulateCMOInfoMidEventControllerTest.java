package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.controllers.cmo.UploadCMOController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadCMOPopulateCMOInfoMidEventControllerTest extends AbstractUploadCMOControllerTest {

    private static final DocumentReference DOCUMENT = DocumentReference.builder().build();

    protected UploadCMOPopulateCMOInfoMidEventControllerTest() {
        super("upload-cmo");
    }

    @Test
    void shouldExtractHearingInfo() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList pastHearingList = dynamicListWithFirstSelected(
            Pair.of("Case management hearing, 15 March 2020", hearings.get(0).getId()),
            Pair.of("Case management hearing, 16 March 2020", hearings.get(1).getId())
        );

        Map<String, Object> pastHearingListAsMap = mapper.convertValue(pastHearingList, new TypeReference<>() {});


        CaseData caseData = CaseData.builder()
            .uploadCMOEventData(UploadCMOEventData.builder()
                .pastHearingsForCMO(pastHearingList)
                .build())
            .hearingDetails(hearings)
            .build();

        CaseData responseData = extractCaseData(postMidEvent(caseData, "populate-cmo-info"));

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .cmoHearingInfo("Case management hearing, 15 March 2020")
            .showReplacementCMO(YesNo.NO)
            // Whilst not set in the controller, this is present due to having been populated at the start of the test
            .pastHearingsForCMO(pastHearingListAsMap)
            .build();

        assertThat(responseData.getUploadCMOEventData()).isEqualTo(expectedEventData);
    }

    @Test
    void shouldExtractPreviousCMODataWhenPresent() {
        List<Element<SupportingEvidenceBundle>> supportingDocs = List.of(element(SupportingEvidenceBundle.builder()
            .name("some doc")
            .build()));

        List<Element<CaseManagementOrder>> unsealedCMOs = List.of(
            element(CaseManagementOrder.builder()
                .order(DOCUMENT)
                .status(CMOStatus.DRAFT)
                .supportingDocs(supportingDocs)
                .build())
        );

        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());
        hearings.add(hearingWithCMOId(LocalDateTime.of(2020, 3, 17, 11, 11), unsealedCMOs.get(0).getId()));

        DynamicList pastHearingList = dynamicListWithSelected(
            2,
            Pair.of("Case management hearing, 15 March 2020", hearings.get(0).getId()),
            Pair.of("Case management hearing, 16 March 2020", hearings.get(1).getId()),
            Pair.of("Case management hearing, 17 March 2020", hearings.get(2).getId())
        );

        Map<String, Object> pastHearingListAsMap = mapper.convertValue(pastHearingList, new TypeReference<>() {});

        UploadCMOEventData eventData = UploadCMOEventData.builder()
            .pastHearingsForCMO(pastHearingList)
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .draftUploadedCMOs(unsealedCMOs)
            .uploadCMOEventData(eventData)
            .build();

        CaseData responseData = extractCaseData(postMidEvent(caseData, "populate-cmo-info"));

        UploadCMOEventData expectedEventData = UploadCMOEventData.builder()
            .previousCMO(DOCUMENT)
            .showReplacementCMO(YesNo.YES)
            .cmoSupportingDocs(supportingDocs)
            .cmoHearingInfo("Case management hearing, 17 March 2020")
            .pastHearingsForCMO(pastHearingListAsMap)
            .build();

        assertThat(responseData.getUploadCMOEventData()).isEqualTo(expectedEventData);
    }
}
