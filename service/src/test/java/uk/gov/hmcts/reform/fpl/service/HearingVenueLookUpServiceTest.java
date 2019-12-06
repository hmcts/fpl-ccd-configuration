package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;

import static java.util.UUID.fromString;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingDynmaicList;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ObjectMapper.class, HearingVenueLookUpService.class })
class HearingVenueLookUpServiceTest {

    @Autowired
    private HearingVenueLookUpService service;

    @Test
    void shouldGetDynamicHearingBookingElementFromDynamicList() {
        HearingDateDynamicElement dateDynamicElement =
            service.getHearingDynamicElement(createHearingBookingDynmaicList());

        assertThat(dateDynamicElement.getId()).isEqualTo(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"));
        assertThat(dateDynamicElement.getDate()).isEqualTo("15th Dec 2019");
    }
}
