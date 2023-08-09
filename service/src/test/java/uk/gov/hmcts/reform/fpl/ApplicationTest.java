package uk.gov.hmcts.reform.fpl;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ApplicationTest {

    @Test
    void shouldEnableAsyncExecutions() {
        assertThat(Application.class).hasAnnotations(EnableAsync.class);
    }
}
