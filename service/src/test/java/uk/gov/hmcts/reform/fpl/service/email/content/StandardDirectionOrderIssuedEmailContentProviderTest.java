package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.CTSCTemplateForSDO;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {StandardDirectionOrderIssuedEmailContentProvider.class, LookupTestConfig.class,
    CaseDataExtractionService.class, HearingVenueLookUpService.class, CaseConverter.class})
@TestPropertySource(properties = {"manage-case.ui.base.url=http://fake-url"})
class StandardDirectionOrderIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private StandardDirectionOrderIssuedEmailContentProvider standardDirectionOrderIssuedEmailContentProvider;

    private static final String ORDERS = "OrdersTab";

    @Test
    void shouldReturnExpectedMapForCTSCWithValidSDODetails() {
        CTSCTemplateForSDO expectedMap = ctscSDOTemplateParameters();

        assertThat(standardDirectionOrderIssuedEmailContentProvider
            .buildNotificationParametersForCTSC(populatedCaseData()))
            .isEqualToComparingFieldByField(expectedMap);
    }

    @Test
    void shouldReturnExpectedMapForCTSCWhenNoneSelectedInHearingNeeds() {
        CTSCTemplateForSDO expectedMap = ctscSDOTemplateParametersWithNoneSelected();

        assertThat(standardDirectionOrderIssuedEmailContentProvider
            .buildNotificationParametersForCTSC(caseData()))
            .isEqualToComparingFieldByField(expectedMap);
    }

    private CTSCTemplateForSDO ctscSDOTemplateParameters() {
        CTSCTemplateForSDO ctscTemplateForSDO = new CTSCTemplateForSDO();
        ctscTemplateForSDO.setDocumentLink("http://fake-url/documents/be17a76e-38ed-4448-8b83-45de1aa93f55/binary");
        ctscTemplateForSDO.setHearingNeeds(List.of("Intermediary"));
        ctscTemplateForSDO.setHearingNeedsPresent("Yes");
        ctscTemplateForSDO.setCourtName(COURT_NAME);
        ctscTemplateForSDO.setCallout("^Smith, 12345, hearing 1 Jan 2020");
        ctscTemplateForSDO.setRespondentLastName("Smith");
        ctscTemplateForSDO.setCaseUrl(caseUrl(CASE_REFERENCE, ORDERS));

        return ctscTemplateForSDO;
    }

    private CTSCTemplateForSDO ctscSDOTemplateParametersWithNoneSelected() {
        CTSCTemplateForSDO ctscTemplateForSDO = new CTSCTemplateForSDO();
        ctscTemplateForSDO.setDocumentLink("http://fake-url/documents/be17a76e-38ed-4448-8b83-45de1aa93f55/binary");
        ctscTemplateForSDO.setHearingNeedsPresent("No");
        ctscTemplateForSDO.setHearingNeeds(List.of());
        ctscTemplateForSDO.setCourtName(COURT_NAME);
        ctscTemplateForSDO.setCallout("^Smith, 12345L, hearing 1 Jan 2020");
        ctscTemplateForSDO.setRespondentLastName("Smith");
        ctscTemplateForSDO.setCaseUrl(caseUrl(CASE_REFERENCE, ORDERS));

        return ctscTemplateForSDO;
    }
}
