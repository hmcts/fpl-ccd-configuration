package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.time.LocalDate;
import java.util.List;

@Data
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
    private List<CafcassApiCaseDocument> caseDocuments;
}
