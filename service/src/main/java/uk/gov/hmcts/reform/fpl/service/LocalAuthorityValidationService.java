package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityValidationService {

    public List<String> validateIfLaIsOnboarded(final String localAuthorityName) {

        List<String> errors = new ArrayList<>();

        errors.add("Test error");

        return errors;
    }
}
