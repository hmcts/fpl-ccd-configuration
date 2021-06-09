package uk.gov.hmcts.reform.fpl.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.docmosis.generator.DocmosisOrderCaseDataGenerator;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C21BlankOrderDocumentParameterGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C23EPOAdditionalDocumentsCollector;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C23EPODocumentParameterGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C32CareOrderDocumentParameterGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C33InterimCareOrderDocumentParameterGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C35aSupervisionOrderDocumentParameterGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C35bISODocumentParameterGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C47AAppointmentOfAChildrensGuardianParameterGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocmosisCommonElementDecorator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocumentMerger;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGeneratorHolder;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderDetailsWithEndTypeGenerator;
import uk.gov.hmcts.reform.fpl.utils.captor.ResultsCaptor;

import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper.extractPdfContent;
import static uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper.remove;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@SpringBootTest(classes = {
    ObjectMapper.class,
    OrderCreationService.class,
    OrderDocumentGenerator.class,
    ChildrenService.class,
    OrderDocumentGenerator.class,
    DocmosisDocumentGeneratorService.class,
    OrderDocumentGeneratorHolder.class,
    DocmosisCommonElementDecorator.class,
    CaseDataExtractionService.class,
    HearingVenueLookUpService.class,
    DocumentMerger.class,
    DocumentConversionService.class,
    C21BlankOrderDocumentParameterGenerator.class,
    C32CareOrderDocumentParameterGenerator.class,
    C23EPODocumentParameterGenerator.class,
    C35aSupervisionOrderDocumentParameterGenerator.class,
    C47AAppointmentOfAChildrensGuardianParameterGenerator.class,
    C33InterimCareOrderDocumentParameterGenerator.class,
    C23EPOAdditionalDocumentsCollector.class,
    C35bISODocumentParameterGenerator.class,
    OrderDetailsWithEndTypeGenerator.class,
    DocmosisDocumentGeneratorService.class,
    RestTemplate.class
})

public class AllOrdersDocmosisTest extends AbstractDocmosisTest {

    private static final String LA_CODE = "LA_CODE";
    private static final String LA_COURT = "La Court";

    // TO implement before merge
    private static final Set<Object> EXCLUDED_ORDERS = Set.of(
        Order.C32_CARE_ORDER,
        Order.C33_INTERIM_CARE_ORDER,
        Order.C35A_SUPERVISION_ORDER,
        Order.C35B_INTERIM_SUPERVISION_ORDER,
        Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN
    );

    @SpyBean
    private DocmosisDocumentGeneratorService generatorService;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @MockBean
    private DocumentDownloadService documentDownloadService;
    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    @MockBean
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    private ResultsCaptor<DocmosisDocument> resultsCaptor = new ResultsCaptor<>();

    @Autowired
    private OrderCreationService underTest;

    private final DocmosisOrderCaseDataGenerator dataGenerator = new DocmosisOrderCaseDataGenerator();

    @ParameterizedTest
    @MethodSource("allOrders")
    void testAllOrders(Order order) {

        String actualContent = getPdfContent(dataGenerator.generateForOrder(order));

        assertThat(actualContent)
            .isEqualToNormalizingWhitespace(readString(format("order-generation/%s.txt", order.name())));
    }

    private static Stream<Arguments> allOrders() {
        return Stream.of(Order.values())
            .filter(order -> !EXCLUDED_ORDERS.contains(order))
            .map(Arguments::of);
    }

    public String getPdfContent(CaseData caseData, String... ignores) {
        when(hmctsCourtLookupConfiguration.getCourt(LA_CODE)).thenReturn(
            new HmctsCourtLookupConfiguration.Court(LA_COURT, null, null));
        when(uploadDocumentService.uploadDocument(any(byte[].class),
            anyString(),
            anyString())).thenReturn(testDocument());
        doAnswer(resultsCaptor).when(generatorService).generateDocmosisDocument(anyMap(), any(), any());

        underTest.createOrderDocument(caseData,
            OrderStatus.PLAIN,
            RenderFormat.PDF);

        String text = extractPdfContent(resultsCaptor.getResult().getBytes());
        return remove(text, ignores);
    }

}
