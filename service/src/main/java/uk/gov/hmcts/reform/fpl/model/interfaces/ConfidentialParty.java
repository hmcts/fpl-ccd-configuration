package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.common.Party;

public interface ConfidentialParty<T> {
    boolean containsConfidentialDetails();

    Party getConfidentialParty();

    T setConfidentialParty(Party party);
}
