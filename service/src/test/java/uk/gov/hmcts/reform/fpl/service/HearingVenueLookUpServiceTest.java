package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;

import static org.assertj.core.api.Assertions.assertThat;

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

    private DynamicList createHearingBookingDynmaicList() {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .label("15th Dec 2019")
                .build())
            .listItems(List.of(DynamicListElement.builder().code(UUID.randomUUID()).label("test").build()))
            .build();
    }
}
