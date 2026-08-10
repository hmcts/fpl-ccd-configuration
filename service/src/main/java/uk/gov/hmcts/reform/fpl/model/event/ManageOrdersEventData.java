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
import uk.gov.hmcts.reform.fpl.enums.TransparencyOrderExpirationType;
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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCrudCaseworkerPubliclawJudiciaryCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus3RolesHlrtwbAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.model.ManageOrdersOperationClosedState;
import uk.gov.hmcts.reform.fpl.model.ManageOrdersUploadType;
import uk.gov.hmcts.reform.fpl.model.ManageOrdersType;
import uk.gov.hmcts.reform.fpl.model.ManageOrdersEndDateTypeWithMonth;
import uk.gov.hmcts.reform.fpl.model.ManageOrdersEndDateTypeWithEndOfProceedings;
import uk.gov.hmcts.reform.fpl.model.CafcassJurisdictionRegion;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ManageOrdersEventData {

    @CCD(
            label = "What do you want to do?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    OrderOperation manageOrdersOperation;
    @CCD(
            label = "What do you want to do?",
            searchable = false,
            typeParameterOverride = "ManageOrdersOperationClosedState",
            typeParameterClass = ManageOrdersOperationClosedState.class,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    OrderOperation manageOrdersOperationClosedState;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    OrderTempQuestions orderTempQuestions;
    @CCD(
            label = "Select order",
            searchable = false,
            typeParameterOverride = "ManageOrdersUploadType",
            typeParameterClass = ManageOrdersUploadType.class,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    Order manageOrdersUploadType;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersUploadTypeOtherTitle;
    @CCD(
            label = "Select order",
            searchable = false,
            typeParameterOverride = "ManageOrdersType",
            typeParameterClass = ManageOrdersType.class,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    Order manageOrdersType;
    @CCD(
            label = "Which hearing?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersApprovedAtHearingList;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    State manageOrdersState;
    @CCD(
            label = "Approval Date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersApprovalDate;
    @CCD(
            label = "Approval Date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDateTime manageOrdersApprovalDateTime;
    @CCD(
            label = "When does it end?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDateTime manageOrdersEndDateTime;
    @CCD(
            label = "Add further directions, if required",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersFurtherDirections;
    @CCD(
            label = "If not a party, detail the special guardians here",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    String additionalAppointedSpecialGuardians;
    @CCD(
            label = "Is this a final order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersIsFinalOrder;
    @CCD(
            label = "Add order title",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersTitle;
    @CCD(
            label = "Add order directions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersDirections;
    @CCD(
            label = "Type of emergency protection order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    EPOType manageOrdersEpoType;
    @CCD(
            label = "Include: \"Any person who can produce the children to the applicant must do so\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersIncludePhrase;
    @CCD(
            label = "Select orders to issue",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    List<C43OrderType> manageOrdersMultiSelectListForC43;
    @CCD(
            label = "Add details for child to live with order",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class, CaseworkerPubliclawGatekeeperCrudCaseworkerPubliclawJudiciaryCruAccess.class}
    )
    String manageOrdersChildArrangementsLiveWithDetails;
    @CCD(
            label = "Add details for child to have contact with order",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersChildArrangementsContactWithDetails;
    @CCD(
            label = "Add details for specific issue order",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersSpecificIssueOrderDetails;
    @CCD(
            label = "Add details for prohibited steps order",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersProhibitedStepsOrderDetails;
    @CCD(
            label = "Add recitals or preamble",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersRecitalsAndPreambles;
    @CCD(
            label = "Add recitals or preamble",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersRecitalsAndPreamblesOptional;
    @CCD(
            label = "Is order by consent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersIsByConsent;
    @CCD(
            label = "Add description of children",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersChildrenDescription;
    @CCD(
            label = "Which court issued the order?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HearingVenue",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersCareOrderIssuedCourt;
    @CCD(
            label = "When was the care order issued?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersCareOrderIssuedDate;
    @CCD(
            label = "Is there an exclusion requirement",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersExclusionRequirement;
    @CCD(
            label = "Add exclusion details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersExclusionDetails;
    @CCD(
            label = "Who's excluded",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersWhoIsExcluded;
    @CCD(
            label = "Do you need to seal the order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersNeedSealing;
    @CCD(
            label = " ",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DocumentReference manageOrdersUploadOrderFile;
    @CCD(
            label = "Upload power of arrest, if required",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DocumentReference manageOrdersPowerOfArrest;
    @CCD(
            label = "Address",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    Address manageOrdersEpoRemovalAddress;
    @CCD(
            label = "Date power of exclusion starts",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersExclusionStartDate;
    @CCD(
            label = "When does the order end?",
            searchable = false,
            typeParameterOverride = "ManageOrdersEndDateTypeWithMonth",
            typeParameterClass = ManageOrdersEndDateTypeWithMonth.class,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    ManageOrdersEndDateType manageOrdersEndDateTypeWithMonth;
    @CCD(
            label = "When does the order end?",
            searchable = false,
            typeParameterOverride = "ManageOrdersEndDateTypeWithEndOfProceedings",
            typeParameterClass = ManageOrdersEndDateTypeWithEndOfProceedings.class,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    ManageOrdersEndDateType manageOrdersEndDateTypeWithEndOfProceedings;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersSetDateEndDate;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDateTime manageOrdersSetDateAndTimeEndDate;
    @CCD(
            label = "Order length, in months",
            searchable = false,
            min = 1,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    Integer manageOrdersSetMonthsEndDate;
    @CCD(
            label = "Does this order close the case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersCloseCase;
    @CCD(
            label = "Which child is the order for?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    DynamicList whichChildIsTheOrderFor;
    @CCD(
            label = "Order given because the child is likely to",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    ReasonForSecureAccommodation manageOrdersReasonForSecureAccommodation;
    @CCD(
            label = "Does the child have a Legal Aid representative?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            typeParameterOverride = "ChildLegalAidRepresentative",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersIsChildRepresented;
    @CCD(
            label = "Jurisdiction",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    Jurisdiction manageOrdersOrderJurisdiction;
    @CCD(
            label = "Select jurisdiction",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "CafcassJurisdictionRegion",
            typeParameterClass = CafcassJurisdictionRegion.class,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersCafcassRegion;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LanguageTranslationRequirement manageOrdersTranslationNeeded;
    @CCD(
            label = "Select region",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CafcassEnglandOffices",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    EnglandOffices manageOrdersCafcassOfficesEngland;
    @CCD(
            label = "Select region",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CafcassWalesOffices",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    WalesOffices manageOrdersCafcassOfficesWales;
    @CCD(
            label = "Applications",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    DynamicList manageOrdersLinkedApplication;
    @CCD(
            label = "Who's been given parental responsibility",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersParentResponsible;
    @CCD(
            label = "Relationship with child",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    RelationshipWithChild manageOrdersRelationshipWithChild;
    @CCD(
            label = "Select order to amend",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersAmendmentList;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DocumentReference manageOrdersOrderToAmend;
    @CCD(
            label = "Upload the amended order. It will then be dated and stamped as amended.",
            regex = ".pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DocumentReference manageOrdersAmendedOrder;
    @CCD(
            label = "Select child placement application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersChildPlacementApplication;
    @CCD(
            label = "Serial number",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersSerialNumber;
    @CCD(
            label = "Birth certificate number",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersBirthCertificateNumber;
    @CCD(
            label = "Birth certificate date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersBirthCertificateDate;
    @CCD(
            label = "Registration District",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersBirthCertificateRegistrationDistrict;
    @CCD(
            label = "Registration Sub-district",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersBirthCertificateRegistrationSubDistrict;
    @CCD(
            label = "Registration County",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersBirthCertificateRegistrationCounty;
    @CCD(
            label = "Other details or any other directions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersPlacementOrderOtherDetails;
    @CCD(
            label = "Name of Local Education Authority (LEA)",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersLeaName;
    @CCD(
            label = "When does it end?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    ManageOrderEndDateOption manageOrdersEndDateWithEducationAge;
    @CCD(
            label = "Which order placed the child in the care of the local authority?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    PlacedUnderOrder manageOrdersPlacedUnderOrder;
    @CCD(
            label = "Has the order been made ex parte?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersIsExParte;
    @CCD(
            label = "What does the order allow?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    List<C29ActionsPermitted> manageOrdersActionsPermitted;
    @CCD(
            label = "Add address to be accessed",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    Address manageOrdersActionsPermittedAddress;
    @CCD(
            label = "Officer's name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersOfficerName;
    @CCD(
            label = "When was the order made?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersOrderCreatedDate;
    @CCD(
            label = "Assessment Start Date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersAssessmentStartDate;
    @CCD(
            label = "Types of Assessment",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    ManageOrdersChildAssessmentType manageOrdersChildAssessmentType;
    @CCD(
            label = "Duration of assessment (days)",
            searchable = false,
            min = 1,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    Integer manageOrdersDurationOfAssessment;
    @CCD(
            label = "Place of Assessment",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersPlaceOfAssessment;
    @CCD(
            label = "Assessing Body",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersAssessingBody;
    @CCD(
            label = "Is child kept away from home?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    YesNo manageOrdersChildKeepAwayFromHome;
    @CCD(
            label = "Full address",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    Address manageOrdersFullAddressToStayIfKeepAwayFromHome;
    @CCD(
            label = "From date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersStartDateOfStayIfKeepAwayFromHome;
    @CCD(
            label = "To date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersEndDateOfStayIfKeepAwayFromHome;
    @CCD(
            label = "Child's first contact",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersChildFirstContactIfKeepAwayFromHome;
    @CCD(
            label = "Child's second contact",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersChildSecondContactIfKeepAwayFromHome;
    @CCD(
            label = "Child's third contact",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersChildThirdContactIfKeepAwayFromHome;
    @CCD(
            label = "Is there a costs order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    YesNo manageOrdersDoesCostOrderExist;
    @CCD(
            label = "Cost order details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersCostOrderDetails;
    @CCD(
            label = "Please select the required order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    C36OrderType manageOrdersSupervisionOrderType;
    @CCD(
            label = "The court directs that",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersSupervisionOrderCourtDirection;
    @CCD(
            label = "Approval Date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersSupervisionOrderApprovalDate;
    @CCD(
            label = "End Date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersSupervisionOrderEndDate;

    //deprecated since DFPL-1060 - use manageOrdersChildArrangementsOrderTypes instead
    @CCD(
            label = "What is the order for?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    ChildArrangementsOrderType manageOrdersChildArrangementsOrderType;
    @CCD(
            label = "What is the order for?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    List<ChildArrangementsOrderType> manageOrdersChildArrangementsOrderTypes;

    @CCD(
            label = "Party granted leave",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersPartyGrantedLeave;
    @CCD(
            label = "Child/Children's new surname",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    String manageOrdersChildNewSurname;
    @CCD(
            label = "1st party allowed contact",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus3RolesHlrtwbAccess.class}
    )
    DynamicList manageOrdersAllowedContact1;
    @CCD(
            label = "2nd party allowed contact",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus3RolesHlrtwbAccess.class}
    )
    DynamicList manageOrdersAllowedContact2;
    @CCD(
            label = "3rd party allowed contact",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus3RolesHlrtwbAccess.class}
    )
    DynamicList manageOrdersAllowedContact3;
    @CCD(
            label = "Conditions of contact",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrudPlus3RolesHlrtwbAccess.class}
    )
    String manageOrdersConditionsOfContact;
    @CCD(
            label = "Who applied for the order on the case?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersParentageApplicant;
    @CCD(
            label = "Upon hearing party 1",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersHearingParty1;
    @CCD(
            label = "Upon hearing party 2",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersHearingParty2;
    @CCD(
            label = "Person applying for declaration of parentage",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersPersonWhoseParenthoodIs;
    @CCD(
            label = "Action",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersParentageAction;
    @CCD(
            label = "First party to be befriended",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersPartyToBeBefriended1;
    @CCD(
            label = "Second party to be befriended",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersPartyToBeBefriended2;
    @CCD(
            label = "Third party to be befriended",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    DynamicList manageOrdersPartyToBeBefriended3;
    @CCD(
            label = "End date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class}
    )
    LocalDate manageOrdersFamilyAssistanceEndDate;

    @CCD(
            label = "Preambles Text",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    String manageOrdersPreamblesText;
    @CCD(
            label = "Paragraphs",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    String manageOrdersParagraphs;
    @CCD(
            label = "Cost Orders",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    String manageOrdersCostOrders;

    @CCD(
            label = "The Court Orders",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    String manageOrdersNonMolestationOrder;
    @CCD(
            label = "Applicant of this order",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    DynamicList manageOrdersNonMolestationOrderApplicant;
    @CCD(
            label = "Respondent of this order",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    DynamicList manageOrdersNonMolestationOrderRespondent;

    @CCD(
            label = "The order will remain in force until:",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    TransparencyOrderExpirationType manageOrdersTransparencyOrderExpiration;
    @CCD(
            label = "End Date",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    LocalDate manageOrdersTransparencyOrderEndDate;
    @CCD(
            label = "If there are any more pieces of information relating to the proceedings, or a section of it, which may not be published to the public, please specify:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    String manageOrdersTransparencyOrderPublishInformationDetails;
    @CCD(
            label = "If there are any more bodies, agencies or professionals who may be identified in any information related to the proceedings, or a section of it, published to the general public, please specify:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    String manageOrdersTransparencyOrderPublishIdentityDetails;
    @CCD(
            label = "If there are any more documents that may be seen, quoted from or published by a pilot reporter, please specify:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    String manageOrdersTransparencyOrderPublishDocumentsDetails;
    @CCD(
            label = "Permission to report is not effective until:",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    LocalDate manageOrdersTransparencyOrderPermissionToReportEffectiveDate;

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
