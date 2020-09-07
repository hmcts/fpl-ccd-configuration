package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadC2DocumentsService {

    private final ValidateGroupService validatorService;

    public List<String> validate(C2DocumentBundle c2DocumentBundle) {
        return Optional.ofNullable(c2DocumentBundle)
            .map(c2Bundle -> unwrapElements(c2Bundle.getSupportingEvidenceBundle()))
            .filter(list -> !list.isEmpty())
            .map(this::validateSupportingBundle)
            .orElse(emptyList());
    }

    private List<String> validateSupportingBundle(List<SupportingEvidenceBundle> supportingEvidenceBundle) {
        return supportingEvidenceBundle.stream()
            .map(supportingEvidence -> validatorService.validateGroup(supportingEvidence, DateOfIssueGroup.class))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
