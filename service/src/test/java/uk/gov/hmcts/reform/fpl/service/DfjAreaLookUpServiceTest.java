package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.DfjAreaCourtMapping;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, DfjAreaLookUpService.class})
class DfjAreaLookUpServiceTest {
    @Autowired
    private DfjAreaLookUpService dfjAreaLookUpService;
    @Test
    void shouldReturnDfjForAGivenCourtCode() {
        assertThat(dfjAreaLookUpService.getDfjArea("344"))
            .isEqualTo(DfjAreaCourtMapping.builder()
                .courtCode("344")
                .courtField("swanseaDFJCourt")
                .dfjArea("SWANSEA")
                .build());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForUnknowCourtCode() {
        Assertions.assertThatThrownBy(() -> dfjAreaLookUpService.getDfjArea("-1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No dfjArea found for court code: -1");
    }

    @Test
    void shouldReturnDistinctCourtFields() {
        assertThat(dfjAreaLookUpService.getAllCourtFields()).hasSize(43);
    }
}
