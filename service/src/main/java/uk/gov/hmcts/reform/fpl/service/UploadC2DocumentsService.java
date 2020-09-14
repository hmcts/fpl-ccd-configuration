package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadC2DocumentsService {

    private final SupportingEvidenceValidatorService validateSupportingEvidenceBundleService;

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(C2DocumentBundle::getSupportingEvidenceBundle)
            .map(validateSupportingEvidenceBundleService::validate)
            .orElse(emptyList());
    }
}
