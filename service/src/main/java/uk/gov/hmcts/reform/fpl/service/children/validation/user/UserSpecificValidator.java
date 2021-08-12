package uk.gov.hmcts.reform.fpl.service.children.validation.user;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.service.UserService;

@RequiredArgsConstructor
public abstract class UserSpecificValidator {
    protected final UserService user;

    protected abstract boolean acceptsUser();
}
