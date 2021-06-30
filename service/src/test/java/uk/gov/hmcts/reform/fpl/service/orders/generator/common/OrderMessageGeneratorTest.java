package uk.gov.hmcts.reform.fpl.service.orders.generator.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;

public class OrderMessageGeneratorTest {

    private final ManageOrderDocumentService manageOrderDocumentService = mock(ManageOrderDocumentService.class);
    private final OrderMessageGenerator underTest = new OrderMessageGenerator(manageOrderDocumentService);

    private static final CaseData CASE_DATA = CaseData.builder().build();
    private static final Map<String, String> CONTEXT_ELEMENTS = Map.of(
        "childOrChildren", "child",
        "childIsOrAre", "is",
        "localAuthorityName", LOCAL_AUTHORITY_1_NAME
    );

    @BeforeEach
    void setUp() {
        when(manageOrderDocumentService.commonContextElements(CASE_DATA)).thenReturn(CONTEXT_ELEMENTS);
    }

    @Test
    void shouldReturnByConsentMessage() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersIsByConsent("Yes")
            .build();

        assertThat(underTest.getOrderByConsentMessage(manageOrdersEventData)).isEqualTo("By consent");
    }

    @Test
    void shouldNotReturnByConsentMessage() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersIsByConsent("No")
            .build();

        assertThat(underTest.getOrderByConsentMessage(manageOrdersEventData)).isEqualTo(null);
    }

    @Test
    void shouldFormatMessage() {
        String message = "The ${childOrChildren} ${childIsOrAre} placed in care of ${localAuthorityName}";
        String formattedMessage = "The child is placed in care of Test 1 Local Authority";

        assertThat(underTest.formatOrderMessage(CASE_DATA, message)).isEqualTo(formattedMessage);
    }

    @Test
    void shouldGetFormattedCareOrderRestrictions() {
        assertThat(underTest.getCareOrderRestrictions(CASE_DATA)).isEqualTo(
            "While a care order is in place, no one can change the child’s "
                + "surname or take the child out of the UK unless they "
                + "have written consent from all people with parental responsibility, or permission from the court.\n"
                + "\n"
                + "Taking the child from the UK without this consent or permission might be an offence under "
                + "the Child Abduction Act 1984.\n"
                + "\n"
                + "Test 1 Local Authority has been given parental responsibility under this care order and may take "
                + "the child out of the UK for up to 1 month without this consent or permission."
        );
    }
}
