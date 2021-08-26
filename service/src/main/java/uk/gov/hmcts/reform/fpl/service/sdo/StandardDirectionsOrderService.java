package uk.gov.hmcts.reform.fpl.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.prepareJudgeFields;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class StandardDirectionsOrderService {
    private final DocumentSealingService sealingService;
    private final FeatureToggleService featureToggleService;
    private final Time time;
    private final IdamClient idamClient;
    private final RequestData requestData;

    public LocalDate generateDateOfIssue(StandardDirectionOrder order) {
        return Optional.ofNullable(order)
            .map(StandardDirectionOrder::getDateOfIssue)
            .map(dateOfIssue -> parseLocalDateFromStringUsingFormat(dateOfIssue, DATE))
            .orElse(time.now().toLocalDate());
    }

    public StandardDirectionOrder buildTemporarySDO(CaseData caseData, StandardDirectionOrder previousSDO) {
        DocumentReference document = caseData.getPreparedSDO();

        if (document == null) {
            // been through once, either pull from replacement doc or SDO if that isn't present
            document = defaultIfNull(
                caseData.getReplacementSDO(),
                previousSDO.getOrderDoc()
            );
        }

        StandardDirectionOrder.StandardDirectionOrderBuilder builder = StandardDirectionOrder.builder()
            .orderDoc(document);

        if (caseData.getJudgeAndLegalAdvisor() != null) {
            JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
                caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()
            );

            removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

            builder.judgeAndLegalAdvisor(judgeAndLegalAdvisor);
        }

        return builder.build();
    }

    public StandardDirectionOrder buildOrderFromUpload(StandardDirectionOrder currentOrder, SealType sealType) {
        UserInfo userInfo = idamClient.getUserInfo(requestData.authorisation());

        return StandardDirectionOrder.builder()
            .orderStatus(currentOrder.getOrderStatus())
            .dateOfUpload(time.now().toLocalDate())
            .uploader(userInfo.getName())
            .orderDoc(prepareOrderDocument(currentOrder.getOrderDoc(), currentOrder.getOrderStatus(), sealType))
            .lastUploadedOrder(currentOrder.isSealed() ? currentOrder.getOrderDoc() : null)
            .judgeAndLegalAdvisor(currentOrder.getJudgeAndLegalAdvisor()).build();
    }

    public JudgeAndLegalAdvisor getJudgeAndLegalAdvisorFromSDO(CaseData caseData) {
        StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder().build();

        if (standardDirectionOrder != null && isNotEmpty(standardDirectionOrder.getJudgeAndLegalAdvisor())) {
            judgeAndLegalAdvisor = standardDirectionOrder.getJudgeAndLegalAdvisor();
        }

        if (isNotEmpty(caseData.getAllocatedJudge())) {
            judgeAndLegalAdvisor = prepareSDOJudgeFields(caseData);
        }

        return judgeAndLegalAdvisor;
    }

    private JudgeAndLegalAdvisor prepareSDOJudgeFields(CaseData caseData) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder().build();

        if (isNotEmpty(caseData.getStandardDirectionOrder())
            && isNotEmpty(caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor())) {
            judgeAndLegalAdvisor = prepareJudgeFields(caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor(),
                caseData.getAllocatedJudge());
        }

        judgeAndLegalAdvisor.setAllocatedJudgeLabel(buildAllocatedJudgeLabel(caseData.getAllocatedJudge()));

        return judgeAndLegalAdvisor;
    }

    private DocumentReference prepareOrderDocument(DocumentReference document, OrderStatus status, SealType sealType) {
        if (status != OrderStatus.SEALED) {
            return document;
        }
        return sealingService.sealDocument(document, sealType);
    }
}
