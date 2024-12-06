package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.A70PlacementOrderDocmosisParameters;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.Constants.NEW_LINE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_SHORT;

@Component
@RequiredArgsConstructor
public class A70PlacementOrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final PlacementService placementService;
    private final CourtService courtService;

    private static final String UNKNOWN = "unknown";

    @Override
    public Order accept() {
        return A70_PLACEMENT_ORDER;
    }

    @Override
    public A70PlacementOrderDocmosisParameters generate(CaseData caseData) {
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
        return A70PlacementOrderDocmosisParameters.builder()
            .orderTitle(A70_PLACEMENT_ORDER.getTitle())
            .childrenAct(A70_PLACEMENT_ORDER.getChildrenAct())
            .localAuthorityName(designatedLocalAuthority.getName())
            .localAuthorityAddress(designatedLocalAuthority.getAddress().getAddressAsString(NEW_LINE))
            .child(
                DocmosisChild.builder()
                    .name(getStringValueOrDefault(childInfo.getFullName()))
                    .fathersName(getStringValueOrDefault(childInfo.getFathersName()))
                    .mothersName(getStringValueOrDefault(childInfo.getMothersName()))
                    .gender(Optional.ofNullable(childInfo.getGender())
                        .map(ChildGender::getLabel).orElse(UNKNOWN))
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
                    .placementOrderOtherDetails(manageOrdersEventData.getManageOrdersPlacementOrderOtherDetails())
                    .build()
            )
            .applicationDate(applicationDate)
            .isHighCourtCase(courtService.isHighCourtCase(caseData))
            .build();
    }

    private String getFormattedDateOrDefault(LocalDate date) {
        return Optional.ofNullable(date)
            .map(dob -> dob.format(ofPattern(DATE_SHORT)))
            .orElse(UNKNOWN);
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.A70;
    }

    private String getStringValueOrDefault(String value) {
        return StringUtils.defaultIfEmpty(value, UNKNOWN);
    }

}
