package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CafcassApiColleague {
    private String role;
    private String title;
    private String email;
    private String phone;
    private String fullName;
    private boolean mainContact;
    private boolean notificationRecipient;
}
