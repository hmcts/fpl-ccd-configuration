package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.service.others.OthersListGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class AllowedContactPrePopulatorTest {

    private final DynamicListService dynamicListService = mock(DynamicListService.class);
    private final OthersListGenerator othersListGenerator = mock(OthersListGenerator.class);

    private final AllowedContactPrePopulator underTest = new AllowedContactPrePopulator(
        othersListGenerator, dynamicListService
    );

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.PARTY_ALLOWED_CONTACTS_AND_CONDITIONS);
    }

    @Test
    @SuppressWarnings("unchecked")
    void prePopulate() {
        List<Element<Respondent>> respondents = List.of(
            element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                Respondent.builder().party(RespondentParty.builder().firstName("Peter").lastName("Smith").build()
                ).build()));
        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .othersV2(wrapElements(mock(Other.class)))
            .build();

        when(othersListGenerator.buildOthersList(any())).thenReturn(DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code("22222222-2222-2222-2222-222222222222")
                .label("Baker Smith").build()))
            .build());
        when(dynamicListService.asDynamicList(isA(Map.class))).thenReturn(DynamicList.builder()
            .listItems(new ArrayList<DynamicListElement>())
            .build());

        DynamicList allowedContacts = DynamicList.builder().listItems(List.of(
            DynamicListElement.builder().code("").label("-- Respondent --").build(),
            DynamicListElement.builder().code("11111111-1111-1111-1111-111111111111").label("Peter Smith").build(),
            DynamicListElement.builder().code("").label("-- Others to be given notice --").build(),
            DynamicListElement.builder().code("22222222-2222-2222-2222-222222222222").label("Baker Smith").build()
        )).build();

        Map<String, Object> actual = underTest.prePopulate(caseData);
        assertThat(actual).isEqualTo(
            Map.of(
                "manageOrdersAllowedContact1", allowedContacts,
                "manageOrdersAllowedContact2", allowedContacts,
                "manageOrdersAllowedContact3", allowedContacts
            )
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void prePopulateWithoutOthersToBeGivenNotice() {
        List<Element<Respondent>> respondents = List.of(
            element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                Respondent.builder().party(RespondentParty.builder().firstName("Peter").lastName("Smith").build()
                ).build()));
        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        when(dynamicListService.asDynamicList(isA(Map.class))).thenReturn(DynamicList.builder()
            .listItems(new ArrayList<DynamicListElement>())
            .build());

        DynamicList allowedContacts = DynamicList.builder().listItems(List.of(
            DynamicListElement.builder().code("").label("-- Respondent --").build(),
            DynamicListElement.builder().code("11111111-1111-1111-1111-111111111111").label("Peter Smith").build()
        )).build();

        Map<String, Object> actual = underTest.prePopulate(caseData);
        assertThat(actual).isEqualTo(
            Map.of(
                "manageOrdersAllowedContact1", allowedContacts,
                "manageOrdersAllowedContact2", allowedContacts,
                "manageOrdersAllowedContact3", allowedContacts
            )
        );
    }
}
