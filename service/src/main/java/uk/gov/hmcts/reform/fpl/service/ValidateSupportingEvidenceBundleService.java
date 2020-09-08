package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ValidateSupportingEvidenceBundleService {
    private final ValidateGroupService validatorService;

    public List<String> validateBundle(List<SupportingEvidenceBundle> documentBundle) {
        return documentBundle.stream()
            .map(validatorService::validateGroup)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
