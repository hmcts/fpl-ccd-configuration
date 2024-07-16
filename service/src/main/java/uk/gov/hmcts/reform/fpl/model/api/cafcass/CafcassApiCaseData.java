package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CafcassApiCaseData {
    private String familyManCaseNumber;
    private LocalDate dateSubmitted;
    private String applicationType;
    private List<OrderType> ordersSought;
    private LocalDate dateOfCourtIssue;
    private boolean citizenIsApplicant;
    private String applicantLA;
    private String respondentLA;
    private List<CafcassApiApplicant> applicants;
    private List<CafcassApiRespondent> respondents;
    private List<CafcassApiChild> children;
    private List<CafcassApiOther> others;
    private List<CafcassApiHearing> hearingDetails;
    private CafcassApiInternationalElement internationalElement;
    private List<CafcassApiProceeding> previousProceedings;
    private CafcassApiRisk risks;
    private CafcassApiFactorsParenting factorsParenting;
    private CafcassApiCaseManagementLocation caseManagementLocation;

    // TODO following field
    private List<?> caseDocuments;
}
