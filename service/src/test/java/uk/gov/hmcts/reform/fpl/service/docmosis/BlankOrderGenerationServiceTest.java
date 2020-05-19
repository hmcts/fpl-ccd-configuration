package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BlankOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class})
class BlankOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {

    @Autowired
    private BlankOrderGenerationService service;

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseData() {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = createPopulatedCaseData(BLANK_ORDER, null, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(BLANK_ORDER, null, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft() {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = createPopulatedCaseData(BLANK_ORDER, null, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(BLANK_ORDER, null, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }
}