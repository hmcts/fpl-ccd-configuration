package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, UploadCMOService.class})
class ReviewCMOServiceTest {

    @Test
    void dynamicListTests() {

    }

    @Test
    void getSelectedCMOTest() {

    }

    @Test
    void getCMOToSealTests() {

    }

    @Test
    void handlePageLogicTests() {

    }

    @Test
    void getCMOsReadyForApprovalTests() {

    }
}
