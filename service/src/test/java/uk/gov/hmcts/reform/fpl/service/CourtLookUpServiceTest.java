package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Court;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_EPIMMS_ID;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_REGION;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_REGION_ID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CourtLookUpService.class})
class CourtLookUpServiceTest {

    @Autowired
    private CourtLookUpService courtLookUpService;

    @Test
    void shouldReturnFullCourtListWithHighCourt() {
        assertThat(courtLookUpService.getCourtFullListWithRcjHighCourt()).hasSize(3);
    }

    @Test
    void shouldReturnCourtByCode() {
        assertThat(courtLookUpService.getCourtByCode("117")).contains(
            Court.builder().code("117").name("Barnet").region("London").build()
        );
    }

    @Test
    void shouldReturnHighCourt() {
        assertThat(courtLookUpService.getCourtByCode("100")).contains(
            Court.builder().code(RCJ_HIGH_COURT_CODE)
                .name(RCJ_HIGH_COURT_NAME)
                .region(RCJ_HIGH_COURT_REGION)
                .regionId(RCJ_HIGH_COURT_REGION_ID)
                .epimmsId(RCJ_HIGH_COURT_EPIMMS_ID)
                .build()
        );
    }
}
