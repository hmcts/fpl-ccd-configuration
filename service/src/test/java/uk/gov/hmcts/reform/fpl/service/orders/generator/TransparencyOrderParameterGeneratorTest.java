package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.TransparencyOrderExpirationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.TransparencyOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.TransparencyOrderExpirationType.DATE_TO_BE_CHOSEN;
import static uk.gov.hmcts.reform.fpl.enums.TransparencyOrderExpirationType.THE_18TH_BDAY_YOUNGEST_CHILD;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.order.Order.TRANSPARENCY_ORDER;

@ExtendWith({MockitoExtension.class})
public class TransparencyOrderParameterGeneratorTest {

    private static final LocalDate END_DATE = LocalDate.of(2022, 12, 15);
    private static final LocalDate PERMISSION_DATE = LocalDate.of(2022, 10, 15);

    private static final String PUBLISH_INFO_DETAILS = "Text 1";
    private static final String PUBLISH_ID_DETAILS = "Text 2";
    private static final String PUBLISH_DOC_DETAILS = "Text 3";

    private final LocalAuthorityNameLookupConfiguration laNameLookup =
        mock(LocalAuthorityNameLookupConfiguration.class);

    private final ManageOrderDocumentService manageOrderDocumentService = mock(ManageOrderDocumentService.class);
    private final OrderMessageGenerator orderMessageGenerator = new OrderMessageGenerator(manageOrderDocumentService);

    private TransparencyOrderParameterGenerator underTest =
        new TransparencyOrderParameterGenerator(orderMessageGenerator);

    @BeforeEach
    void setUp() {
        Map<String, String> context = new HashMap<>();
        when(manageOrderDocumentService.commonContextElements(any())).thenReturn(context);
        when(laNameLookup.getLocalAuthorityName(LOCAL_AUTHORITY_1_CODE)).thenReturn(LOCAL_AUTHORITY_1_NAME);
    }

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(underTest.accept()).isEqualTo(TRANSPARENCY_ORDER);
    }

    @Test
    void shouldReturnCorrectTemplate() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.TRANSPARENCY_ORDER);
    }

    @Test
    void generateOrderWith18thBirthdayExpiration() {
        CaseData caseData = buildCaseDataWithSpecifiedExpirationType(THE_18TH_BDAY_YOUNGEST_CHILD);
        DocmosisParameters docParam = underTest.generate(caseData);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        DocmosisParameters expectedParam = buildExpectedParameters(THE_18TH_BDAY_YOUNGEST_CHILD, eventData);

        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithChosenExpiration() {
        CaseData caseData = buildCaseDataWithSpecifiedExpirationType(DATE_TO_BE_CHOSEN);
        DocmosisParameters docParam = underTest.generate(caseData);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        DocmosisParameters expectedParam = buildExpectedParameters(DATE_TO_BE_CHOSEN, eventData);

        assertThat(docParam).isEqualTo(expectedParam);
    }

    private CaseData buildCaseDataWithSpecifiedExpirationType(TransparencyOrderExpirationType expirationType) {
        return CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(TRANSPARENCY_ORDER)
                .manageOrdersIsByConsent(YES.getValue())
                .manageOrdersTransparencyOrderExpiration(expirationType)
                .manageOrdersTransparencyOrderEndDate(
                    expirationType == DATE_TO_BE_CHOSEN ?  END_DATE : null)
                .manageOrdersTransparencyOrderPublishInformationDetails(PUBLISH_INFO_DETAILS)
                .manageOrdersTransparencyOrderPublishIdentityDetails(PUBLISH_ID_DETAILS)
                .manageOrdersTransparencyOrderPublishDocumentsDetails(PUBLISH_DOC_DETAILS)
                .manageOrdersTransparencyOrderPermissionToReportEffectiveDate(PERMISSION_DATE)
                .build())
            .build();
    }

    private DocmosisParameters buildExpectedParameters(
        TransparencyOrderExpirationType expirationType,
        ManageOrdersEventData eventData
    ) {

        return TransparencyOrderDocmosisParameters.builder()
            .orderTitle(TRANSPARENCY_ORDER.getTitle())
            .childrenAct(TRANSPARENCY_ORDER.getChildrenAct())
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderExpiration(expirationType == DATE_TO_BE_CHOSEN
                ? dateBuilder(END_DATE) + "." : "the 18th birthday of the youngest child.")
            .publishInformationDetails(PUBLISH_INFO_DETAILS)
            .publishIdentityDetails(PUBLISH_ID_DETAILS)
            .publishDocumentsDetails(PUBLISH_DOC_DETAILS)
            .permissionToReportEffectiveDate(dateBuilder(PERMISSION_DATE) + ".")
            .build();
    }

    private String dateBuilder(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
