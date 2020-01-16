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
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class OthersServiceTest {
    private static final UUID ID = randomUUID();
    private static final UUID ID_2 = randomUUID();

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

    private Other buildFirstOther(){
        return Other.builder()
            .name("Sarah Moley")
            .gender("Female")
            .build();
    }

    @Test
    void shouldPrepareOthersWithConfidentialValuesWhenConfidentialOthersIsNotEmpty() {
        List<Element<Other>> additionalOthersList = new ArrayList<>();
        Element<Other> additionalOther = Element.<Other>builder().id(ID).value(Other.builder().build()).build();
        additionalOthersList.add(additionalOther);

        List<Element<Other>> confidentialOthers = new ArrayList<>();
        confidentialOthers.add(othersWithConfidentialFields(ID));

        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(othersWithRemovedConfidentialFields(ID).getValue()).additionalOthers(additionalOthersList).build())
            .confidentialOthers(confidentialOthers)
            .build();

        Others others = service.prepareOthers(caseData);

        assertThat(others.getFirstOther()).isEqualTo(othersWithConfidentialFields(ID).getValue());
    }

    private Element<Other> othersWithConfidentialFields(UUID id) {
        return Element.<Other>builder()
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
            .build();
    }

    private Element<Other> othersWithRemovedConfidentialFields(UUID id) {
        return Element.<Other>builder()
            .id(id)
            .value(Other.builder()
                .name("Sarah Moley")
                .gender("Female")
                .detailsHidden("Yes")
                .build())
            .build();
    }

    private Element<Other> othersWithConfidentialFields2(UUID id) {
        return Element.<Other>builder()
            .id(id)
            .value(Other.builder()
                .name("James")
                .detailsHidden("Yes")
                .build())
            .build();
    }
}
