package uk.gov.hmcts.reform.fpl.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum UserRole {
    LOCAL_AUTHORITY {
        public List<String> getRoles() {
            return addRoles("caseworker-publiclaw-solicitor");
        }
    },

    HMCTS_ADMIN {
        public List<String> getRoles() {
            return addRoles("caseworker-publiclaw-courtadmin");
        }
    },

    CAFCASS {
        public List<String> getRoles() {
            return addRoles("caseworker-publiclaw-cafcass");
        }
    },

    GATEKEEPER {
        public List<String> getRoles() {
            return addRoles("caseworker-publiclaw-gatekeeper");
        }
    },

    JUDICIARY {
        public List<String> getRoles() {
            return addRoles("caseworker-publiclaw-judiciary");
        }
    };

    public abstract List<String> getRoles();

    private static List<String> addRoles(final String roleToAdd) {
        List<String> userRoles = new ArrayList<>();
        userRoles.addAll(Arrays.asList("caseworker", "caseworker-publiclaw"));
        userRoles.add(roleToAdd);
        return userRoles;
    }
}
