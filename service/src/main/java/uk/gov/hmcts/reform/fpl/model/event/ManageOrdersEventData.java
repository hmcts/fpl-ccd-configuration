package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.C29ActionsPermitted;
import uk.gov.hmcts.reform.fpl.enums.C36OrderType;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.enums.ChildArrangementsOrderType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.EnglandOffices;
import uk.gov.hmcts.reform.fpl.enums.Jurisdiction;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.PlacedUnderOrder;
import uk.gov.hmcts.reform.fpl.enums.ReasonForSecureAccommodation;
import uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.WalesOffices;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrderEndDateOption;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersChildAssessmentType;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ManageOrdersEventData {

    OrderOperation manageOrdersOperation;
    OrderOperation manageOrdersOperationClosedState;
    OrderTempQuestions orderTempQuestions;
    Order manageOrdersUploadType;
    String manageOrdersUploadTypeOtherTitle;
    Order manageOrdersType;
    DynamicList manageOrdersApprovedAtHearingList;
    State manageOrdersState;
    LocalDate manageOrdersApprovalDate;
    LocalDateTime manageOrdersApprovalDateTime;
    LocalDateTime manageOrdersEndDateTime;
    String manageOrdersFurtherDirections;
    String additionalAppointedSpecialGuardians;
    String manageOrdersIsFinalOrder;
    String manageOrdersTitle;
    String manageOrdersDirections;
    EPOType manageOrdersEpoType;
    String manageOrdersIncludePhrase;
    List<C43OrderType> manageOrdersMultiSelectListForC43;
    String manageOrdersRecitalsAndPreambles;
    String manageOrdersRecitalsAndPreamblesOptional;
    String manageOrdersIsByConsent;
    String manageOrdersChildrenDescription;
    String manageOrdersCareOrderIssuedCourt;
    LocalDate manageOrdersCareOrderIssuedDate;
    String manageOrdersExclusionRequirement;
    String manageOrdersExclusionDetails;
    String manageOrdersWhoIsExcluded;
    String manageOrdersNeedSealing;
    DocumentReference manageOrdersUploadOrderFile;
    DocumentReference manageOrdersPowerOfArrest;
    Address manageOrdersEpoRemovalAddress;
    LocalDate manageOrdersExclusionStartDate;
    ManageOrdersEndDateType manageOrdersEndDateTypeWithMonth;
    ManageOrdersEndDateType manageOrdersEndDateTypeWithEndOfProceedings;
    LocalDate manageOrdersSetDateEndDate;
    LocalDateTime manageOrdersSetDateAndTimeEndDate;
    Integer manageOrdersSetMonthsEndDate;
    String manageOrdersCloseCase;
    DynamicList whichChildIsTheOrderFor;
    ReasonForSecureAccommodation manageOrdersReasonForSecureAccommodation;
    String manageOrdersIsChildRepresented;
    Jurisdiction manageOrdersOrderJurisdiction;
    String manageOrdersCafcassRegion;
    LanguageTranslationRequirement manageOrdersTranslationNeeded;
    EnglandOffices manageOrdersCafcassOfficesEngland;
    WalesOffices manageOrdersCafcassOfficesWales;
    DynamicList manageOrdersLinkedApplication;
    String manageOrdersParentResponsible;
    RelationshipWithChild manageOrdersRelationshipWithChild;
    DynamicList manageOrdersAmendmentList;
    DocumentReference manageOrdersOrderToAmend;
    DocumentReference manageOrdersAmendedOrder;
    DynamicList manageOrdersChildPlacementApplication;
    String manageOrdersSerialNumber;
    String manageOrdersBirthCertificateNumber;
    String manageOrdersBirthCertificateDate;
    String manageOrdersBirthCertificateRegistrationDistrict;
    String manageOrdersBirthCertificateRegistrationSubDistrict;
    String manageOrdersBirthCertificateRegistrationCounty;
    String manageOrdersPlacementOrderOtherDetails;
    String manageOrdersLeaName;
    ManageOrderEndDateOption manageOrdersEndDateWithEducationAge;
    PlacedUnderOrder manageOrdersPlacedUnderOrder;
    String manageOrdersIsExParte;
    List<C29ActionsPermitted> manageOrdersActionsPermitted;
    Address manageOrdersActionsPermittedAddress;
    String manageOrdersOfficerName;
    LocalDate manageOrdersOrderCreatedDate;
    LocalDate manageOrdersAssessmentStartDate;
    ManageOrdersChildAssessmentType manageOrdersChildAssessmentType;
    Integer manageOrdersDurationOfAssessment;
    String manageOrdersPlaceOfAssessment;
    String manageOrdersAssessingBody;
    YesNo manageOrdersChildKeepAwayFromHome;
    Address manageOrdersFullAddressToStayIfKeepAwayFromHome;
    LocalDate manageOrdersStartDateOfStayIfKeepAwayFromHome;
    LocalDate manageOrdersEndDateOfStayIfKeepAwayFromHome;
    String manageOrdersChildFirstContactIfKeepAwayFromHome;
    String manageOrdersChildSecondContactIfKeepAwayFromHome;
    String manageOrdersChildThirdContactIfKeepAwayFromHome;
    YesNo manageOrdersDoesCostOrderExist;
    String manageOrdersCostOrderDetails;
    C36OrderType manageOrdersSupervisionOrderType;
    String manageOrdersSupervisionOrderCourtDirection;
    LocalDate manageOrdersSupervisionOrderApprovalDate;
    LocalDate manageOrdersSupervisionOrderEndDate;

    //deprecated since DFPL-1060 - use manageOrdersChildArrangementsOrderTypes instead
    ChildArrangementsOrderType manageOrdersChildArrangementsOrderType;
    List<ChildArrangementsOrderType> manageOrdersChildArrangementsOrderTypes;

    String manageOrdersPartyGrantedLeave;
    String manageOrdersChildNewSurname;
    DynamicList manageOrdersAllowedContact1;
    DynamicList manageOrdersAllowedContact2;
    DynamicList manageOrdersAllowedContact3;
    String manageOrdersConditionsOfContact;
    DynamicList manageOrdersParentageApplicant;
    DynamicList manageOrdersHearingParty1;
    DynamicList manageOrdersHearingParty2;
    DynamicList manageOrdersPersonWhoseParenthoodIs;
    DynamicList manageOrdersParentageAction;
    DynamicList manageOrdersPartyToBeBefriended1;
    DynamicList manageOrdersPartyToBeBefriended2;
    DynamicList manageOrdersPartyToBeBefriended3;
    LocalDate manageOrdersFamilyAssistanceEndDate;

    String manageOrdersPreamblesText;
    String manageOrdersParagraphs;
    String manageOrdersCostOrders;

    String manageOrdersNonMolestationOrder;
    DynamicList manageOrdersNonMolestationOrderApplicant;
    DynamicList manageOrdersNonMolestationOrderRespondent;

    @JsonIgnore
    public List<ChildArrangementsOrderType> getManageOrdersChildArrangementsOrderTypes() {
        if (isEmpty(manageOrdersChildArrangementsOrderTypes) && manageOrdersChildArrangementsOrderType != null) {
            return List.of(manageOrdersChildArrangementsOrderType);
        }
        return manageOrdersChildArrangementsOrderTypes;
    }

    @JsonIgnore
    public LocalDateTime getManageOrdersApprovalDateOrDateTime() {
        return Optional.ofNullable(manageOrdersApprovalDateTime)
            .or(() -> Optional.ofNullable(manageOrdersApprovalDate).map(LocalDate::atStartOfDay))
            .orElse(null);
    }
}
