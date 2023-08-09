package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class IdentityServiceTest {

    @Test
    void shouldGenerateUniqueId() {
        int numberOfIds = 20;

        IdentityService identityService = new IdentityService();

        Set<UUID> ids = rangeClosed(1, numberOfIds)
            .mapToObj(id -> identityService.generateId())
            .collect(toSet());

        assertThat(ids).hasSize(numberOfIds);
    }
}
