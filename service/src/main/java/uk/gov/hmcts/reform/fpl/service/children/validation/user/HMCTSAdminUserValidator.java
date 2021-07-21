package uk.gov.hmcts.reform.fpl.service.children.validation.user;

import uk.gov.hmcts.reform.fpl.service.UserService;

public abstract class HMCTSAdminUserValidator extends UserSpecificValidator {
    public HMCTSAdminUserValidator(UserService user) {
        super(user);
    }

    @Override
    protected final boolean acceptsUser() {
        return user.isHmctsAdminUser();
    }
}
