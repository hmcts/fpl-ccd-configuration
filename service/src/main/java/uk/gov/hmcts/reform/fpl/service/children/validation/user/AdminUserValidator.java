package uk.gov.hmcts.reform.fpl.service.children.validation.user;

import uk.gov.hmcts.reform.fpl.service.UserService;

public abstract class AdminUserValidator extends UserSpecificValidator {
    public AdminUserValidator(UserService user) {
        super(user);
    }

    @Override
    protected final boolean acceptsUser() {
        return user.isHmctsAdminUser();
    }
}
