package uk.gov.hmcts.reform.ccd.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import static org.assertj.core.api.Assertions.assertThat;

class RespondentPartyTest {

    @Test
    void shouldReturnYesWhenAddressKnowIsNullWithExistingAddress() {
        final RespondentParty actualRespondent = RespondentParty.builder()
            .addressKnow(null)
            .address(Address.builder()
                .addressLine1("XX Test Close")
                .country("United Kingdom")
                .postcode("AB1 BC2")
                .build())
            .build();

        assertThat(actualRespondent.getAddressKnow()).isNotNull();
        assertThat(actualRespondent.getAddressKnow()).isEqualTo(YesNo.YES.getValue());
    }
}
