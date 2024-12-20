package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ConfidentialDetailsHelper.getConfidentialItemToAdd;
import static uk.gov.hmcts.reform.fpl.utils.ConfidentialDetailsHelper.getConfidentialOtherToAdd;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ConfidentialDetailsHelperTest {

    private static final String CONFIDENTIAL = "Yes";
    private static final UUID ID = randomUUID();
    private static final UUID SECOND_RANDOM_ID = randomUUID();

    @Test
    void shouldFindItemToAddWhenInConfidential() {
        List<Element<Other>> others = List.of(
            otherWithConfidentialFields(ID, CONFIDENTIAL),
            otherWithConfidentialFields(SECOND_RANDOM_ID, CONFIDENTIAL)
        );
        Element<Other> othersNotConfidential = otherWithRemovedConfidentialFields(ID);

        Other confidentialOthers = getConfidentialItemToAdd(others, othersNotConfidential);

        assertThat(confidentialOthers).isEqualToComparingFieldByField(others.get(0).getValue());
    }

    @Test
    void shouldReturnItemWhenNoMatchFoundInConfidential() {
        List<Element<Other>> others = List.of(otherWithConfidentialFields(ID, CONFIDENTIAL));
        Element<Other> othersNotConfidential = otherWithRemovedConfidentialFields(SECOND_RANDOM_ID);

        Other confidentialOthers = getConfidentialItemToAdd(others, othersNotConfidential);

        assertThat(confidentialOthers).isEqualToComparingFieldByField(othersNotConfidential.getValue());
    }

    @Test
    void shouldReturnOtherByNameIfNoUUIDMatch() {
        List<Element<Other>> others = List.of(element(UUID.randomUUID(),
            Other.builder().name("John Smith").addressKnowV2(IsAddressKnowType.LIVE_IN_REFUGE).build()));

        Other confidentialOther = getConfidentialOtherToAdd(others,
            element(UUID.randomUUID(), Other.builder().name("John Smith").build()));

        assertThat(confidentialOther).extracting("addressKnowV2")
            .isEqualTo(IsAddressKnowType.LIVE_IN_REFUGE);
    }

    @Test
    void shouldReturnOtherIfUUIDMatch() {
        UUID id = UUID.randomUUID();
        List<Element<Other>> others = List.of(element(id,
            Other.builder().name("John Smith").addressKnowV2(IsAddressKnowType.LIVE_IN_REFUGE).build()));

        Other confidentialOther = getConfidentialOtherToAdd(others,
            element(id, Other.builder().name("John Smith").build()));

        assertThat(confidentialOther).extracting("addressKnowV2")
            .isEqualTo(IsAddressKnowType.LIVE_IN_REFUGE);
    }


    private Other.OtherBuilder baseOtherBuilder(String detailsHidden) {
        return Other.builder()
            .name("John Smith")
            .detailsHidden(detailsHidden);
    }

    private Element<Other> otherWithRemovedConfidentialFields(UUID id) {
        return element(id, baseOtherBuilder(CONFIDENTIAL).build());
    }

    private Element<Other> otherWithConfidentialFields(UUID id, String detailsHidden) {
        return element(id, baseOtherBuilder(detailsHidden)
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build());
    }
}
