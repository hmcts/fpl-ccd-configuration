package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.FAMILY_ASSISTANCE_ORDER;

class FamilyAssistanceBefriendValidatorTest {

    private static final String ERROR_MESSAGE = "You cannot name the same party to be befriended more than once.";

    private static final DynamicList PARTY_TO_BE_BEFRIENDED_1 = DynamicList.builder()
        .value(DynamicListElement.builder().code(UUID.randomUUID().toString()).label("John Smith").build())
        .build();

    private static final DynamicList PARTY_TO_BE_BEFRIENDED_2 = DynamicList.builder()
        .value(DynamicListElement.builder().code(UUID.randomUUID().toString()).label("Jack Smith").build())
        .build();

    private static final DynamicList PARTY_TO_BE_BEFRIENDED_3 = DynamicList.builder()
        .value(DynamicListElement.builder().code(UUID.randomUUID().toString()).label("Jean Smith").build())
        .build();

    private final FamilyAssistanceBefriendValidator underTest = new FamilyAssistanceBefriendValidator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(FAMILY_ASSISTANCE_ORDER);
    }

    @Test
    void shouldAcceptOneUniqueChoices() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersPartyToBeBefriended1(PARTY_TO_BE_BEFRIENDED_1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptTwoUniqueChoices() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersPartyToBeBefriended1(PARTY_TO_BE_BEFRIENDED_1)
                .manageOrdersPartyToBeBefriended2(PARTY_TO_BE_BEFRIENDED_2)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldAcceptThreeUniqueChoices() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersPartyToBeBefriended1(PARTY_TO_BE_BEFRIENDED_1)
                .manageOrdersPartyToBeBefriended2(PARTY_TO_BE_BEFRIENDED_2)
                .manageOrdersPartyToBeBefriended3(PARTY_TO_BE_BEFRIENDED_3)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldFailAt2DuplicateChoices() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersPartyToBeBefriended1(PARTY_TO_BE_BEFRIENDED_1)
                .manageOrdersPartyToBeBefriended2(PARTY_TO_BE_BEFRIENDED_1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(ERROR_MESSAGE));
    }

    @Test
    void shouldFailAt2DuplicateChoices_2() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersPartyToBeBefriended1(PARTY_TO_BE_BEFRIENDED_1)
                .manageOrdersPartyToBeBefriended3(PARTY_TO_BE_BEFRIENDED_1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(ERROR_MESSAGE));
    }

    @Test
    void shouldFailAt3DuplicateChoices() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersPartyToBeBefriended1(PARTY_TO_BE_BEFRIENDED_1)
                .manageOrdersPartyToBeBefriended2(PARTY_TO_BE_BEFRIENDED_1)
                .manageOrdersPartyToBeBefriended3(PARTY_TO_BE_BEFRIENDED_1)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(ERROR_MESSAGE));
    }
}
