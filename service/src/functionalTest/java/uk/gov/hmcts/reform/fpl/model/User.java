package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@ConstructorBinding
public class User {

    private final String name;
    private final String password;

    public static User user(String userName) {
        return new User(userName, null);
    }
}
