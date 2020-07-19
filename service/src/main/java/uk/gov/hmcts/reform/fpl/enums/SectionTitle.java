package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SectionTitle {
    ADD_APPLICATION_DETAILS("Add application details"),
    ADD_GROUNDS_FOR_THE_APPLICATION("Add grounds for the application"),
    ADD_SUPPORTING_DOCUMENTS("Add supporting documents"),
    ADD_INFORMATION_ABOUT_THE_PARTIES("Add information about the parties"),
    ADD_COURT_REQUIREMENTS("Add court requirements"),
    ADD_ADDITIONAL_INFORMATION("Add additional information"),
    SUBMIT_APPLICATION("Submit application");

    private final String sectionTitle;
}
