package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.utils.assertions.DynamicListAssert;

import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class LinkApplicationBlockPrePopulatorTest {

    private CaseData.CaseDataBuilder caseDataBuilder;

    LinkApplicationBlockPrePopulator classUnderTest = new LinkApplicationBlockPrePopulator();

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldReturnRightQuestionBlock() {
        AssertionsForClassTypes.assertThat(classUnderTest.accept()).isEqualTo(OrderQuestionBlock.LINK_APPLICATION);
    }

    @Test
    void shouldPrePopulateFields_WithApplicationsToLink() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .id(UUID.randomUUID())
            .build();
        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(UUID.randomUUID())
            .applicationType(OtherApplicationType.C1_PARENTAL_RESPONSIBILITY)
            .build();
        caseDataBuilder.additionalApplicationsBundle(singletonList(element(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2DocumentBundle)
                .otherApplicationsBundle(otherApplicationsBundle)
                .build()
        )));

        Map<String, Object> prePopulatedData = classUnderTest.prePopulate(caseDataBuilder.build());

        assertThat(prePopulatedData).hasSize(2)
            .containsEntry("manageOrdersShouldAllowLinkingApplication", "Yes");
        assertThat(prePopulatedData).extractingByKey("manageOrdersLinkedApplication")
            .asInstanceOf(DynamicListAssert.getInstanceOfAssertFactory())
            .hasSize(2)
            .hasElement(c2DocumentBundle.getId(), c2DocumentBundle.toLabel())
            .hasElement(otherApplicationsBundle.getId(), otherApplicationsBundle.toLabel());
    }

    @Test
    void shouldPrePopulateFields_WithNoApplicationsToLink() {
        Map<String, Object> prePopulatedData = classUnderTest.prePopulate(caseDataBuilder.build());

        assertThat(prePopulatedData).isEmpty();
    }

}
