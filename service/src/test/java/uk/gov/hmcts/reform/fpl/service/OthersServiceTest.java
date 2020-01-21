package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class OthersServiceTest {
    private static final UUID ID = randomUUID();

    private final OthersService service = new OthersService();

    @Test
    void shouldBuildExpectedLabelWhenSingleElementInList() {
        Others others = Others.builder()
            .firstOther(Other.builder()
                .name("James Daniels")
                .build())
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenSingleElementInListWithEmptyName() {
        Others others = Others.builder().firstOther(Other.builder().birthPlace("birth place").build()).build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - BLANK - Please complete\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInList() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("James Daniels").build())
            .additionalOthers(wrapElements((Other.builder().name("Bob Martyn").build())))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\nOther person 1 - Bob Martyn\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInListWithEmptyName() {
        Others others = Others.builder()
            .firstOther(Other.builder().birthPlace("birth place").build())
            .additionalOthers(wrapElements(Other.builder().birthPlace("birth place").build()))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - BLANK - Please complete\nOther person 1 - BLANK - Please complete\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenNull() {
        String result = service.buildOthersLabel(null);

        assertThat(result).isEqualTo("No others on the case");
    }

    @Test
    void shouldBuildExpectedLabelWhenEmptyOthers() {
        String result = service.buildOthersLabel(Others.builder().build());

        assertThat(result).isEqualTo("No others on the case");
    }

    @Test
    void shouldReturnOthersWhenOthersIsPrePopulated() {
        List<Element<Other>> additionalOthers = othersWithRemovedConfidentialFields();

        CaseData caseData = buildCaseDataWithOthers(otherWithDetailsHiddenValue("No"), additionalOthers, null);

        Others others = service.prepareOthers(caseData);

        assertThat(caseData.getOthers()).isEqualTo(others);
    }

    @Test
    void shouldPrepareOthersWithConfidentialValuesWhenConfidentialOthersIsNotEmpty() {
        List<Element<Other>> additionalOthersList = othersWithConfidentialFields(randomUUID());

        Other firstOther = othersWithRemovedConfidentialFields().get(0).getValue();
        List<Element<Other>> confidentialOthers = othersWithConfidentialFields(ID);

        CaseData caseData = buildCaseDataWithOthers(firstOther, additionalOthersList, confidentialOthers);

        Others others = service.prepareOthers(caseData);

        assertThat(others.getFirstOther()).isEqualTo(confidentialOthers.get(0).getValue());
    }

    @Test
    void shouldReturnOtherWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialOther() {
        Other firstOther = othersWithRemovedConfidentialFields().get(0).getValue();
        List<Element<Other>> additionalOther = othersWithRemovedConfidentialFields();
        List<Element<Other>> confidentialOther = othersWithConfidentialFields(randomUUID());

        CaseData caseData = buildCaseDataWithOthers(firstOther, additionalOther, confidentialOther);

        Others others = service.prepareOthers(caseData);

        assertThat(others.getAdditionalOthers()).containsOnly(additionalOther.get(0));
    }

    @Test
    void shouldAddExpectedRespondentWhenHiddenDetailsMarkedAsNo() {
        Other firstOther = otherWithDetailsHiddenValue("No");
        List<Element<Other>> confidentialOther = othersWithConfidentialFields(ID);

        CaseData caseData = buildCaseDataWithOthers(firstOther, null, confidentialOther);

        Others others = service.prepareOthers(caseData);

        assertThat(others.getFirstOther()).isEqualTo(otherWithDetailsHiddenValue("No"));
        assertThat(others.getAdditionalOthers()).isEmpty();
    }

    @Test
    void shouldHideOtherContactDetailsWhenConfidentialityFlagSet() {
        List<Element<Other>> additionalOthers = othersWithRemovedConfidentialFields();
        Other others = otherWithDetailsHiddenValue("Yes");

        CaseData caseData = buildCaseDataWithOthers(others, additionalOthers, null);

        Others updatedOthers = service.modifyHiddenValues(caseData.getAllOthers());

        assertThat(updatedOthers.getFirstOther().getTelephone()).isNull();
        assertThat(updatedOthers.getFirstOther().getAddress()).isNull();
    }

    @Test
    void shouldNotHideOtherContactDetailsWhenConfidentialityFlagSet() {
        List<Element<Other>> additionalOthers = othersWithRemovedConfidentialFields();

        Other others = otherWithDetailsHiddenValue("No");

        CaseData caseData = buildCaseDataWithOthers(others, additionalOthers, null);

        Others updatedOthers = service.modifyHiddenValues(caseData.getAllOthers());

        assertThat(updatedOthers.getFirstOther().getTelephone()).isNotNull();
        assertThat(updatedOthers.getFirstOther().getAddress()).isNotNull();
    }

    @Test
    void shouldRetainConfidentialDetailsWhenConfidentialOtherExists() {
        List<Element<Other>> confidentialOthers =
            service.retainConfidentialDetails(wrapElements(otherWithDetailsHiddenValue("Yes")));

        assertThat(unwrapElements(confidentialOthers)).containsOnly(confidentialOther());
    }

    @Test
    void shouldReturnEmptyListWhenNoConfidentialOthers() {
        List<Element<Other>> confidentialOthers = service.retainConfidentialDetails(emptyList());

        assertThat(confidentialOthers).isEmpty();
    }

    private CaseData buildCaseDataWithOthers(Other firstOther,
                                             List<Element<Other>> additionalOthers,
                                             List<Element<Other>> confidentialOthers) {
        return CaseData.builder()
            .others(Others.builder().firstOther(firstOther).additionalOthers(additionalOthers).build())
            .confidentialOthers(confidentialOthers)
            .build();
    }

    private Other otherWithDetailsHiddenValue(String hidden) {
        return Other.builder()
            .name("James")
            .gender("Female")
            .detailsHidden(hidden)
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build();
    }

    private Other confidentialOther() {
        return Other.builder()
            .name("James")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build();
    }

    private List<Element<Other>> othersWithConfidentialFields(UUID id) {
        return newArrayList(element(id, Other.builder()
            .name("James")
            .gender("Female")
            .detailsHidden("Yes")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build()));
    }

    private List<Element<Other>> othersWithRemovedConfidentialFields() {
        return newArrayList(element(ID, Other.builder()
            .name("James")
            .gender("Female")
            .detailsHidden("Yes")
            .build()));
    }
}
