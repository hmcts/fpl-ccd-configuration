package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.caseflag.CaseFlag;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseFlagsService {
/*
    private final String APPLICANT = "applicant";
    private final String CASE_APPLICANT_FLAGS = "applicantFlags";
    private final String CASE_LEVEL_FLAGS = "caseFlags";
    private final String CASE_LEVEL_ROLE = "";
    private final String CASE_RESPONDENT_FLAGS = "";
    private final String RESPONDENT = "";
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;

    public void setCaseFlagInformation(CaseDetails caseDetails) {
        log.info("Received request to update case flags with Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();

        updateCaseFlagsForParty(caseData, CASE_APPLICANT_FLAGS, caseDataService.buildFullApplicantName(caseDetails), APPLICANT);
        updateCaseFlagsForParty(caseData, CASE_RESPONDENT_FLAGS, caseDataService.buildFullRespondentName(caseDetails), RESPONDENT);
        updateCaseFlagsAtCaseLevel(caseData);
    }

    private void updateCaseFlagsAtCaseLevel(CaseData caseData) {
        CaseFlag caseFlagDetailsData = Optional.ofNullable(caseData.getCaseFlagsWrapper().getCaseFlags())
            .orElse(new CaseFlag());
        caseFlagDetailsData.setPartyName(CASE_LEVEL_ROLE);
        caseFlagDetailsData.setRoleOnCase(CASE_LEVEL_ROLE);
        caseData.getCaseFlagsWrapper().setCaseFlags(caseFlagDetailsData);
    }

    private void updateCaseFlagsAtCaseLevel(Map<String, Object> caseData) {
        CaseFlag caseFlagDetailsData = Optional.ofNullable(objectMapper.convertValue(caseData.get(CASE_LEVEL_FLAGS), CaseFlag.class))
            .orElse(new CaseFlag());
        caseFlagDetailsData.setPartyName(CASE_LEVEL_ROLE);
        caseFlagDetailsData.setRoleOnCase(CASE_LEVEL_ROLE);
        caseData.put(CASE_LEVEL_FLAGS, caseFlagDetailsData);
    }

    private void updateCaseFlagsForParty(CaseData caseData,
                                         String flagLevel,
                                         String party,
                                         String roleOnCase) {
        if (flagLevel.equalsIgnoreCase(CASE_APPLICANT_FLAGS)) {
            CaseFlag caseFlag = Optional.ofNullable(caseData.getCaseFlagsWrapper().getApplicantFlags())
                .orElse(new CaseFlag());
            caseFlag.setPartyName(party);
            caseFlag.setRoleOnCase(roleOnCase);
            caseData.getCaseFlagsWrapper().setApplicantFlags(caseFlag);
        } else if (flagLevel.equalsIgnoreCase(CASE_RESPONDENT_FLAGS)) {
            CaseFlag caseFlag = Optional.ofNullable(caseData.getCaseFlagsWrapper().getRespondentFlags())
                .orElse(new CaseFlag());
            caseFlag.setPartyName(party);
            caseFlag.setRoleOnCase(roleOnCase);
            caseData.getCaseFlagsWrapper().setRespondentFlags(caseFlag);
        }
    }

    private void updateCaseFlagsForParty(Map<String, Object> caseData,
                                         String flagLevel,
                                         String party,
                                         String roleOnCase) {
        CaseFlag caseFlag = Optional.ofNullable(objectMapper.convertValue(caseData.get(flagLevel), CaseFlag.class))
            .orElse(new CaseFlag());
        caseFlag.setPartyName(party);
        caseFlag.setRoleOnCase(roleOnCase);
        caseData.put(flagLevel, caseFlag);
    }
    */
}


