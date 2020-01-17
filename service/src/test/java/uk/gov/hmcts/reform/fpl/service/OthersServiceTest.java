package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

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
        Others others = Others.builder()
            .firstOther(Other.builder()
                .birthPlace("birth place")
                .build())
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - BLANK - Please complete\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInList() {
        Others others = Others.builder()
            .firstOther(Other.builder()
                .name("James Daniels")
                .build())
            .additionalOthers(ImmutableList.of(Element.<Other>builder()
                .value(Other.builder()
                    .name("Bob Martyn")
                    .build())
                .build()))
            .build();

        String result = service.buildOthersLabel(others);

        assertThat(result).isEqualTo("Person 1 - James Daniels\nOther person 1 - Bob Martyn\n");
    }

    @Test
    void shouldBuildExpectedLabelWhenManyElementsInListWithEmptyName() {
        Others others = Others.builder()
            .firstOther(Other.builder()
                .birthPlace("birth place")
                .build())
            .additionalOthers(ImmutableList.of(Element.<Other>builder()
                .value(Other.builder()
                    .birthPlace("birth place")
                    .build())
                .build()))
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
    void shouldReturnOthersIfOthersIsPrePopulated() {
        List<Element<Other>> additionalOthers = new ArrayList<>();

        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(buildFirstOther()).additionalOthers(additionalOthers).build())
            .build();

        Others others = service.prepareOthers(caseData);

        assertThat(caseData.getOthers()).isEqualTo(others);
    }

    @Test
    void shouldPrepareOthersWithConfidentialValuesWhenConfidentialOthersIsNotEmpty() {
        List<Element<Other>> additionalOthersList = new ArrayList<>();
        Element<Other> additionalOther = Element.<Other>builder().id(ID).value(Other.builder().build()).build();
        additionalOthersList.add(additionalOther);

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(othersWithRemovedConfidentialFields(ID).get(0).getValue())
                .additionalOthers(additionalOthersList).build())
            .confidentialOthers(othersWithConfidentialFields(ID))
            .build();

        Others others = service.prepareOthers(caseData);

        assertThat(others.getFirstOther()).isEqualTo(othersWithConfidentialFields(ID).get(0).getValue());
    }

    @Test
    void shouldReturnOtherWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialOther() {
        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(othersWithRemovedConfidentialFields(ID).get(0).getValue())
                .additionalOthers(othersWithRemovedConfidentialFields(ID)).build())
            .confidentialOthers(othersWithConfidentialFields(randomUUID()))
            .build();

        Others others = service.prepareOthers(caseData);

        assertThat(others.getAdditionalOthers()).containsOnly(othersWithRemovedConfidentialFields(ID).get(0));
    }

    @Test
    void shouldAddExpectedRespondentWhenHiddenDetailsMarkedAsNo() {
        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(
                otherWithDetailsHiddenNo(ID)).build())
            .confidentialOthers(othersWithConfidentialFields(ID)).build();

        Others others = service.prepareOthers(caseData);

        assertThat(others.getFirstOther()).isEqualTo(otherWithDetailsHiddenNo(ID));
        assertThat(others.getAdditionalOthers()).isEqualTo(new ArrayList<>());
    }

    @Test
    void shouldHideOtherContactDetailsWhenConfidentialityFlagSet() {

        List<Element<Other>> additionalOthers = new ArrayList<>();
        additionalOthers.add(Element.<Other>builder()
            .id(ID)
            .value(Other.builder()
                .build())
            .build());

        List<Element<Other>> others = otherElementWithDetailsHiddenValue("Yes");

        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(others.get(0).getValue()).additionalOthers(additionalOthers)
            .build()).build();

        Others updatedOthers = service.modifyHiddenValues(caseData.getOthers());

        assertThat(updatedOthers.getFirstOther().getTelephone()).isNull();
        assertThat(updatedOthers.getFirstOther().getAddress()).isNull();

    }

    @Test
    void shouldNotHideOtherContactDetailsWhenConfidentialityFlagSet() {
        List<Element<Other>> additionalOthers = new ArrayList<>();
        additionalOthers.add(Element.<Other>builder()
            .id(ID)
            .value(Other.builder()
                .build())
            .build());

        List<Element<Other>> others = otherElementWithDetailsHiddenValue("No");

        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(others.get(0).getValue()).additionalOthers(additionalOthers)
                .build()).build();

        Others updatedOthers = service.modifyHiddenValues(caseData.getOthers());

        assertThat(updatedOthers.getFirstOther().getTelephone()).isNotNull();
        assertThat(updatedOthers.getFirstOther().getAddress()).isNotNull();
    }

    private List<Element<Other>> otherElementWithDetailsHiddenValue(String hidden) {
        return ImmutableList.of(Element.<Other>builder()
            .id(randomUUID())
            .value(Other.builder()
                    .name("James")
                    .detailsHidden(hidden)
                    .address(Address.builder()
                        .addressLine1("Address Line 1").build())
                    .telephone("01227 831393")
                        .build())
                    .build());
    }

    private List<Element<Other>> othersWithConfidentialFields(UUID id) {
        List<Element<Other>> confidentialOthers = new ArrayList<>();
        confidentialOthers.add(Element.<Other>builder()
            .id(id)
            .value(Other.builder()
                .name("Sarah Moley")
                .gender("Female")
                .detailsHidden("Yes")
                .address(Address.builder()
                    .addressLine1("Address Line 1")
                    .build())
                .telephone("01227 831393")
                .build())
            .build());

        return confidentialOthers;
    }

    private List<Element<Other>> othersWithRemovedConfidentialFields(UUID id) {
        List<Element<Other>> confidentialOthers = new ArrayList<>();
        confidentialOthers.add(Element.<Other>builder()
            .id(id)
            .value(Other.builder()
                .name("Sarah Moley")
                .gender("Female")
                .detailsHidden("Yes")
                .build())
            .build());

        return confidentialOthers;
    }

    private Other buildFirstOther() {
        return Other.builder()
            .name("Sarah Moley")
            .gender("Female")
            .build();
    }

    private Other otherWithDetailsHiddenNo(UUID id) {
        return Other.builder()
                    .name("Sarah Moley")
                    .gender("Female")
                    .detailsHidden("No")
                    .address(Address.builder()
                        .addressLine1("Address Line 1")
                        .build())
                    .telephone("01227 831393")
                    .build();
    }
}
