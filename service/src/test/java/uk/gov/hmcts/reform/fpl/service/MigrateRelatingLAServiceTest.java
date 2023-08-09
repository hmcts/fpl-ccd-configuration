package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, MigrateRelatingLAService.class})
class MigrateRelatingLAServiceTest {

    @Autowired
    private MigrateRelatingLAService migrateRelatingLAService;

    @Test
    void shouldReturnEmptyOptionalIfNoConfigForCase() {
        String caseId = "1234";

        assertThat(migrateRelatingLAService.getRelatingLAString(caseId)).isEqualTo(Optional.empty());
    }

    @Test
    void shouldReturnCorrectLACodeIfConfigForCase() {
        String caseId = "1234567890";

        assertThat(migrateRelatingLAService.getRelatingLAString(caseId)).isEqualTo(Optional.of("ABC"));
    }

}
