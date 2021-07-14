package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicantsListGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_FATHER;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_SECOND_FEMALE_PARENT;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ParentalResponsibilityPrePopulator implements QuestionBlockOrderPrePopulator {

    private final ApplicantsListGenerator applicantsListGenerator;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.PARENTAL_RESPONSIBILITY;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {

        Map<String, Object> data = new HashMap<>();

        if (hasDataAlreadySet(caseData)) {
            return Map.of();
        }

        DynamicList linkedApplication = caseData.getManageOrdersEventData().getManageOrdersLinkedApplication();

        UUID selectedApplicationId = linkedApplication.getValueCodeAsUUID();
        ApplicationsBundle selectedApplicationBundle = caseData.getApplicationBundleByUUID(selectedApplicationId);

        if (selectedApplicationBundle instanceof C2DocumentBundle) {
            C2DocumentBundle c2DocumentBundle = (C2DocumentBundle) selectedApplicationBundle;

            if (c2DocumentBundle.getC2AdditionalOrdersRequested()
                .contains(C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY)) {
                data.put("manageOrdersParentResponsible",
                    StringUtils.substringBefore(c2DocumentBundle.getApplicantName(), ", "));

                mapParentResponsibility(data, c2DocumentBundle.getParentalResponsibilityType());
            }

        } else {
            OtherApplicationsBundle otherApplicationsBundle = (OtherApplicationsBundle) selectedApplicationBundle;

            if (otherApplicationsBundle.getApplicationType() == OtherApplicationType.C1_PARENTAL_RESPONSIBILITY) {
                data.put("manageOrdersParentResponsible",
                    StringUtils.substringBefore(otherApplicationsBundle.getApplicantName(), ", "));

                mapParentResponsibility(data, otherApplicationsBundle.getParentalResponsibilityType());
            }

        }

        return data;
    }

    private void mapParentResponsibility(Map<String, Object> data, ParentalResponsibilityType type) {
        if (type == PR_BY_FATHER) {
            data.put("manageOrdersRelationshipWithChild", RelationshipWithChild.FATHER);
        } else if (type == PR_BY_SECOND_FEMALE_PARENT) {
            data.put("manageOrdersRelationshipWithChild", RelationshipWithChild.SECOND_FEMALE_PARENT);
        }
    }

    private boolean hasDataAlreadySet(CaseData caseData) {
        return caseData.getManageOrdersEventData().getManageOrdersLinkedApplication() != null
            && caseData.getManageOrdersEventData().getManageOrdersParentResponsible() != null
            && caseData.getManageOrdersEventData().getManageOrdersRelationshipWithChild() != null;
    }
}

