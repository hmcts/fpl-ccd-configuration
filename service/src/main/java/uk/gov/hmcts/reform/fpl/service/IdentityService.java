package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdentityService {

    public UUID generateId() {
        return UUID.randomUUID();
    }
}
