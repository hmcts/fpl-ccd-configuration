package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupportingEvidenceValidatorService {
    private final ValidateGroupService validatorService;

    public List<String> validate(List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {
        return supportingEvidenceBundle.stream()
            .map(validatorService::validateGroup)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
