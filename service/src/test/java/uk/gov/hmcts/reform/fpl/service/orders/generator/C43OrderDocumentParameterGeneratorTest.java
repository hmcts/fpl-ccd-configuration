package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class C43OrderDocumentParameterGeneratorTest {
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final String RECITALS_AND_PREAMBLES = "Recitals and Preambles";
    private static final String DIRECTIONS = "Directions";
    private static final String FURTHER_DIRECTIONS = "Further directions";
    private static final List<C43OrderType> C43OrderTypes = List.of(C43OrderType.CHILD_ARRANGEMENT_ORDER,
        C43OrderType.SPECIFIC_ISSUE_ORDER, C43OrderType.PROHIBITED_STEPS_ORDER);
    private static final CaseData CASE_DATA = CaseData.builder()
        .caseLocalAuthority(LA_CODE)
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersType(Order.C43_ORDER)
            .manageOrdersC43Orders(C43OrderTypes)
            .manageOrdersC43RecitalsAndPreambles(RECITALS_AND_PREAMBLES)
            .manageOrdersC43Directions(DIRECTIONS)
            .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
            .build())
        .build();

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C43OrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C43_ORDER);
    }

    @Test
    void generate() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);

        assertThat(generatedParameters).isEqualTo(expectedCommonParameters());
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER);
    }

    private C43DocmosisParameters expectedCommonParameters() {
        String orderTitle = "Child arrangements and Specific issue and Prohibited steps order";
        String orderDetails = "The Court orders" + "\n\n " + RECITALS_AND_PREAMBLES;
        String directions = DIRECTIONS + "\n\n " + FURTHER_DIRECTIONS;

        return C43DocmosisParameters.builder()
            .orderTitle(orderTitle)
            .orderDetails(orderDetails)
            .furtherDirections(directions)
            .localAuthorityName(LA_NAME)
            .build();
    }
}
