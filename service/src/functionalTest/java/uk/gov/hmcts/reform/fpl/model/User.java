package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;

@Data
public class User {
    String name;
    String password;


    public static User user(String userName) {
        User user = new User();
        user.setName(userName);
        return user;
    }
}
