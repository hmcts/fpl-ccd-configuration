package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;

@Data
public class User {

    private final String name;
    private final String password;

    public static User user(String userName) {
        return new User(userName, null);
    }
}
