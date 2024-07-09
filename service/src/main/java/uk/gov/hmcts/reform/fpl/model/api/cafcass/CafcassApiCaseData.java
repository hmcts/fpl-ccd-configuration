package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;

import java.util.List;

@Builder
public class CafcassApiCaseData {
    private String familyManCaseNumber;
    private String dateSubmitted;
    private String applicationType;
    private List<String> ordersSought;
    private String dateOfCourtIssue;
    private boolean citizenIsApplicant;
    private String applicantLA;
    private String respondentLA;
    private List<?> applicants;
    private List<?> children;
    private List<?> respondents;
    private List<?> others;
    private Object internationalElement;
    private List<?> previousProceedings;
    private List<?> hearingDetails;
    private List<?> caseDocuments;
    private Object risks;
    private Object factorsParenting;
    private Object caseManagementLocation;
}
