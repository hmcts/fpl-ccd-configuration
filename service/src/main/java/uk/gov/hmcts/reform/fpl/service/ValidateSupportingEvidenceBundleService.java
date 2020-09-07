package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ValidateSupportingEvidenceBundleService {
    private final ValidateGroupService validatorService;

    public <T> List<String> validateBundle(List<T> documentBundle) {
        return documentBundle.stream()
            .map(e -> validatorService.validateGroup(e, DateOfIssueGroup.class))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
