package uk.gov.hmcts.reform.fpl.service.children.validation.user;

import uk.gov.hmcts.reform.fpl.service.UserService;

public abstract class LocalAuthorityUserValidator extends UserSpecificValidator {
    public LocalAuthorityUserValidator(UserService user) {
        super(user);
    }

    @Override
    protected final boolean acceptsUser() {
        return !user.isHmctsUser();
    }
}
