package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SupervisionOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class})
class SupervisionOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {

    @Autowired
    private SupervisionOrderGenerationService service;

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldGetTemplateDataWhenGivenPopulatedCaseData(GeneratedOrderType type,
        GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = createPopulatedCaseData(type, subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(type, subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft(GeneratedOrderType type,
        GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = createPopulatedCaseData(type, subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(type, subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    private static Stream<Arguments> docmosisDataGenerationSource() {
        return Stream.of(
            Arguments.of(SUPERVISION_ORDER, INTERIM),
            Arguments.of(SUPERVISION_ORDER, FINAL)
        );
    }
}