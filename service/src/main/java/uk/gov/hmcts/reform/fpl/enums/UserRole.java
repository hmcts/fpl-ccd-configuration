package uk.gov.hmcts.reform.fpl.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum UserRole {
    DEFAULT {
        public List<String> getRoles() {
            return Arrays.asList("caseworker", "caseworker-publiclaw");
        }
    },

    LOCAL_AUTHORITY {
        public List<String> getRoles() {
            return roles(DEFAULT.getRoles(), "caseworker-publiclaw-solicitor");
        }
    },

    HNCTS_ADMIN {
        public List<String> getRoles() {
            return roles(DEFAULT.getRoles(),"caseworker-publiclaw-courtadmin");
        }
    },

    CAFCASS {
        public List<String> getRoles() {
            return roles(DEFAULT.getRoles(),"caseworker-publiclaw-cafcass");
        }
    },

    GATEKEEPER {
        public List<String> getRoles() {
            return roles(DEFAULT.getRoles(), "caseworker-publiclaw-gatekeeper");
        }
    },

    JUDICIARY {
        public List<String> getRoles() {
            return roles(DEFAULT.getRoles(), "caseworker-publiclaw-gatekeeper");
        }
    };

    public abstract List<String> getRoles();

    private static List<String> roles(final List<String> existingRoles, final String roleToAdd) {
        List<String> userRoles = new ArrayList<>(existingRoles);
        userRoles.add(roleToAdd);
        return userRoles;
    }
}
