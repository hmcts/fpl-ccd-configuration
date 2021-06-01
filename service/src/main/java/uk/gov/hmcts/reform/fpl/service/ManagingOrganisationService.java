package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.exceptions.CaseNotOutsourcedException;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationNotFound;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;

import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManagingOrganisationService {

    private final Time time;
    private final OrganisationService organisationService;

    public uk.gov.hmcts.reform.rd.model.Organisation getManagingOrganisation(CaseData caseData) {
        if (!caseData.isOutsourced()) {
            throw new CaseNotOutsourcedException(caseData.getId());
        }

        final String outsourcingOrganisationId = caseData.getOutsourcingPolicy().getOrganisation().getOrganisationID();

        return organisationService.findOrganisation(outsourcingOrganisationId)
            .orElseThrow(() -> new OrganisationNotFound(outsourcingOrganisationId));
    }

    public ChangeOrganisationRequest getRemovalRequest(CaseData caseData) {
        if (!caseData.isOutsourced()) {
            throw new CaseNotOutsourcedException(caseData.getId());
        }

        final OrganisationPolicy organisationPolicy = caseData.getOutsourcingPolicy();

        final DynamicListElement roleItem = DynamicListElement.builder()
            .code(organisationPolicy.getOrgPolicyCaseAssignedRole())
            .label(organisationPolicy.getOrgPolicyCaseAssignedRole())
            .build();

        return ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(time.now())
            .caseRoleId(DynamicList.builder()
                .value(roleItem)
                .listItems(List.of(roleItem))
                .build())
            .organisationToRemove(organisationPolicy.getOrganisation())
            .build();
    }
}
