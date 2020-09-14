package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadC2DocumentsService {

    private final ValidateSupportingEvidenceBundleService validateSupportingEvidenceBundleService;

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(c2Bundle -> unwrapElements(c2Bundle.getSupportingEvidenceBundle()))
            .map(validateSupportingEvidenceBundleService::validateBundle)
            .orElse(emptyList());
    }
}
