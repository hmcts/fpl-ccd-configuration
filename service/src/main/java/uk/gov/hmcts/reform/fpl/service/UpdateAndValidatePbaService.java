package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.PBANumberHelper;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.updatePBANumber;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.validatePBANumber;

@Service
public class UpdateAndValidatePbaService {

    private final ObjectMapper mapper;

    @Autowired
    public UpdateAndValidatePbaService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public AboutToStartOrSubmitCallbackResponse updateAndValidatePbaNumbers(CaseDetails caseDetails) {
        ImmutableList.Builder<String> validationErrors = ImmutableList.builder();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isEmpty(caseData.getApplicants())) {
            List<Element<Applicant>> applicants = caseData.getApplicants().stream()
                .map(element -> {
                    Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();

                    if (!isEmpty(element.getValue().getParty().getPbaNumber())) {
                        String pba = updatePBANumber(element.getValue().getParty().getPbaNumber());
                        validationErrors.addAll(validatePBANumber(pba));

                        applicantBuilder.party(element.getValue().getParty().toBuilder().pbaNumber(pba).build());
                    }

                    return Element.<Applicant>builder()
                        .id(element.getId())
                        .value(applicantBuilder.build())
                        .build();
                })
                .collect(Collectors.toList());

            caseDetails.getData().put("applicants", applicants);

        } else {
            OldApplicant applicantData = caseData.getApplicant();

            if (isEmpty(applicantData.getPbaNumber())) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDetails.getData())
                    .errors(validationErrors.build())
                    .build();
            }

            String newPbaNumberData = PBANumberHelper.updatePBANumber(applicantData.getPbaNumber());
            validationErrors.addAll(validatePBANumber(newPbaNumberData));

            if (validationErrors.build().isEmpty()) {
                applicantData.setPbaNumber(newPbaNumberData);
                caseDetails.getData().put("applicant", applicantData);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors.build())
            .build();
    }
}
