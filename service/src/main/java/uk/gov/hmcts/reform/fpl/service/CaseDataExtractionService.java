package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CaseDataExtractionService {
    public Map<String, String> getNoticeOfProceedingTemplateData(CaseData caseData) {
        return Map.of(
            "familyManCaseNumber", caseData.getFamilyManCaseNumber() != null
                ? caseData.getFamilyManCaseNumber() : "",
            "todaysDate", LocalDateTime.now().toString(),
            "applicantName", getFirstApplicantName(caseData),
            "orderTypes", caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
                ? StringUtils.join(caseData.getOrders().getOrderType(), ", ") : "",
            "childrenNames", getAllChildrenNames(caseData),
            "hearingDate", "",
            "venue", "",
            "preHearingAttendance", "",
            "hearingTime", ""
        );
    }

    private String getFirstApplicantName(CaseData caseData) {
        if (caseData.getApplicants() == null) {
            return "";
        }

        List<String> applicantNames = caseData.getApplicants().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Applicant::getParty)
            .filter(Objects::nonNull)
            .map(ApplicantParty::getOrganisationName)
            .collect(Collectors.toList());

        return applicantNames.get(0);
    }

    private String getAllChildrenNames(CaseData caseData) {
        if (caseData.getChildren1() == null) {
            return "";
        }

        List<String> names =  caseData.getChildren1().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(childParty -> {
                return (childParty.getFirstName()) + " "
                    + (childParty.getLastName());
            }).collect(Collectors.toList());

        return StringUtils.join(names, ", ");
    }
}
