package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.CTSCTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {StandardDirectionOrderIssuedEmailContentProvider.class, LookupTestConfig.class,
    CaseDataExtractionService.class, HearingVenueLookUpService.class})
class StandardDirectionOrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private StandardDirectionOrderIssuedEmailContentProvider standardDirectionOrderIssuedEmailContentProvider;

    @Test
    void shouldReturnNotifyDataForCTSCWithValidSDODetails() {
        CTSCTemplateForSDO expectedData = ctscSDOTemplateParameters();
        CTSCTemplateForSDO actualData = standardDirectionOrderIssuedEmailContentProvider
            .buildNotificationParametersForCTSC(populatedCaseData());

        assertThat(actualData).usingRecursiveComparison().isEqualTo(expectedData);
    }

    @Test
    void shouldReturnNotifyDataForCTSCWhenNoneSelectedInHearingNeeds() {
        CTSCTemplateForSDO expectedData = ctscSDOTemplateParametersWithNoneSelected();
        CTSCTemplateForSDO actualData = standardDirectionOrderIssuedEmailContentProvider
            .buildNotificationParametersForCTSC(caseData());

        assertThat(actualData).usingRecursiveComparison().isEqualTo(expectedData);
    }

    private CTSCTemplateForSDO ctscSDOTemplateParameters() {
        return CTSCTemplateForSDO.builder()
            .documentLink("http://fake-url/documents/be17a76e-38ed-4448-8b83-45de1aa93f55/binary")
            .hearingNeeds(List.of("Intermediary"))
            .hearingNeedsPresent("Yes")
            .courtName(COURT_NAME)
            .callout("Smith, 12345, hearing 1 Jan 2020")
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .build();
    }

    private CTSCTemplateForSDO ctscSDOTemplateParametersWithNoneSelected() {
        return CTSCTemplateForSDO.builder()
            .documentLink("http://fake-url/documents/be17a76e-38ed-4448-8b83-45de1aa93f55/binary")
            .hearingNeeds(List.of())
            .hearingNeedsPresent("No")
            .courtName(COURT_NAME)
            .callout("Smith, 12345L, hearing 1 Jan 2020")
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .build();
    }
}
