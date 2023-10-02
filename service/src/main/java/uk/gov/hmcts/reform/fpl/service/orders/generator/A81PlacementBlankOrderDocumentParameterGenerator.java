package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisBirthCertificate;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.A81PlacementBlankOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A81_PLACEMENT_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.Constants.NEW_LINE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_SHORT;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class A81PlacementBlankOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final PlacementService placementService;
    private final CourtService courtService;

    @Override
    public Order accept() {
        return A81_PLACEMENT_BLANK_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        LocalAuthority designatedLocalAuthority = Optional.ofNullable(caseData.getDesignatedLocalAuthority())
            .orElse(LocalAuthority.builder().address(Address.builder().build()).build());

        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        UUID placementId = manageOrdersEventData.getManageOrdersChildPlacementApplication().getValueCodeAsUUID();
        Placement selectedPlacementApplication = placementService.getPlacementById(caseData, placementId);
        ChildParty childInfo = placementService.getChildByPlacementId(caseData, placementId)
            .getValue()
            .getParty();

        String applicationDate = selectedPlacementApplication.getPlacementUploadDateTime()
            .format(ofPattern(DATE_SHORT));

        return A81PlacementBlankOrderDocmosisParameters.builder()
            .orderTitle(A81_PLACEMENT_BLANK_ORDER.getTitle())
            .childrenAct(A81_PLACEMENT_BLANK_ORDER.getChildrenAct())
            .localAuthorityName(designatedLocalAuthority.getName())
            .localAuthorityAddress(designatedLocalAuthority.getAddress().getAddressAsString(NEW_LINE))
            .child(
                DocmosisChild.builder()
                    .name(getStringValueOrDefault(childInfo.getFullName()))
                    .fathersName(getStringValueOrDefault(childInfo.getFathersName()))
                    .mothersName(getStringValueOrDefault(childInfo.getMothersName()))
                    .gender(Optional.ofNullable(childInfo.getGender())
                        .map(ChildGender::getLabel).orElse("unknown"))
                    .dateOfBirth(getFormattedDateOrDefault(childInfo.getDateOfBirth()))
                    .birthCertificate(DocmosisBirthCertificate.builder()
                        .number(manageOrdersEventData.getManageOrdersBirthCertificateNumber())
                        .date(manageOrdersEventData.getManageOrdersBirthCertificateDate())
                        .registrationDistrict(
                            manageOrdersEventData.getManageOrdersBirthCertificateRegistrationDistrict())
                        .registrationSubDistrict(
                            manageOrdersEventData.getManageOrdersBirthCertificateRegistrationSubDistrict())
                        .registrationCounty(manageOrdersEventData.getManageOrdersBirthCertificateRegistrationCounty())
                        .build())
                    .build()
            )
            .applicationDate(applicationDate)
            .isHighCourtCase(courtService.isHighCourtCase(caseData))
            .preamblesText(manageOrdersEventData.getManageOrdersPreamblesText())
            .costOrders(manageOrdersEventData.getManageOrdersCostOrders())
            .paragraphs(manageOrdersEventData.getManageOrdersParagraphs())
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.A81;
    }

    private String getStringValueOrDefault(String value) {
        return StringUtils.defaultIfEmpty(value, "unknown");
    }

    private String getFormattedDateOrDefault(LocalDate date) {
        return Optional.ofNullable(date)
            .map(dob -> dob.format(ofPattern(DATE_SHORT)))
            .orElse("unknown");
    }
}
