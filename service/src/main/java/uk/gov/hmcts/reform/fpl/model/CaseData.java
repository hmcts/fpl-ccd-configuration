package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.enums.EPOExclusionRequirementType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.HearingReListOption;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.OutsourcingType;
import uk.gov.hmcts.reform.fpl.enums.ProceedingType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskUrgency;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.caselink.CaseLink;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.CaseLinksElement;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOChildren;
import uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder.EPOPhrase;
import uk.gov.hmcts.reform.fpl.model.event.AllocateJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.CaseProgressionReportEventData;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.model.event.HearingJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.event.RecordChildrenFinalDecisionsEventData;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.generated.OrderExclusionClause;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingDatesGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingEndDateGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.SealedSDOGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.UploadDocumentsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.epoordergroup.EPOEndDateGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasDocumentsIncludedInSwet;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsValidHearingEdit;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.EPOTimeRange;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasFutureEndDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasHearingEndDateAfterStartDate;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasTimeNotMidnight;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeNotMidnight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateTimeFromStringUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRPlus2RolesQqgmyhAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCruPlus13RolesVztsmsAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerCaaRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerCaaCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrudPlus38RolesDaunvuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACrudPlus25RolesDalfnpAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesBetqimAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.GSProfileRPlus3RolesRkhhdlAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORBCuPlus23RolesLimnqvAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus5RolesUumdqfAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus28RolesNrpimkAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRPlus3RolesDckcthAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERRSOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorCruPlus1RolesOjgtycAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorCaseworkerPubliclawSystemupdateCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCudPlus33RolesBhxlugAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGCruLAMANAGINGCruLASHAREDCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRCaseworkerPubliclawSystemupdateCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORBUPlus23RolesLfbtswAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERUPlus9RolesUvllojAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcasssystemupdateRPlus1RolesCgqrlsAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminRuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERUPlus40RolesUirdihAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassCaseworkerPubliclawCourtadminUAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCuCaseworkerPubliclawSystemupdateCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORACuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCruPlus38RolesPoytobAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminRPlus1RolesAetnqnAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCruSOLICITORCruCaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCudPlus36RolesErmxxuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassCuPlus8RolesXbyqysAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCudPlus3RolesNislmxAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcasssystemupdateRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus25RolesFufkkqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCaseworkerPubliclawSolicitorCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORCruPlus3RolesUpxliqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCrudPlus2RolesThgnehAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCAFCASSSOLICITORCaseworkerPubliclawCafcassCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCuPlus38RolesDjdcedAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCuPlus39RolesIbtvalAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrudPlus22RolesKsuwpoAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGCruPlus4RolesFtwwcyAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCruPlus1RolesYwyfdvAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateUAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawRparobotCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERRdAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASHAREDCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawSolicitorCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORLABARRISTERCaseworkerPubliclawCafcassRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawBulkscanCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawBulkscansystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORASOLICITORACrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACuPlus6RolesLofmtxAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCPlus39RolesIagdkzAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRPlus40RolesMjvsolAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LAMANAGINGCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASHAREDCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORARPlus24RolesXmdwczAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERSOLICITORRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCudPlus14RolesZohtrwAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerApproverCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerApproverCrudPlus2RolesNhaismAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCruPlus18RolesCoxqdqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCdAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCaseworkerPubliclawMagistrateRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCrudPlus2RolesGjbeqhAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateDAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRCaseworkerPubliclawSolicitorCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERSOLICITORRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACrudPlus3RolesApwidhAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGCruLABARRISTERRLAMANAGINGRLASHAREDRLASOLICITORRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus3RolesQtjkvuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORASOLICITORACruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLASHAREDCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LAMANAGINGCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawBulkscanCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawBulkscansystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawJudiciaryCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORARAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrudPlus22RolesWyrnzzAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORARAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASHAREDRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruCaseworkerPubliclawJudiciaryCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGCaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.GSProfileRPlus3RolesOxxhtyAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.GSProfileRPlus40RolesFkdfmiAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCruPlus43RolesIgpdzgAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.TTLProfileCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawJudiciaryCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminLegalAdviserCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrudPlus39RolesKamvspAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCrAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawJudiciaryCrAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCrAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrdPlus2RolesJepnldAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERRPlus2RolesZbkigdAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERUPlus20RolesUqsdckAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORAUPlus23RolesAtfmazAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCaseworkerPubliclawCourtadminRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrudPlus38RolesIhmdtsAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCrudCaseworkerPubliclawSystemupdateCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LAMANAGINGCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORACrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORBCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORCCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORDCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORECrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORFCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORGCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORHCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORICrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORJCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORBCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORCCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORDCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORECrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORFCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORGCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORHCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORICrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORJCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORKCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORLCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORMCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORNCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITOROCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawJudiciaryCaseworkerPubliclawMagistrateCrudAccess;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Schedule;
import uk.gov.hmcts.reform.fpl.model.Recitals;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.CourtBundleV2;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.Documents;
import uk.gov.hmcts.reform.fpl.model.DocumentWithConfidentialAddress;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentType;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentSubtypeList;
import uk.gov.hmcts.reform.fpl.model.HearingDocumentType;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentTypeLA;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentSubtypeListLA;
import uk.gov.hmcts.reform.fpl.enums.JudgeType;
import uk.gov.hmcts.reform.fpl.model.FeePaidJudgeTitle;
import uk.gov.hmcts.reform.fpl.model.ManualLegalAdvisorDetail;
import uk.gov.hmcts.reform.fpl.model.ShowHide;
import uk.gov.hmcts.reform.fpl.model.Consent;
import uk.gov.hmcts.reform.fpl.model.DeletionConsent;
import uk.gov.hmcts.reform.fpl.model.ExpertReport;
import uk.gov.hmcts.reform.fpl.model.children.ChildFinalDecisionDetails;
import uk.gov.hmcts.reform.fpl.model.ConfirmPR;
import uk.gov.hmcts.reform.fpl.model.HighCourtDFJCourts;
import uk.gov.hmcts.reform.fpl.model.CentralLondonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.EastLondonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WestLondonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BirminghamDFJCourts;
import uk.gov.hmcts.reform.fpl.model.CoventryDFJCourts;
import uk.gov.hmcts.reform.fpl.model.DerbyDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LeicesterDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LincolnDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorthamptonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NottinghamDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WolverhamptonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WorcesterDFJCourts;
import uk.gov.hmcts.reform.fpl.model.ClevelandAndSouthDurhamDFJCourts;
import uk.gov.hmcts.reform.fpl.model.HumbersideDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorthYorkshireDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorthumbriaAndNorthDurhamDFJCourts;
import uk.gov.hmcts.reform.fpl.model.SouthYorkshireDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WestYorkshireDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BlackburnLancasterDFJCourts;
import uk.gov.hmcts.reform.fpl.model.CarlisleDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LiverpoolDFJCourts;
import uk.gov.hmcts.reform.fpl.model.ManchesterDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BrightonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.EssexAndSuffolkDFJCourts;
import uk.gov.hmcts.reform.fpl.model.GuildfordDFJCourts;
import uk.gov.hmcts.reform.fpl.model.LutonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.MedwayDFJCourts;
import uk.gov.hmcts.reform.fpl.model.MiltonKeynesDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorwichDFJCourts;
import uk.gov.hmcts.reform.fpl.model.PeterboroughDFJCourts;
import uk.gov.hmcts.reform.fpl.model.ReadingDFJCourts;
import uk.gov.hmcts.reform.fpl.model.WatfordDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BournemouthAndDorsetDFJCourts;
import uk.gov.hmcts.reform.fpl.model.BristolDFJCourts;
import uk.gov.hmcts.reform.fpl.model.DevonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.PortsmouthDFJCourts;
import uk.gov.hmcts.reform.fpl.model.SwindonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.TauntonDFJCourts;
import uk.gov.hmcts.reform.fpl.model.TruroDFJCourts;
import uk.gov.hmcts.reform.fpl.model.NorthWalesDFJCourts;
import uk.gov.hmcts.reform.fpl.model.SouthEastWalesDFJCourts;
import uk.gov.hmcts.reform.fpl.model.SwanseaDFJCourts;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.EditableStandardDirection;
import uk.gov.hmcts.reform.fpl.model.ImmediateStandardDirection;
import uk.gov.hmcts.reform.fpl.model.ReviewedListingAction;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@HasDocumentsIncludedInSwet(groups = UploadDocumentsGroup.class)
@IsValidHearingEdit(groups = HearingBookingGroup.class)
@HasHearingEndDateAfterStartDate(message = "The end date and time must be after the start date and time",
    groups = HearingEndDateGroup.class)
@EPOTimeRange(message = "Date must be within 8 days of the order date", groups = EPOEndDateGroup.class,
    maxDate = @TimeDifference(amount = 8, unit = DAYS))
public class CaseData extends CaseDataParent {
    public static final int DEFAULT_CASE_COMPLETION = 26;
    @CCD(ignore = true)
    private final Long id;
    @CCD(
            label = "End State",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRPlus2RolesQqgmyhAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final State state;
    @CCD(
            label = "Enter draft case name",
            hint = "It will be standardised when you submit the application",
            regex = "^(?!.*<[^>\\d]+>*).*",
            access = {BARRISTERCruPlus13RolesVztsmsAccess.class}
    )
    @NotBlank(message = "Enter a case name")
    private final String caseName;
    @CCD(
            label = "Local Authority :",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "AuthorityFixedList",
            access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private String caseLocalAuthority;
    @CCD(
            label = "Local Authority :",
            access = {CaseworkerCaaRAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private String caseLocalAuthorityName;
    @CCD(
            label = "Organisation policy",
            access = {CaseworkerCaaCruAccess.class, CaseworkerPubliclawSolicitorCruAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private OrganisationPolicy localAuthorityPolicy;
    @CCD(
            label = "Outsourcing policy",
            access = {CaseworkerCaaCruAccess.class, CaseworkerPubliclawSolicitorCruAccess.class}
    )
    private OrganisationPolicy outsourcingPolicy;
    @CCD(
            label = "Secondary local authority policy",
            access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class, CaseworkerCaaCruAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private OrganisationPolicy sharedLocalAuthorityPolicy;
    @CCD(
            label = "Outsourcing type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CaseOutsourcingType",
            access = {CaseworkerPubliclawSolicitorCrudAccess.class}
    )
    private OutsourcingType outsourcingType;
    @CCD(
            label = "Select who you are representing",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCrudAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class}
    )
    private RepresentativeType representativeType;
    @CCD(
            label = "Is local authority?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawSolicitorCrudAccess.class, CaseworkerPubliclawSystemupdateRAccess.class}
    )
    private YesNo isLocalAuthority;
    @CCD(label = " ", access = {BARRISTERCrudPlus38RolesDaunvuAccess.class})
    private String latestQueryID;

    @JsonIgnore
    public boolean checkIfCaseIsSubmittedByLA() {
        // isLocalAuthority is set to No if submitted by solicitor user and act as respondent / child solicitor
        // otherwise, it could be null or Yes
        return RepresentativeType.LOCAL_AUTHORITY.equals(representativeType);
    }

    @CCD(
            label = "Select the local authority you're representing",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawSolicitorCrudAccess.class}
    )
    private Object outsourcingLAs;
    @CCD(
            label = "Select the local authority which relates to the case",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "AuthorityFixedList",
            access = {CaseworkerPubliclawSolicitorCrudAccess.class, CaseworkerPubliclawSystemupdateRAccess.class}
    )
    private String relatingLA;
    @CCD(
            label = "Court to issue",
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawCourtadminCrudPlus2RolesBetqimAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    private Court court;
    @CCD(label = "Past Court List", searchable = false)
    private List<Element<Court>> pastCourtList;
    @JsonIgnore
    private String courtField;
    @CCD(
            label = "DFJ Area :",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DFJArea",
            access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private String dfjArea;

    @CCD(label = "Linked Cases", searchable = false, access = {GSProfileRPlus3RolesRkhhdlAccess.class})
    @JsonProperty("caseLinks")
    private List<CaseLinksElement<CaseLink>> caseLinks;

    @Builder.Default
    @JsonUnwrapped
    private final AllocateJudgeEventData allocateJudgeEventData = new AllocateJudgeEventData();
    @Builder.Default
    @JsonUnwrapped
    private final HearingJudgeEventData hearingJudgeEventData = new HearingJudgeEventData();


    public List<Element<Court>> getPastCourtList() {
        return defaultIfNull(pastCourtList, new ArrayList<>());
    }

    public void setPastCourtList(List<Element<Court>> pastCourtList) {
        this.pastCourtList = pastCourtList;
    }

    @CCD(
            label = "Would you like to share the case with all members of your organisation?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawSolicitorCruAccess.class}
    )
    private YesNo shouldShareWithOrganisationUsers;

    @CCD(
            label = "Has many associated courts",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private YesNo multiCourts;

    @CCD(
            label = "Risks and harm to children",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private final Risks risks;
    @CCD(
            label = "Orders and directions sought",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    @NotNull(message = "Add the orders and directions sought")
    @Valid
    private final Orders orders;
    @CCD(
            label = "Orders and directions sought",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, LABARRISTERRSOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private final Orders ordersSolicitor;
    @CCD(
            label = "How does this case meet the threshold criteria?",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    @NotNull(message = "Add the grounds for the application")
    @Valid
    private final Grounds grounds;
    @CCD(
            label = "How are there grounds for a child assessment order?",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCuAccess.class}
    )
    @NotNull(message = "Add the grounds for the application")
    @Valid
    private final GroundsForChildAssessmentOrder groundsForChildAssessmentOrder;
    @CCD(
            label = "How are there grounds for an emergency protection order?",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    @NotNull(message = "Add the grounds for the application", groups = EPOGroup.class)
    @Valid
    private final GroundsForEPO groundsForEPO;
    @CCD(
            label = "Applicants",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Applicants",
            access = {CaseworkerPubliclawSolicitorCruPlus1RolesOjgtycAccess.class}
    )
    @NotEmpty(message = "Add applicant's details")
    @Valid
    @Deprecated
    private final List<@NotNull(message = "Add applicant's details") Element<Applicant>> applicants;

    // This holds all applicants, not just LA's
    @CCD(
            label = "Applicant",
            searchable = false,
            access = {CaseworkerPubliclawSolicitorCaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private List<@NotNull(message = "Add applicant's details") Element<LocalAuthority>> localAuthorities;

    @CCD(
            label = "Respondents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentNew",
            access = {BARRISTERCudPlus33RolesBhxlugAccess.class}
    )
    @Valid
    @NotEmpty(message = "Add the respondents' details")
    private final List<@NotNull(message = "Add the respondents' details") Element<Respondent>> respondents1;

    @CCD(
            label = "Other proceedings",
            searchable = false,
            access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawSolicitorCruPlus1RolesOjgtycAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    private final Proceeding proceeding;

    @CCD(
            label = "Solicitor",
            searchable = false,
            access = {EPSMANAGINGCruLAMANAGINGCruLASHAREDCuAccess.class, LASOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    @Deprecated
    @NotNull(message = "Add the applicant's solicitor's details")
    @Valid
    private final Solicitor solicitor;
    @CCD(
            label = "Factors affecting parenting",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private final FactorsParenting factorsParenting;

    @CCD(
            label = "Allocation proposal",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSystemupdateCudAccess.class}
    )
    @NotNull(message = "Add the allocation proposal")
    @Valid
    private final Allocation allocationProposal;
    @CCD(
            label = "Allocation decision",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    private final Allocation allocationDecision;

    @CCD(
            label = "Gatekeeping order",
            searchable = false,
            access = {CHILDSOLICITORBUPlus23RolesLfbtswAccess.class, BARRISTERUPlus9RolesUvllojAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesCgqrlsAccess.class, CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCrudAccess.class, CaseworkerPubliclawCourtadminRuAccess.class, CaseworkerPubliclawMagistrateRuAccess.class}
    )
    private final StandardDirectionOrder standardDirectionOrder;
    @CCD(label = "Urgent directions order", searchable = false, access = {BARRISTERUPlus40RolesUirdihAccess.class})
    private final StandardDirectionOrder urgentDirectionsOrder;

    @CCD(
            label = "Gatekeeping order - urgent hearing order",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCuAccess.class, CaseworkerPubliclawCafcassCaseworkerPubliclawCourtadminUAccess.class, CaseworkerPubliclawSuperuserCuCaseworkerPubliclawSystemupdateCudAccess.class, CHILDSOLICITORACuAccess.class, SOLICITORACuAccess.class}
    )
    @Deprecated
    private final UrgentHearingOrder urgentHearingOrder;

    @CCD(
            label = "What do you want to do?",
            hint = "You can upload the final order to send to all parties now, or just upload it and save.",
            searchable = false,
            access = {DefaultAccess.class}
    )
    private GatekeepingOrderRoute sdoRouter;
    @CCD(
            label = "Order type",
            hint = "What do you want to do?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    private GatekeepingOrderRoute gatekeepingOrderRouter;
    @CCD(
            label = "Order type",
            hint = "What do you want to do?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    private GatekeepingOrderRoute urgentDirectionsRouter;

    @CCD(
            label = "Attach prepared order",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private final DocumentReference preparedSDO;
    @CCD(
            label = "Or upload an updated order",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    private final DocumentReference replacementSDO;

    @CCD(
            label = "Allocated Judge",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSystemupdateCudAccess.class, CaseworkerPubliclawCafcassRAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    @NotNull(message = "You need to enter the allocated judge.",
        groups = {SealedSDOGroup.class, HearingBookingDetailsGroup.class})
    private final Judge allocatedJudge;

    @CCD(
            label = "Justices' Legal Adviser's full name",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    @Temp
    private final String legalAdvisorName;
    @CCD(
            label = "Is the allocated judge or magistrate sitting this hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    @Temp
    private final YesNo useAllocatedJudge;

    @CCD(
            label = "Hearing needed",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSystemupdateCudAccess.class, CaseworkerPubliclawCafcassRAccess.class}
    )
    @NotNull(message = "Add the hearing urgency details")
    @Valid
    private final Hearing hearing;
    @CCD(
            label = "Court services",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, DefaultAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private final HearingPreferences hearingPreferences;
    @CCD(
            label = "International element",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, DefaultAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSystemupdateCudAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    private final InternationalElement internationalElement;
    @CCD(
            label = "C1 with supplement (Original application)",
            searchable = false,
            access = {BARRISTERCruPlus38RolesPoytobAccess.class}
    )
    private final SubmittedC1WithSupplementBundle submittedC1WithSupplement;

    @CCD(
            label = "Additional documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherDocument",
            access = {CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    @JsonProperty("documents_socialWorkOther")
    private final List<Element<DocumentSocialWorkOther>> otherSocialWorkDocuments;

    @CCD(
            label = "4. Care plan",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminRPlus1RolesAetnqnAccess.class}
    )
    @JsonProperty("documents_socialWorkCarePlan_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document socialWorkCarePlanDocument;
    @CCD(
            label = "2. Social work statement and genogram",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminRPlus1RolesAetnqnAccess.class}
    )
    @JsonProperty("documents_socialWorkStatement_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document socialWorkStatementDocument;
    @CCD(
            label = "3. Social work assessment",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminRPlus1RolesAetnqnAccess.class}
    )
    @JsonProperty("documents_socialWorkAssessment_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document socialWorkAssessmentDocument;
    @CCD(
            label = "1. Social work chronology",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminRPlus1RolesAetnqnAccess.class}
    )
    @JsonProperty("documents_socialWorkChronology_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document socialWorkChronologyDocument;
    @CCD(
            label = "7. Checklist document",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, LABARRISTERCruSOLICITORCruCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    @JsonProperty("documents_checklist_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document checklistDocument;
    @CCD(
            label = "6. Threshold document",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, LABARRISTERCruSOLICITORCruCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    @JsonProperty("documents_threshold_document")
    @NotNull(message = "Add social work documents, or details of when you'll send them")
    @Valid
    public final Document thresholdDocument;
    @CCD(
            label = "5. Social work evidence template (SWET)",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, LABARRISTERCruSOLICITORCruCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    @JsonProperty("documents_socialWorkEvidenceTemplate_document")
    @Valid
    public final Document socialWorkEvidenceTemplateDocument;
    @CCD(
            label = "Child",
            searchable = false,
            max = 15,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ChildrenNew",
            access = {BARRISTERCudPlus36RolesErmxxuAccess.class}
    )
    @NotEmpty(message = "Add the child's details")
    @Valid
    private final List<@NotNull(message = "Add the child's details") Element<Child>> children1;
    @CCD(
            label = "Guardian",
            searchable = false,
            access = {CaseworkerPubliclawCafcasssystemupdateRPlus1RolesCgqrlsAccess.class}
    )
    private final List<Element<Guardian>> guardians;
    @CCD(
            label = "FamilyMan case number",
            regex = "^[a-zA-Z0-9]*$",
            access = {CaseworkerPubliclawCafcassCuPlus8RolesXbyqysAccess.class}
    )
    @NotBlank(message = "Enter Familyman case number", groups = {NoticeOfProceedingsGroup.class,
        ValidateFamilyManCaseNumberGroup.class})
    private final String familyManCaseNumber;
    @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
    private final NoticeOfProceedings noticeOfProceedings;
    @CCD(
            label = "Party",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsSentToParty",
            access = {CaseworkerPubliclawCourtadminCuAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private final List<Element<SentDocuments>> documentsSentToParties;

    @JsonIgnore
    @Deprecated
    public List<Element<Applicant>> getAllApplicants() {
        return applicants != null ? applicants : new ArrayList<>();
    }

    @JsonIgnore
    public List<Element<Respondent>> getAllRespondents() {
        return respondents1 != null ? respondents1 : new ArrayList<>();
    }

    public RepresentativeType getRepresentativeType() {
        return representativeType != null ? representativeType : RepresentativeType.LOCAL_AUTHORITY;
    }

    // This is a clone of the first respondent on the case in new 3rd party standalone apps, used for pre-filling data
    // on case creation.
    @CCD(
            label = "Details of local authority in this case",
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    public final RespondentLocalAuthority respondentLocalAuthority;

    @JsonIgnore
    public List<Element<Child>> getAllChildren() {
        return children1 != null ? children1 : new ArrayList<>();
    }

    public Orders getOrders() {
        return ordersSolicitor != null && ordersSolicitor.getOrderType() != null ? ordersSolicitor : orders;
    }

    //TODO add null-checker getter for hearingDetails during refactor/removal of legacy code (FPLA-2280)
    @CCD(
            label = "Hearing",
            access = {CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class, CaseworkerPubliclawCourtadminCudPlus3RolesNislmxAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    @NotNull(message = "Enter hearing details", groups = NoticeOfProceedingsGroup.class)
    @NotEmpty(message = "You need to enter a hearing date.", groups = SealedSDOGroup.class)
    @JsonProperty
    private List<Element<HearingBooking>> hearingDetails;
    @CCD(
            label = "Adjourned or vacated hearing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CancelledHearingBooking",
            access = {CaseworkerPubliclawCourtadminCudPlus3RolesNislmxAccess.class, CaseworkerPubliclawCafcasssystemupdateRAccess.class}
    )
    @JsonProperty
    private List<Element<HearingBooking>> cancelledHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final List<Element<UUID>> selectedHearingIds;

    @JsonIgnore
    public Optional<Element<HearingBooking>> findHearingBookingElement(UUID elementId) {
        return findElement(elementId, hearingDetails);
    }

    @JsonIgnore
    public Optional<Element<HearingBooking>> getHearingLinkedToCMO(UUID removedOrderId) {
        return getAllHearings().stream()
            .filter(hearingBookingElement ->
                removedOrderId.equals(hearingBookingElement.getValue().getCaseManagementOrderId()))
            .findFirst();
    }

    @CCD(label = "Date submitted", access = {CaseworkerPubliclawSystemupdateCudAccess.class})
    private LocalDate dateSubmitted;
    @CCD(
            label = "Previous date submitted of a returned case",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private LocalDate lastSubmittedDate;
    @CCD(
            label = "Notice of proceedings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NoticeOfProceedingsBundle",
            access = {CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private final List<Element<DocumentBundle>> noticeOfProceedingsBundle;
    @CCD(
            label = "Recipients",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CaseworkerPubliclawSolicitorCudAccess.class}
    )
    private final List<Element<Recipients>> statementOfService;
    @CCD(
            label = "Judge and Justices' Legal Adviser",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    @CCD(
            label = "C2 application",
            searchable = false,
            access = {CHILDSOLICITORACruPlus25RolesFufkkqAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class, LABARRISTERCaseworkerPubliclawCourtadminCruAccess.class, BARRISTERCaseworkerPubliclawSolicitorCruAccess.class}
    )
    private final C2DocumentBundle temporaryC2Document;
    @CCD(
            label = "Other applications",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class}
    )
    private final OtherApplicationsBundle temporaryOtherApplicationsBundle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CAFCASSSOLICITORCruPlus3RolesUpxliqAccess.class, BARRISTERCruAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    private final PBAPayment temporaryPbaPayment;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class, BARRISTERCAFCASSSOLICITORCaseworkerPubliclawCafcassCrudAccess.class}
    )
    private final YesNo isCTSCUser;
    @CCD(label = "C2 Application", searchable = false, access = {BARRISTERCuPlus38RolesDjdcedAccess.class})
    private final List<Element<C2DocumentBundle>> c2DocumentBundle;
    @CCD(label = "Additional applications", searchable = false, access = {BARRISTERCuPlus39RolesIbtvalAccess.class})
    private final List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle;
    @CCD(
            label = "Select applicant",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class}
    )
    private final DynamicList applicantsList;
    @CCD(
            label = "Add applicant's name",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class}
    )
    private final String otherApplicant;

    @CCD(
            label = "Upload assessment form or security requests",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, CaseworkerPubliclawMagistrateCruAccess.class}
    )
    private final DocumentReference redDotAssessmentForm;
    @CCD(
            label = "Additional notes",
            hint = "These will be saved in a separate document.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, CaseworkerPubliclawMagistrateCruAccess.class}
    )
    private final String caseFlagNotes;
    @CCD(
            label = "Add a case flag?",
            hint = "Select 'No' to remove a case flag.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawMagistrateCruAccess.class}
    )
    private final String caseFlagAdded;
    // Transient field
    @CCD(ignore = true)
    private YesNo caseFlagValueUpdated;

    @JsonIgnore
    public boolean hasC2DocumentBundle() {
        return isNotEmpty(c2DocumentBundle);
    }

    @JsonIgnore
    public boolean hasApplicationBundles() {
        return isNotEmpty(c2DocumentBundle) || isNotEmpty(additionalApplicationsBundle);
    }

    @JsonIgnore
    public C2DocumentBundle getLastC2DocumentBundle() {
        return Stream.of(ElementUtils.unwrapElements(c2DocumentBundle))
            .filter(list -> !list.isEmpty())
            .map(c2DocumentBundles -> c2DocumentBundles.get(c2DocumentBundles.size() - 1))
            .findFirst()
            .orElse(null);
    }

    @JsonIgnore
    public C2DocumentBundle getC2DocumentBundleByUUID(UUID elementId) {
        return nullSafeList(c2DocumentBundle).stream()
            .filter(c2DocumentBundleElement -> c2DocumentBundleElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    public DynamicList buildC2DocumentDynamicList() {
        return buildC2DocumentDynamicList(null);
    }

    public DynamicList buildC2DocumentDynamicList(UUID selected) {
        IncrementalInteger i = new IncrementalInteger(1);
        return asDynamicList(c2DocumentBundle, selected, documentBundle -> documentBundle.toLabel(i.getAndIncrement()));
    }

    public DynamicList buildApplicationBundlesDynamicList() {
        return buildApplicationBundlesDynamicList(null);
    }

    public DynamicList buildApplicationBundlesDynamicList(UUID selected) {
        List<Element<ApplicationsBundle>> applicationsBundles = getAllApplicationsBundles();

        Comparator<Element<ApplicationsBundle>> reverseChronological = comparing((Element<ApplicationsBundle> bundle) ->
            parseLocalDateTimeFromStringUsingFormat(bundle.getValue().getUploadedDateTime(), DATE_TIME, TIME_DATE))
            .reversed();

        applicationsBundles.sort(Comparator
            .comparing((Element<ApplicationsBundle> bundle) -> bundle.getValue().getSortOrder())
            .thenComparing(reverseChronological));

        return asDynamicList(applicationsBundles, selected, ApplicationsBundle::toLabel);
    }

    @JsonIgnore
    public List<Element<ApplicationsBundle>> getAllApplicationsBundles() {
        List<Element<ApplicationsBundle>> applicationBundles = new ArrayList<>();

        ofNullable(c2DocumentBundle).ifPresent(
            bundle -> bundle.forEach(c2 -> applicationBundles.add(element(c2.getId(), c2.getValue()))));

        unwrapElements(getAdditionalApplicationsBundle()).forEach(
            bundle -> {
                ofNullable(bundle.getC2DocumentBundle()).ifPresent(
                    c2 -> applicationBundles.add(element(c2.getId(), c2)));
                ofNullable(bundle.getC2DocumentBundleConfidential()).ifPresent(
                    c2 -> applicationBundles.add(element(c2.getId(), c2)));
                ofNullable(bundle.getOtherApplicationsBundle()).ifPresent(
                    otherBundle -> applicationBundles.add(element(otherBundle.getId(), otherBundle)));
            }
        );

        return applicationBundles;
    }

    @JsonIgnore
    public ApplicationsBundle getApplicationBundleByUUID(UUID elementId) {
        return getAllApplicationsBundles().stream()
            .filter(bundleElement -> bundleElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    @CCD(
            label = "Upload C2",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CAFCASSSOLICITORCruPlus3RolesUpxliqAccess.class, CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryRAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final Map<String, C2ApplicationType> c2ApplicationType;
    @CCD(
            label = "What type of C2 application?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class}
    )
    private final C2ApplicationType c2Type;
    @CCD(
            label = "Is this a confidential application? If you select yes, only yourself and HMCTS will be able to view this application.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class}
    )
    private final YesNo isC2Confidential;
    @CCD(
            label = "Type of order",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class}
    )
    private final OrderTypeAndDocument orderTypeAndDocument;
    @CCD(
            label = "What are you applying for?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class}
    )
    private final List<AdditionalApplicationType> additionalApplicationType;

    public List<AdditionalApplicationType> getAdditionalApplicationType() {
        return defaultIfNull(additionalApplicationType, emptyList());
    }

    @CCD(
            label = "Further directions",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final FurtherDirections orderFurtherDirections;
    @CCD(
            label = "Exclusion Clause",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final OrderExclusionClause orderExclusionClause;
    @CCD(
            label = "Create an order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class}
    )
    private final GeneratedOrder order;
    @CCD(
            label = "Add the sealed order",
            regex = ".pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final DocumentReference uploadedOrder;
    @JsonIgnore
    private OrderStatus generatedOrderStatus;
    @CCD(
            label = "How many months will this order remain in place",
            searchable = false,
            min = 1,
            max = 99,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final Integer orderMonths;
    @CCD(
            label = "When will this order end?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final InterimEndDate interimEndDate;
    @CCD(
            label = "Who’s included in the order?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class}
    )
    private final Selector childSelector;
    @CCD(
            label = "Select who should get the order",
            searchable = false,
            access = {BARRISTERCrudPlus22RolesKsuwpoAccess.class}
    )
    private final Selector othersSelector;
    @CCD(
            label = "Select who should be notified",
            searchable = false,
            access = {EPSMANAGINGCruPlus4RolesFtwwcyAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    private final Selector respondentsSelector;
    @CCD(
            label = "Select who should be notified",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CAFCASSSOLICITORCruPlus3RolesUpxliqAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    private final Selector personSelector;
    @CCD(
            label = "Which orders are being discharged?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final Selector careOrderSelector;
    @CCD(
            label = "Send notice of hearing",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final Selector newHearingSelector;
    @CCD(
            label = "Who's the appointed guardian?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    private final Selector appointedGuardianSelector;
    @CCD(
            label = "Select person(s) refused contact",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final Selector respondentsRefusedSelector;

    @CCD(
            label = "Is the order about all the children?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class}
    )
    private final String orderAppliesToAllChildren;
    @CCD(
            label = "Send order to all others in the case?",
            hint = "The applicant and respondents will also be sent this order",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCruPlus1RolesYwyfdvAccess.class}
    )
    private final String sendOrderToAllOthers;
    @CCD(
            label = "Send notice of hearing for placement to all respondents?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {EPSMANAGINGCruPlus4RolesFtwwcyAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    private final String sendPlacementNoticeToAllRespondents;
    @CCD(
            label = "Respondents to notify",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentNew"
    )
    private final List<Element<Respondent>> placementRespondentsToNotify;

    @CCD(
            label = "Notify all people on the case about this application?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CAFCASSSOLICITORCruPlus3RolesUpxliqAccess.class, BARRISTERCaseworkerPubliclawCourtadminCrudAccess.class}
    )
    private final String notifyApplicationsToAllOthers;

    public String getOrderAppliesToAllChildren() {
        return getAllChildren().size() == 1 ? YES.getValue() : orderAppliesToAllChildren;
    }

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class}
    )
    private String remainingChildIndex;

    @CCD(
            label = "Enter the date that the order was issued",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class}
    )
    @PastOrPresent(message = "Date of issue cannot be in the future", groups = DateOfIssueGroup.class)
    private final LocalDate dateOfIssue;
    @CCD(
            label = "Order",
            searchable = false,
            access = {CHILDSOLICITORBUPlus23RolesLfbtswAccess.class, BARRISTERUPlus9RolesUvllojAccess.class, CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesCgqrlsAccess.class, CaseworkerPubliclawMagistrateUAccess.class}
    )
    private final List<Element<GeneratedOrder>> orderCollection;
    @JsonUnwrapped
    @Builder.Default
    private final ConfidentialGeneratedOrders confidentialOrders = ConfidentialGeneratedOrders.builder().build();

    public List<Element<GeneratedOrder>> getOrderCollection() {
        return orderCollection != null ? orderCollection : new ArrayList<>();
    }

    @JsonIgnore
    public List<Element<GeneratedOrder>> getAllOrderCollections() {
        return Stream.of(getOrderCollection(), confidentialOrders.getAllConfidentialOrders())
            .flatMap(List::stream)
            .toList();
    }

    @JsonUnwrapped
    @Builder.Default
    private final RemovalToolData removalToolData = RemovalToolData.builder().build();

    @CCD(
            label = "Others to be given notice",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSystemupdateCudAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class, CaseworkerPubliclawSolicitorCruAccess.class}
    )
    private final Others others;

    @CCD(
            label = "Does any respondent, child or other person on this case need orders or court documents in Welsh?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawCafcassCruAccess.class, CaseworkerPubliclawRparobotCruAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private final String languageRequirement;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    private final String languageRequirementUrgent; // Replica field to work with Urgent Hearing

    @JsonIgnore
    public boolean isWelshLanguageRequested() {
        Optional<String> languageValue = Optional.ofNullable(languageRequirement);
        if (languageValue.isEmpty()) {
            return false;
        }
        return languageValue.get().equals("Yes");
    }

    @JsonIgnore
    public SealType getSealType() {
        return isWelshLanguageRequested() ? SealType.WELSH : SealType.ENGLISH;
    }

    @JsonIgnore
    public Language getImageLanguage() {
        return isWelshLanguageRequested() ? Language.WELSH : Language.ENGLISH;
    }

    @CCD(
            label = "Representatives",
            searchable = false,
            access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private final List<Element<Representative>> representatives;

    @JsonIgnore
    public List<Representative> getRepresentativesByServedPreference(RepresentativeServingPreferences preference) {
        return getRepresentativesElementsByServedPreference(preference).stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Element<Representative>> getRepresentativesElementsByServedPreference(
        RepresentativeServingPreferences preference
    ) {
        if (isNotEmpty(representatives)) {
            return representatives.stream()
                .filter(Objects::nonNull)
                .filter(representative -> preference == representative.getValue().getServingPreferences())
                .collect(toList());
        }
        return emptyList();
    }

    @CCD(
            label = "LA counsel/external solicitors",
            searchable = false,
            access = {CaseworkerPubliclawSolicitorCaseworkerPubliclawSystemupdateCudAccess.class, LABARRISTERRdAccess.class, LASHAREDCrudAccess.class}
    )
    private final List<Element<LegalRepresentative>> legalRepresentatives;

    // EPO Order
    @CCD(
            label = "Enter the date and time that the order was issued",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    @PastOrPresent(message = "Date of issue cannot be in the future", groups = DateOfIssueGroup.class)
    private final LocalDateTime dateAndTimeOfIssue;
    @CCD(
            label = "Describe the children",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final EPOChildren epoChildren;
    @CCD(
            label = "When does the order end",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    @TimeNotMidnight(message = "Enter a valid end time", groups = EPOEndDateGroup.class)
    @Future(message = "Enter an end date in the future", groups = EPOEndDateGroup.class)
    private final LocalDateTime epoEndDate;
    @CCD(
            label = "Do you want to include this phrase in the order?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final EPOPhrase epoPhrase;
    @CCD(
            label = "What type of EPO are you requesting?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final EPOType epoType;
    @CCD(
            label = "Address",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    @Valid
    private final Address epoRemovalAddress;
    @CCD(
            label = "Who's excluded?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCrudAccess.class}
    )
    private final String epoWhoIsExcluded;
    @CCD(
            label = "Exclusion start date",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCrudAccess.class}
    )
    private final LocalDate epoExclusionStartDate;
    @CCD(
            label = "Is there an exclusion requirement?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCrudAccess.class}
    )
    private final EPOExclusionRequirementType epoExclusionRequirementType;

    @JsonIgnore
    public List<Element<Proceeding>> getAllProceedings() {
        List<Element<Proceeding>> proceedings = new ArrayList<>();

        ofNullable(this.getProceeding()).map(ElementUtils::element).ifPresent(proceedings::add);
        ofNullable(this.getProceeding())
            .map(Proceeding::getAdditionalProceedings).ifPresent(proceedings::addAll);

        return Collections.unmodifiableList(proceedings);
    }

    @JsonIgnore
    public String getRelevantProceedings() {
        return ofNullable(this.getProceeding())
            .map(Proceeding::getOnGoingProceeding)
            .orElse("");
    }

    @JsonIgnore
    public List<Element<Other>> getAllOthers() {
        List<Element<Other>> othersList = new ArrayList<>();

        ofNullable(this.getOthers()).map(Others::getFirstOther).filter(not(Other::isEmpty))
            .map(ElementUtils::element).ifPresent(othersList::add);
        ofNullable(this.getOthers()).map(Others::getAdditionalOthers).ifPresent(othersList::addAll);

        return Collections.unmodifiableList(othersList);
    }

    public Optional<Other> findOther(int sequenceNo) {
        List<Other> allOthers = this.getAllOthers().stream().map(Element::getValue).collect(toList());

        return allOthers.size() <= sequenceNo ? empty() : Optional.of(allOthers.get(sequenceNo));
    }

    public Optional<Respondent> findRespondent(int seqNo) {
        return isEmpty(getRespondents1()) || getRespondents1().size() <= seqNo
            ? empty() : Optional.of(getRespondents1().get(seqNo).getValue());
    }

    public Optional<Element<Respondent>> findRespondent(UUID id) {
        return findElement(id, getAllRespondents());
    }

    @JsonIgnore
    public boolean hasRespondentsOrOthers() {
        return isNotEmpty(getAllRespondents()) || isNotEmpty(getAllOthers());
    }

    @JsonIgnore
    public String getFurtherDirectionsText() {
        return Optional.ofNullable(orderFurtherDirections).map(FurtherDirections::getDirections).orElse("");
    }

    @JsonIgnore
    public String getExclusionClauseText() {
        return Optional.ofNullable(orderExclusionClause).map(OrderExclusionClause::getExclusionClause).orElse("");
    }

    @CCD(
            label = "Child",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ChildrenNew",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSolicitorCudAccess.class}
    )
    private final List<Element<Child>> confidentialChildren;

    public List<Element<Child>> getConfidentialChildren() {
        return confidentialChildren != null ? confidentialChildren : new ArrayList<>();
    }

    @JsonUnwrapped
    @Builder.Default
    private final ChildrenEventData childrenEventData = ChildrenEventData.builder().build();

    @CCD(
            label = "Respondents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentNew",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSolicitorCudAccess.class}
    )
    private final List<Element<Respondent>> confidentialRespondents;

    public List<Element<Respondent>> getConfidentialRespondents() {
        return confidentialRespondents != null ? confidentialRespondents : new ArrayList<>();
    }

    @CCD(
            label = "Others",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Others",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSolicitorCudAccess.class}
    )
    private final List<Element<Other>> confidentialOthers;

    public List<Element<Other>> getConfidentialOthers() {
        return Optional.ofNullable(confidentialOthers).orElse(new ArrayList<>());
    }

    public boolean hasConfidentialParty() {
        return isNotEmpty(getConfidentialChildren()) || isNotEmpty(getConfidentialRespondents())
               || isNotEmpty(getConfidentialOthers());
    }

    @CCD(
            label = "Note",
            hint = "Add note detail, including relevant dates and people involved",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final String caseNote;
    @CCD(label = "Note", searchable = false, access = {CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class})
    private final List<Element<CaseNote>> caseNotes;
    @CCD(
            label = "Gatekeeper",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GatekeeperEmail",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private final List<Element<EmailAddress>> gatekeeperEmails;

    @JsonIgnore
    public LocalDate getDefaultCompletionDate() {
        return dateSubmitted.plusWeeks(DEFAULT_CASE_COMPLETION);
    }

    @JsonIgnore
    public String getComplianceDeadline() {
        return formatLocalDateToString(getDefaultCompletionDate(), FormatStyle.LONG);
    }

    @CCD(
            label = "Application fee to pay",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private final String amountToPay;

    @CCD(label = "Extended timeline date", searchable = false, access = {CaseworkerPubliclawCourtadminCuAccess.class})
    private LocalDate caseCompletionDate;
    @CCD(
            label = "New end date",
            hint = "For example, 31 3 1980",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class, CaseworkerPubliclawGatekeeperRAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    @FutureOrPresent(message = "Enter an end date in the future", groups = CaseExtensionGroup.class)
    private LocalDate extensionDateOther;
    @CCD(
            label = "New end date",
            hint = "For example, 31 3 1980",
            searchable = false,
            access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    @FutureOrPresent(message = "Enter an end date in the future", groups = CaseExtensionGroup.class)
    private LocalDate eightWeeksExtensionDateOther;
    @CCD(
            label = "You can extend this date again, but you will not be able to revert back to the original date.",
            searchable = false
    )
    private final CaseExtensionTime caseExtensionTimeList;
    @CCD(
            label = "Do you still want to extend the date by 8 weeks?",
            searchable = false,
            access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final CaseExtensionTime caseExtensionTimeConfirmationList;
    @CCD(label = "Why is this case being extended?", searchable = false)
    private final CaseExtensionReasonList caseExtensionReasonList;
    @JsonUnwrapped
    @Builder.Default
    private final ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder().build();

    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    private final CloseCase closeCase;
    @CCD(
            label = "Flag for whether a case was closed due to deprivation of liberty",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    private final String deprivationOfLiberty;
    @CCD(
            label = "Close the case",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private final CloseCase closeCaseTabField;
    @CCD(
            label = "Do you want to close the case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class}
    )
    private final String closeCaseFromOrder;
    @JsonUnwrapped
    @Builder.Default
    private final RecordChildrenFinalDecisionsEventData recordChildrenFinalDecisionsEventData =
        RecordChildrenFinalDecisionsEventData.builder().build();

    @JsonUnwrapped
    @Builder.Default
    private final ManageDocumentEventData manageDocumentEventData = ManageDocumentEventData.builder().build();
    @CCD(
            label = "Other documents",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CAFCASSSOLICITORLABARRISTERCaseworkerPubliclawCafcassRAccess.class, BARRISTERCaseworkerPubliclawSolicitorCruAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final List<Element<CourtAdminDocument>> otherCourtAdminDocuments;
    @CCD(
            label = "Scanned Documents",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawBulkscanCrudAccess.class, CaseworkerPubliclawBulkscansystemupdateCrudAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final List<Element<ScannedDocument>> scannedDocuments;


    @JsonUnwrapped
    @Builder.Default
    private final HearingDocuments hearingDocuments = HearingDocuments.builder().build();

    public List<Element<HearingCourtBundle>> getCourtBundleListV2() {
        return hearingDocuments.getCourtBundleListV2();
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListCTSC() {
        return hearingDocuments.getCourtBundleListCTSC();
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListLA() {
        return hearingDocuments.getCourtBundleListLA();
    }

    public Placement getPlacement() {
        return placementEventData.getPlacement();
    }

    public List<Element<Placement>> getPlacements() {
        return placementEventData.getPlacements();
    }

    public List<Element<AdditionalApplicationsBundle>> getHiddenApplicationsBundle() {
        return removalToolData.getHiddenApplicationsBundle();
    }

    public DynamicList buildDynamicChildrenList(UUID selected) {
        return buildDynamicChildrenList(getAllChildren(), selected);
    }

    public DynamicList buildDynamicChildrenList(List<Element<Child>> children, UUID selected) {
        return asDynamicList(children, selected, child -> child.getParty().getFullName());
    }

    @JsonIgnore
    public boolean isClosedFromOrder() {
        return YES.getValue().equals(closeCaseFromOrder);
    }

    @CCD(
            label = "Return details",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
    )
    private final ReturnApplication returnApplication;

    public boolean allocatedJudgeExists() {
        return allocatedJudge != null;
    }

    public boolean hasAllocatedJudgeEmail() {
        return allocatedJudgeExists() && isNotEmpty(allocatedJudge.getJudgeEmailAddress());
    }

    @JsonIgnore
    public Optional<HearingBooking> getFirstHearing() {
        return unwrapElements(hearingDetails).stream()
            .min(comparing(HearingBooking::getStartDate));
    }

    @JsonIgnore
    public Optional<HearingBooking> getFirstHearingOfType(HearingType type) {
        return unwrapElements(hearingDetails).stream()
            .filter(hearingBooking -> hearingBooking.isOfType(type))
            .min(comparing(HearingBooking::getStartDate));
    }

    @JsonIgnore
    public Optional<HearingBooking> getFirstHearingOfTypes(List<HearingType> types) {
        return unwrapElements(hearingDetails).stream()
            .filter(hearingBooking -> types.stream().anyMatch(type -> hearingBooking.isOfType(type)))
            .min(comparing(HearingBooking::getStartDate));
    }

    @JsonIgnore
    public Optional<HearingBooking> getNextHearingAfter(LocalDateTime time) {
        return unwrapElements(hearingDetails).stream()
            .filter(hearingBooking -> hearingBooking.getStartDate().isAfter(time))
            .min(comparing(HearingBooking::getStartDate));
    }

    @JsonIgnore
    public Optional<HearingBooking> getLastHearingBefore(LocalDateTime time) {
        return unwrapElements(hearingDetails).stream()
            .filter(hearingBooking -> hearingBooking.getStartDate().isBefore(time))
            .max(comparing(HearingBooking::getStartDate));
    }

    @JsonIgnore
    public HearingBooking getMostUrgentHearingBookingAfter(LocalDateTime time) {
        return getNextHearingAfter(time).orElseThrow(NoHearingBookingException::new);
    }

    @JsonIgnore
    public List<Element<HearingBooking>> addCancelledHearingBooking(Element<HearingBooking> hearing) {
        if (cancelledHearingDetails == null) {
            cancelledHearingDetails = new ArrayList<>();
        }
        this.cancelledHearingDetails.add(hearing);
        return this.cancelledHearingDetails;
    }

    @JsonIgnore
    public List<Element<HearingBooking>> addHearingBooking(Element<HearingBooking> hearing) {
        if (hearingDetails == null) {
            hearingDetails = new ArrayList<>();
        }
        hearingDetails.add(hearing);
        return hearingDetails;
    }

    @JsonIgnore
    public List<Element<HearingBooking>> setHearingDetails(List<Element<HearingBooking>> hearings) {
        this.hearingDetails = hearings;
        return hearingDetails;
    }

    @JsonIgnore
    public void removeHearingDetails(Element<HearingBooking> hearing) {
        if (hearingDetails != null) {
            hearingDetails.remove(hearing);
        }
    }

    public boolean hasFutureHearing(List<Element<HearingBooking>> hearingBookings) {
        return isNotEmpty(hearingBookings) && hearingBookings.stream()
            .anyMatch(hearingBooking -> hearingBooking.getValue().startsAfterToday());
    }

    @JsonUnwrapped
    @Builder.Default
    private final C110A c110A = C110A.builder().build();
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CHILDSOLICITORASOLICITORACrudAccess.class}
    )
    private final DocumentReference draftApplicationDocument;

    @CCD(
            label = "Draft case management order",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACuPlus6RolesLofmtxAccess.class, CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCuAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private final List<Element<HearingOrder>> draftUploadedCMOs;
    @CCD(
            label = "Hearing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OrdersBundleForApproval",
            access = {BARRISTERCPlus39RolesIagdkzAccess.class}
    )
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts;
    @CCD(
            label = "Hearing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OrdersBundleForReview",
            access = {BARRISTERRPlus40RolesMjvsolAccess.class}
    )
    private List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftReview;
    @CCD(
            label = "Refused Order",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCuAccess.class, CaseworkerPubliclawSuperuserCuCaseworkerPubliclawSystemupdateCudAccess.class, EPSMANAGINGCuAccess.class, LAMANAGINGCuAccess.class, LASHAREDCuAccess.class, LASOLICITORCuAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrders;
    @CCD(
            label = "Removed draft orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingOrderForApproval",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCuAccess.class, CaseworkerPubliclawSuperuserCudAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    @Setter
    private List<Element<HearingOrder>> draftOrdersRemoved;
    @JsonUnwrapped
    @Builder.Default
    private ConfidentialRefusedOrders confidentialRefusedOrders = ConfidentialRefusedOrders.builder().build();
    @CCD(
            label = "Hearing",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private final UUID lastHearingOrderDraftsHearingId;

    @JsonIgnore
    public List<Element<HearingOrdersBundle>> getBundlesForApproval() {
        return defaultIfNull(getHearingOrdersBundlesDrafts(), new ArrayList<Element<HearingOrdersBundle>>())
            .stream().filter(bundle -> isNotEmpty(bundle.getValue().getOrders(SEND_TO_JUDGE))
                                       || isNotEmpty(bundle.getValue().getAllConfidentialOrdersByStatus(SEND_TO_JUDGE)))
            .collect(toList());
    }

    @CCD(
            label = "Last upload draft order event resulted in a review order task",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, LABARRISTERCruAccess.class, SOLICITORCruAccess.class}
    )
    private final YesNo draftOrderNeedsReviewUploaded;

    @JsonUnwrapped
    @Builder.Default
    private final UploadDraftOrdersData uploadDraftOrdersEventData = UploadDraftOrdersData.builder().build();

    public List<Element<HearingOrder>> getDraftUploadedCMOs() {
        return defaultIfNull(draftUploadedCMOs, new ArrayList<>());
    }

    public Optional<Element<HearingOrder>> getDraftUploadedCMOWithId(UUID orderId) {
        return getDraftUploadedCMOs().stream()
            .filter(draftCmoElement -> draftCmoElement.getId().equals(orderId))
            .findFirst();
    }

    @JsonIgnore
    public List<Element<HearingOrder>> getOrdersFromHearingOrderDraftsBundles() {
        return Stream.concat(nullSafeList(hearingOrdersBundlesDrafts).stream(),
                nullSafeList(hearingOrdersBundlesDraftReview).stream())
            .map(Element::getValue)
            .flatMap((HearingOrdersBundle hearingOrdersBundle)
                -> hearingOrdersBundle.getOrders().stream())
            .collect(toList());
    }

    public Optional<Element<HearingOrdersBundle>> getHearingOrderBundleThatContainsOrder(UUID orderId) {
        return Stream.concat(nullSafeList(hearingOrdersBundlesDrafts).stream(),
                    nullSafeList(hearingOrdersBundlesDraftReview).stream())
            .filter(hearingOrdersBundleElement
                -> hearingOrdersBundleElement.getValue().getOrders().stream()
                .anyMatch(orderElement -> orderElement.getId().equals(orderId)))
            .findFirst();
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getAllHearings() {
        return Stream.of(defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()),
            defaultIfNull(cancelledHearingDetails, new ArrayList<Element<HearingBooking>>()))
            .flatMap(Collection::stream).collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getAllNonCancelledHearings() {
        return Stream.of(defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()))
            .flatMap(Collection::stream).collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getPastHearings() {
        return defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> !hearingBooking.getValue().startsAfterToday())
            .collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getPastAndTodayHearings() {
        return defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> hearingBooking.getValue().startsTodayOrBefore())
            .collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getFutureHearings() {
        return defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> hearingBooking.getValue().startsAfterToday())
            .collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getFutureAndTodayHearings() {
        return defaultIfNull(hearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> hearingBooking.getValue().startsTodayOrAfter())
            .collect(toList());
    }

    @JsonIgnore
    public List<Element<HearingBooking>> getToBeReListedHearings() {
        return defaultIfNull(cancelledHearingDetails, new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearingBooking -> hearingBooking.getValue().isToBeReListed())
            .collect(toList());
    }

    @CCD(
            label = "Do one or more orders need priority approval from the judge?",
            searchable = false,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class, BARRISTERCrudAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    private DraftOrderUrgencyOption draftOrderUrgency;
    @CCD(
            label = "Select the CMO you want to review first",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRPlus2RolesQqgmyhAccess.class}
    )
    private final Object cmoToReviewList;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRPlus2RolesQqgmyhAccess.class}
    )
    private final ReviewDecision reviewCMODecision;
    @CCD(
            label = "Number of draft CMOs ready for approval",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRPlus2RolesQqgmyhAccess.class}
    )
    private final String numDraftCMOs;
    @CCD(
            label = "Sealed Case Management Order",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class, CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCuAccess.class, CaseworkerPubliclawCafcassCaseworkerPubliclawCourtadminUAccess.class, CaseworkerPubliclawSystemupdateCudAccess.class}
    )
    private final List<Element<HearingOrder>> sealedCMOs;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCuAccess.class, CaseworkerPubliclawSuperuserCruPlus1RolesYwyfdvAccess.class}
    )
    private final List<Element<HearingOrder>> ordersToBeSent;
    @CCD(
            label = "Do any of these orders need to be reviewed urgently? If no urgency in any order, select continue only.",
            searchable = false,
            access = {CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class, CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCrudAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    private ApproveOrderUrgencyOption orderReviewUrgency;

    @JsonUnwrapped
    @Builder.Default
    private final ReviewDraftOrdersData reviewDraftOrdersData = ReviewDraftOrdersData.builder().build();

    public List<Element<HearingOrder>> getSealedCMOs() {
        return defaultIfNull(sealedCMOs, new ArrayList<>());
    }

    @JsonIgnore
    public Optional<HearingBooking> getNextHearingAfterCmo(UUID cmoID) {
        LocalDateTime currentCmoStartDate = unwrapElements(getAllHearings()).stream()
            .filter(hearingBooking -> cmoID.equals(hearingBooking.getCaseManagementOrderId()))
            .map(HearingBooking::getStartDate)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Failed to find hearing matching cmo id " + cmoID));

        return unwrapElements(hearingDetails).stream()
            .filter(hearingBooking -> hearingBooking.getStartDate().isAfter(currentCmoStartDate))
            .min(comparing(HearingBooking::getStartDate));
    }


    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, LABARRISTERSOLICITORRAccess.class, CaseworkerPubliclawCafcassRAccess.class}
    )
    private String sendToCtsc;
    @CCD(
            label = " ",
            searchable = false,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, LABARRISTERCrudPlus2RolesThgnehAccess.class, BARRISTERCAFCASSSOLICITORCaseworkerPubliclawCafcassCrudAccess.class}
    )
    private String displayAmountToPay;
    @CCD(
            label = "Change the state",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawSuperuserCruAccess.class}
    )
    private final String confirmChangeState;

    public DynamicList buildDynamicHearingList() {
        return buildDynamicHearingList(null);
    }

    public DynamicList buildDynamicHearingList(UUID selected) {
        return asDynamicList(getHearingDetails(), selected, HearingBooking::toLabel);
    }

    public DynamicList buildDynamicHearingList(List<Element<HearingBooking>> hearingDetails, UUID selected) {
        return asDynamicList(hearingDetails, selected, HearingBooking::toLabel);
    }

    @CCD(
            label = "Type of hearing",
            hint = "NOTE: For applications and any interim directions select \"Further Case Management\"",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final HearingType hearingType;
    @CCD(
            label = "Hearing type description",
            hint = "Provide more detail on hearing type, if required",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String hearingTypeDetails;
    @CCD(
            label = "Give reason",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String hearingTypeReason;
    @CCD(
            label = "Court",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HearingVenue",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String hearingVenue;
    @CCD(
            label = "Court address",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final Address hearingVenueCustom;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String firstHearingFlag; //also used for logic surrounding legacy hearings
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String hasPreviousHearingVenue;
    @CCD(
            label = "Last court",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final PreviousHearingVenue previousHearingVenue;
    @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
    private String previousVenueId;
    @CCD(
            label = "Additional notes",
            hint = "This will be printed on the notice of hearing, if issued",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String noticeOfHearingNotes;
    @CCD(
            label = "Which hearing do you want to edit?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final Object pastHearingDateList;
    @CCD(
            label = "Which hearing?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final Object pastAndTodayHearingDateList;
    @CCD(
            label = "Which hearing?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final Object futureHearingDateList;
    @CCD(
            label = "Which hearing?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final Object vacateHearingDateList;
    @CCD(
            label = "Which hearing?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final Object toReListHearingDateList;
    @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
    private final String hasExistingHearings;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final UUID selectedHearingId;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final UUID cancelledHearingId;
    @CCD(
            label = "Hearing attendance",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final List<HearingAttendance> hearingAttendance;
    @CCD(
            label = "Add details",
            hint = "For example, video link or telephone number",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String hearingAttendanceDetails;
    @CCD(
            label = "Pre-hearing attendance",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String preHearingAttendanceDetails;

    @Builder.Default
    @JsonUnwrapped
    private final ManageHearingHousekeepEventData manageHearingHousekeepEventData =
        ManageHearingHousekeepEventData.builder().build();

    @CCD(
            label = "Start date and time",
            hint = "Use 24 hour format",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    @TimeNotMidnight(message = "Enter a valid start time", groups = HearingDatesGroup.class)
    @Future(message = "Enter a start date in the future", groups = HearingDatesGroup.class)
    private final LocalDateTime hearingStartDate;

    @CCD(
            label = "End date and time",
            hint = "Use 24 hour format",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    @HasTimeNotMidnight(message = "Enter a valid end time", groups = HearingDatesGroup.class)
    @HasFutureEndDate(message = "Enter an end date in the future", groups = HearingDatesGroup.class)
    private final LocalDateTime hearingEndDateTime;
    @CCD(
            label = "End date and time",
            hint = "Use 24 hour format",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final LocalDateTime hearingEndDate;
    @CCD(
            label = "Hearing length, in days",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final Integer hearingDays;
    @CCD(
            label = "Hearing length, in minutes",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final Integer hearingMinutes;
    @CCD(
            label = "Hearing length, in hours",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final Integer hearingHours;
    @CCD(
            label = "Hearing duration",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "HearingDuration",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String hearingDuration;
    @CCD(
            label = "Do you want to send a notice of hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final String sendNoticeOfHearing;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final LanguageTranslationRequirement sendNoticeOfHearingTranslationRequirements;
    @CCD(
            label = "What would you like to do?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    private final HearingOptions hearingOption;
    @CCD(
            label = "Will the hearing be re-listed?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final HearingReListOption hearingReListOption;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
    private final HearingCancellationReason adjournmentReason;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
    private final HearingCancellationReason vacatedReason;
    @CCD(
            label = "When was the hearing vacated?",
            hint = "This is the date the hearing was vacated by the listings team. This date must be before the hearing date.",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final LocalDate vacatedHearingDate;
    @CCD(
            label = "What type of proceeding would you like to create?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ProceedingType",
            access = {CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final List<ProceedingType> proceedingType;
    @CCD(label = "Change status", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
    private final State closedStateRadioList;

    @CCD(
            label = "End date and time",
            hint = "Use 24 hour format",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final LocalDateTime hearingEndDateConfirmation;
    @CCD(
            label = "Start date and time",
            hint = "Use 24 hour format",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private final LocalDateTime hearingStartDateConfirmation;

    @JsonIgnore
    public boolean isHearingDateInPast() {
        return hearingEndDate.isBefore(LocalDateTime.now()) || hearingStartDate.isBefore(LocalDateTime.now());
    }

    // It will be used in "upload-documents" event (when the case is in Open state)
    @CCD(
            label = "Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "TemporaryApplicationDocuments",
            access = {BARRISTERCudPlus14RolesZohtrwAccess.class}
    )
    private final List<Element<ApplicationDocument>> temporaryApplicationDocuments;
    @CCD(
            label = "Documents to follow",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawSolicitorCruPlus1RolesOjgtycAccess.class, BARRISTERCuAccess.class, LABARRISTERCruAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    private final String applicationDocumentsToFollowReason;

    @JsonUnwrapped
    @Builder.Default
    private final MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder().build();
    @CCD(
            label = "Message",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class, CaseworkerPubliclawSystemupdateCuAccess.class}
    )
    private final List<Element<JudicialMessage>> judicialMessages;
    @CCD(label = "Message", searchable = false, access = {CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class})
    private final List<Element<JudicialMessage>> closedJudicialMessages;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "JudicialMessageRoleTypes",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    private JudicialMessageRoleType latestRoleSent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    private WorkAllocationTaskUrgency waTaskUrgencyLevel;


    public DynamicList buildJudicialMessageDynamicList(UUID selected) {
        return asDynamicList(judicialMessages, selected, JudicialMessage::toLabel);
    }

    public DynamicList buildJudicialMessageDynamicList() {
        return buildJudicialMessageDynamicList(null);
    }

    public List<Element<JudicialMessage>> getJudicialMessages() {
        return defaultIfNull(judicialMessages, new ArrayList<>());
    }

    @JsonUnwrapped
    @Builder.Default
    private final SyntheticCaseSummary syntheticCaseSummary = SyntheticCaseSummary.builder().build();

    @JsonUnwrapped
    @Builder.Default
    private final ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder().build();

    @JsonUnwrapped
    @Builder.Default
    private final UploadTranslationsEventData uploadTranslationsEventData = UploadTranslationsEventData.builder()
        .build();

    @JsonUnwrapped
    @Builder.Default
    private final LocalAuthorityEventData localAuthorityEventData = LocalAuthorityEventData.builder().build();

    public boolean hasSelectedTemporaryJudge(JudgeAndLegalAdvisor judge) {
        return judge.getJudgeTitle() != null;
    }

    @JsonUnwrapped
    @Builder.Default
    private final NoticeOfChangeAnswersData noticeOfChangeAnswersData = NoticeOfChangeAnswersData.builder().build();
    @JsonUnwrapped
    @Builder.Default
    private final NoticeOfChangeChildAnswersData noticeOfChangeChildAnswersData =
        NoticeOfChangeChildAnswersData.builder()
            .build();

    @JsonUnwrapped
    @Builder.Default
    private final RespondentPolicyData respondentPolicyData = RespondentPolicyData.builder().build();
    @JsonUnwrapped
    @Builder.Default
    private final ChildPolicyData childPolicyData = ChildPolicyData.builder().build();

    @JsonUnwrapped
    @Builder.Default
    private final GatekeepingOrderEventData gatekeepingOrderEventData = GatekeepingOrderEventData.builder().build();

    @CCD(
            label = "Change of representative",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerApproverCruAccess.class}
    )
    private final List<Element<ChangeOfRepresentation>> changeOfRepresentatives;
    @CCD(label = "Change Organisation Request", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class})
    private final ChangeOrganisationRequest changeOrganisationRequestField;

    @JsonUnwrapped
    @Builder.Default
    private final ManageLegalCounselEventData manageLegalCounselEventData =
        ManageLegalCounselEventData.builder().build();

    @JsonIgnore
    public boolean isOutsourced() {
        return Optional.ofNullable(outsourcingPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .filter(StringUtils::isNotEmpty)
            .isPresent();
    }

    public List<Element<LocalAuthority>> getLocalAuthorities() {
        if (isNull(localAuthorities)) {
            localAuthorities = new ArrayList<>();
        }
        return localAuthorities;
    }

    @CCD(
            label = "Select court",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class}
    )
    private final DynamicList courtsList;

    @JsonIgnore
    public LocalAuthority getDesignatedLocalAuthority() {

        if (isEmpty(getLocalAuthorities())) {
            return null;
        }

        return getLocalAuthorities().stream()
            .map(Element::getValue)
            .filter(la -> YesNo.YES.getValue().equals(la.getDesignated()))
            .findFirst()
            .orElse(null);
    }

    @JsonIgnore
    public Optional<LocalAuthority> getSecondaryLocalAuthority() {

        if (isEmpty(getLocalAuthorities())) {
            return Optional.empty();
        }

        return getLocalAuthorities().stream()
            .map(Element::getValue)
            .filter(la -> !YesNo.YES.getValue().equals(la.getDesignated()))
            .findFirst();
    }

    @JsonUnwrapped
    @Builder.Default
    private final LocalAuthoritiesEventData localAuthoritiesEventData = LocalAuthoritiesEventData.builder().build();

    @JsonUnwrapped
    @Builder.Default
    private final CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData
        .builder().build();

    @JsonUnwrapped
    @Builder.Default
    private final PlacementEventData placementEventData = PlacementEventData.builder().build();

    @CCD(
            label = "Which placement application do you wish to issue a notice of placement - hearing for?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final DynamicList placementList;

    @CCD(
            label = "Notice of placement response",
            searchable = false,
            access = {BARRISTERCruPlus18RolesCoxqdqAccess.class}
    )
    private List<Element<PlacementNoticeDocument>> placementNoticeResponses;

    @JsonIgnore
    public boolean isDischargeOfCareApplication() {

        return ofNullable(getOrders())
            .map(Orders::isDischargeOfCareOrder)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isContactWithChildInCareApplication() {

        return ofNullable(getOrders())
            .map(Orders::isContactWithChildInCareOrder)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isC1Application() {
        return ofNullable(getOrders())
            .map(Orders::isC1Order)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isSecureAccommodationOrderType() {
        return ofNullable(getOrders())
            .map(Orders::isSecureAccommodationOrder)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isChildRecoveryOrder() {
        return ofNullable(getOrders())
            .map(Orders::isChildRecoveryOrder)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isChildAssessmentOrder() {
        return ofNullable(getOrders())
            .map(Orders::isChildAssessmentOrder)
            .orElse(false);
    }

    @JsonUnwrapped
    @Builder.Default
    private final OtherToRespondentEventData otherToRespondentEventData = OtherToRespondentEventData.builder().build();

    @CCD(
            label = "Colleagues for ${respondentName}'s solicitor",
            searchable = false,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class}
    )
    private List<Element<Colleague>> colleaguesToNotify;

    public List<Element<Colleague>> getColleaguesToNotify() {
        return colleaguesToNotify != null ? colleaguesToNotify : new ArrayList<>();
    }

    @JsonIgnore
    public boolean isRefuseContactWithChildApplication() {
        return ofNullable(getOrders())
            .map(Orders::isRefuseContactWithChildApplication)
            .orElse(false);
    }

    @JsonUnwrapped
    @Builder.Default
    protected final ConfirmApplicationReviewedEventData confirmApplicationReviewedEventData =
        ConfirmApplicationReviewedEventData.builder().build();

    @JsonIgnore
    public boolean isEducationSupervisionApplication() {
        return ofNullable(getOrders())
            .map(Orders::isEducationSupervisionOrder)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isCareOrderCombinedWithUrgentDirections() {
        return ofNullable(getOrders())
            .map(Orders::isCareOrderCombinedWithEPOorICO)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isStandaloneEPOApplication() {
        return ofNullable(getOrders())
            .map(Orders::isEmergencyProtectionOrderOnly)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isStandaloneInterimCareOrder() {
        return ofNullable(getOrders())
            .map(Orders::isInterimCareOrderOnly)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isStandaloneSecureAccommodationOrder() {
        return ofNullable(getOrders())
            .map(Orders::isSecureAccommodationOrderOnly)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isStandaloneChildRecoveryOrder() {
        return ofNullable(getOrders())
            .map(Orders::isChildRecoveryOrderOnly)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isEPOCombinedWithICO() {
        return ofNullable(getOrders())
            .map(Orders::isEPOCombinedWithICO)
            .orElse(false);
    }

    @JsonIgnore
    public String getCaseLaOrRelatingLa() {
        return isEmpty(caseLocalAuthority) ? relatingLA : caseLocalAuthority;
    }

    @JsonIgnore
    public Optional<String> getApplicantName() {
        if (!isEmpty(getLocalAuthorities())) {
            return Optional.of(getLocalAuthorities().get(0).getValue().getName());
        } else {
            return Optional.empty();
        }
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Number of past hearings without a cmo", searchable = false)
  private String numHearingsWithoutCMO;
  @CCD(
          label = "You cannot upload an agreed CMO.\n\nThis is because the judge is considering, or has sealed, the CMOs from all past hearings.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String noHearingsForCMOs;
  @CCD(label = "Hearings with CMOs already being reviewed", searchable = false, typeOverride = FieldType.TextArea)
  private String multiHearingsWithCMOs;
  @CCD(label = "Hearings with CMOs already being reviewed", searchable = false, typeOverride = FieldType.TextArea)
  private String singleHearingWithCMO;
  @CCD(label = "Field to store conditional result for showing multiHearingsWithCMOs", searchable = false)
  private String showHearingsMultiTextArea;
  @CCD(label = "Field to store conditional result for showing singleHearingWithCMO", searchable = false)
  private String showHearingsSingleTextArea;
  @CCD(label = "Respondent statements", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<RespondentStatement>> respondentStatements;
  @CCD(
          label = "Which hearing is this order for?",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, LABARRISTERRAccess.class}
  )
  private String cmoHearingDateList;
  @CCD(
          label = "Case management order",
          searchable = false,
          access = {CaseworkerPubliclawSolicitorCudAccess.class, CaseworkerPubliclawSystemupdateCdAccess.class}
  )
  private CaseManagementOrder caseManagementOrder;
  @CCD(
          label = "## For all parties",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawSolicitorRAccess.class}
  )
  private String allPartiesLabelCMO;
  @CCD(
          label = "## For the local authority",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawSolicitorRAccess.class}
  )
  private String localAuthorityDirectionsLabelCMO;
  @CCD(
          label = "## For Cafcass",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawSolicitorRAccess.class}
  )
  private String cafcassDirectionsLabelCMO;
  @CCD(
          label = "## For the court",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawSolicitorRAccess.class}
  )
  private String courtDirectionsLabelCMO;
  @CCD(
          label = "Schedule",
          searchable = false,
          access = {DefaultAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, LABARRISTERCaseworkerPubliclawMagistrateRAccess.class}
  )
  private Schedule schedule;
  @CCD(
          label = "## Basis of order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRAccess.class}
  )
  private String orderBasisLabel;
  @CCD(
          label = "### Add recital",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, LABARRISTERRAccess.class}
  )
  private String addRecitalLabel;
  @CCD(
          label = "Recitals",
          searchable = false,
          access = {CaseworkerPubliclawGatekeeperCrudPlus2RolesGjbeqhAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Recitals>> recitals;
  @CCD(
          label = "## For the parents or respondents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSolicitorRAccess.class}
  )
  private String respondentsDirectionLabelCMO;
  @CCD(
          label = "## For other parties",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCrudAccess.class, CaseworkerPubliclawMagistrateRAccess.class, CaseworkerPubliclawSolicitorRAccess.class}
  )
  private String otherPartiesDirectionLabelCMO;
  @CCD(
          label = "Draft case management order",
          searchable = false,
          access = {CaseworkerPubliclawSystemupdateCdAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document sharedDraftCMODocument;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRAccess.class})
  private OrderAction orderAction;
  @CCD(label = "### Is this ready to be sent to parties?", searchable = false, typeOverride = FieldType.Label)
  private String cmoActionTypeLabel;
  @CCD(
          label = "Case management order",
          searchable = false,
          access = {CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCuAccess.class, CaseworkerPubliclawSystemupdateCdAccess.class}
  )
  private CaseManagementOrder cmoToAction;
  @CCD(label = "Case management orders", searchable = false, access = {CaseworkerPubliclawSystemupdateCdAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<CaseManagementOrder>> servedCaseManagementOrders;
  @CCD(
          label = "## Check date and venue details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class}
  )
  private String nextHearingDateHeading;
  @CCD(
          label = "Check the 'Hearings' tab to confirm date and venue details.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class}
  )
  private String nextHearingDateHintText;
  @CCD(
          label = "Which hearing is next?",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRAccess.class, CaseworkerPubliclawSystemupdateDAccess.class}
  )
  private String nextHearingDateList;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRAccess.class})
  private NextHearing nextHearing;
  @CCD(
          label = "## You cannot edit this order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class}
  )
  private String actionCMOPlaceholderHeading;
  @CCD(
          label = "You can only review the draft order after it has been submitted",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class}
  )
  private String actionCMOPlaceholderHint;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawSolicitorCruAccess.class}
  )
  private String cmoEventId;
  @CCD(
          label = "## You can no longer edit this order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRAccess.class}
  )
  private String cmoDraftInReviewHeading;
  @CCD(
          label = "The judge is reviewing this order. You can only edit it if the judge asks for changes.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRAccess.class}
  )
  private String cmoDraftInReviewHint;
  @CCD(
          label = "Add completed directions from the precedent library or your own template.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawSolicitorRAccess.class}
  )
  private String allPartiesPrecedentLabelCMO;
  @CCD(
          label = "Add directions",
          searchable = false,
          access = {CaseworkerPubliclawGatekeeperCrudPlus2RolesGjbeqhAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Direction>> allPartiesCustomCMO;
  @CCD(
          label = "Add directions",
          searchable = false,
          access = {CaseworkerPubliclawGatekeeperCrudPlus2RolesGjbeqhAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Direction>> localAuthorityDirectionsCustomCMO;
  @CCD(
          label = "Add directions",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSolicitorCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Direction>> cafcassDirectionsCustomCMO;
  @CCD(
          label = "Add directions",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSolicitorCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Direction>> respondentDirectionsCustomCMO;
  @CCD(
          label = "Add directions",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSolicitorCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Direction>> otherPartiesDirectionsCustomCMO;
  @CCD(
          label = "Add directions",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSolicitorCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Direction>> courtDirectionsCustomCMO;
  @CCD(
          label = "# Check you're registered for an account\n\nAfter 14 September 2020, only registered users can create cases on this system.\n\nAsk your team administrator to [check if you're registered](https://manage-org.platform.hmcts.net/organisation).",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String migrateToMoWarning;
  @CCD(label = "Further evidence document", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> furtherEvidenceDocumentsTEMP;
  @CCD(
          label = "In person or remote via video",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private HearingPresence hearingPresence;
  @CCD(
          label = "[Make changes to local authority details](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterApplicant)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class}
  )
  private String organisationDetailsLink;
  @CCD(label = "Child", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Placement>> placementsWithoutPlacementOrder;
  @CCD(
          label = "End date and time",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String hearingEndDateLabel;
  @CCD(label = "Court bundle", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<CourtBundleV2>> courtBundleList;
  @CCD(label = "There are no future hearings to edit", searchable = false, typeOverride = FieldType.Label)
  private String noHearingsToEditLabel;
  @CCD(label = "There are no hearings to associate a bundle with", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentsCourtBundleNoHearingsLabel;
  @CCD(label = "Which hearing?", searchable = false, typeOverride = FieldType.DynamicList)
  private String courtBundleHearingList;
  @CCD(label = "## Court bundle", searchable = false, typeOverride = FieldType.Label)
  private String courtBundleTabHeading;
  @CCD(label = " ", searchable = false)
  private String reviewCMOShowOthers;
  @CCD(label = "Case summary or supporting documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> cmoSupportingDocs;
  @CCD(label = "Are you a solicitor creating a case on behalf of a local authority?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isOutsourcingLA;
  @CCD(
          label = "${gatekeepingOrderSealDecision.nextSteps}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String nextStepsLabel;
  @CCD(label = "Position statement child", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<PositionStatementChild>> positionStatementChildList;
  @CCD(label = "Position statement respondent", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<PositionStatementRespondent>> positionStatementRespondentList;
  @CCD(label = "Position statement child", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<PositionStatementChild>> positionStatementChildListV2;
  @CCD(label = "Position statement respondent", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<PositionStatementRespondent>> positionStatementRespondentListV2;
  @CCD(label = "Correspondence uploaded by HMCTS", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> correspondenceDocuments;
  @CCD(label = "Correspondence uploaded by HMCTS", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> correspondenceDocumentsNC;
  @CCD(label = "Correspondence uploaded by local authority", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> correspondenceDocumentsLA;
  @CCD(label = "Correspondence uploaded by local authority", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> correspondenceDocumentsLANC;
  @CCD(label = "Correspondence uploaded by solicitor", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> correspondenceDocumentsSolicitor;
  @CCD(label = "HMCTS further evidence documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> furtherEvidenceDocuments;
  @CCD(label = "HMCTS further evidence documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> furtherEvidenceDocumentsNC;
  @CCD(label = "Local authority further evidence documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> furtherEvidenceDocumentsLA;
  @CCD(label = "Local authority further evidence documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> furtherEvidenceDocumentsLANC;
  @CCD(label = "Solicitor further evidence documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> furtherEvidenceDocumentsSolicitor;
  @CCD(label = "Documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Documents>> applicationDocuments;
  @CCD(
          label = "[Manage documents](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/manageDocumentsSolicitor)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String manageDocumentsSolicitorLink;
  @CCD(label = "Document with confidential address", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<DocumentWithConfidentialAddress>> documentsWithConfidentialAddress;
  @CCD(
          label = "[Manage documents](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/manageDocuments)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String manageDocumentsLink;
  @CCD(
          label = "[Manage documents](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/manageDocumentsLA)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String manageDocumentsLALink;
  @CCD(label = "Case name", searchable = false)
  private String documentViewHMCTS;
  @CCD(label = "Case name", searchable = false)
  private String documentViewLA;
  @CCD(label = "Case name", searchable = false)
  private String documentViewNC;
  @CCD(label = "${documentViewHMCTS}", searchable = false, typeOverride = FieldType.Label)
  private String documentListPageHMCTS;
  @CCD(label = "${documentViewLA}", searchable = false, typeOverride = FieldType.Label)
  private String documentListPageLA;
  @CCD(label = "${documentViewNC}", searchable = false, typeOverride = FieldType.Label)
  private String documentListPageNC;
  @CCD(label = " ", searchable = false)
  private String showFurtherEvidenceTab;
  @CCD(label = "## Correspondence", searchable = false, typeOverride = FieldType.Label)
  private String correspondenceTabHeading;
  @CCD(label = "Court bundle", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<HearingCourtBundle>> courtBundleListV2Backup;
  @CCD(label = "Case summary", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<CaseSummary>> caseSummaryListBackup;
  @CCD(label = "Skeleton argument", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SkeletonArgument>> skeletonArgumentListBackup;
  @CCD(label = "Has been migrated in the CFV migration")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasBeenCFVMigrated;
  @CCD(
          label = "8. Court bundle",
          searchable = false,
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, LABARRISTERSOLICITORRAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private CourtBundle courtBundle;
  @CCD(label = "## Hearing documents", searchable = false, typeOverride = FieldType.Label)
  private String hearingDocumentsTabHeading;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
  private String people_label;
  @CCD(label = "## Further evidence documents", searchable = false, typeOverride = FieldType.Label)
  private String documentTabFurtherEvidenceHeading;
  @CCD(label = "## Application documents", searchable = false, typeOverride = FieldType.Label)
  private String documentTabApplicationHeading;
  @CCD(label = "## Bulk scan documents", searchable = false, typeOverride = FieldType.Label)
  private String documentTabBulkScanHeading;
  @CCD(label = "Further evidence documents for hearings", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments;
  @CCD(
          label = "Nominate a case contact",
          hint = "HMCTS will contact this person if they have any questions",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {CaseworkerPubliclawSolicitorCrudAccess.class}
  )
  private String localAuthorityColleaguesList;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawSolicitorCrudAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo localAuthorityMainContactShown;
  @CCD(label = "Colleague", searchable = false, access = {CaseworkerPubliclawSolicitorCrudAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Colleague>> localAuthorityColleagues;
  @CCD(
          label = "<h2>Main contact</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSolicitorRAccess.class}
  )
  private String mainContactLabel;
  @CCD(
          label = "[Make changes to factors affecting parenting](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterParentingFactors)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, SOLICITORRAccess.class}
  )
  private String factorsAffectingParentingLink;
  @CCD(
          label = "Add legal adviser details",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo enterManually;
  @CCD(
          label = "Allocated Judge",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private JudgeAndLegalAdvisor tempAllocatedJudge;
  @CCD(
          label = "Search for Judge",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String judicialUserHearingJudge;
  @CCD(
          label = "Hearing Judge",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private JudgeAndLegalAdvisor hearingJudge;
  @CCD(
          label = "Add legal adviser details",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo enterManuallyHearingJudge;
  @CCD(label = " ", searchable = false)
  private ManageDocumentType manageDocument;
  @CCD(label = "Which hearing?", searchable = false, typeOverride = FieldType.DynamicList)
  private String manageDocumentsHearingList;
  @CCD(label = "Select application:", searchable = false, typeOverride = FieldType.DynamicList)
  private String manageDocumentsSupportingC2List;
  @CCD(label = "C2 supporting document", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> c2SupportingDocuments;
  @CCD(label = "Supporting documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> supportingEvidenceDocumentsTemp;
  @CCD(label = "Upload other documents relating to:", searchable = false)
  private String manageDocumentsHearingLabel;
  @CCD(label = "Application:", searchable = false)
  private String manageDocumentsSupportingC2Label;
  @CCD(label = "## Other documents", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentsFurtherEvidenceHeading;
  @CCD(label = "## Correspondence", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentsCorrespondenceHeading;
  @CCD(label = "## Further order application documents", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentsC2Heading;
  @CCD(label = "## Respondent statement", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentsRespondentStatementsHeading;
  @CCD(label = "Does the document relate to a hearing?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo manageDocumentsRelatedToHearing;
  @CCD(label = "Choose a document type", searchable = false)
  private ManageDocumentSubtypeList manageDocumentSubtypeList;
  @CCD(label = "Select respondent", searchable = false, typeOverride = FieldType.DynamicList)
  private String respondentStatementList;
  @CCD(
          label = "There are no hearings to associate a hearing document with",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String manageDocumentsHearingDocumentNoHearingsLabel;
  @CCD(label = "Court bundle for", searchable = false)
  private String manageDocumentsCourtBundleHearingLabel;
  @CCD(label = "Documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<CourtBundleV2>> manageDocumentsCourtBundle;
  @CCD(label = " ", searchable = false)
  private CaseSummary manageDocumentsCaseSummary;
  @CCD(label = " ", searchable = false)
  private PositionStatementChild manageDocumentsPositionStatementChild;
  @CCD(label = " ", searchable = false)
  private SkeletonArgument manageDocumentsSkeletonArgument;
  @CCD(label = "Which party?", searchable = false, typeOverride = FieldType.DynamicList)
  private String hearingDocumentsPartyList;
  @CCD(label = "Which child?", searchable = false, typeOverride = FieldType.DynamicList)
  private String manageDocumentsChildrenList;
  @CCD(label = " ", searchable = false)
  private PositionStatementRespondent manageDocumentsPositionStatementRespondent;
  @CCD(label = "Which respondent?", searchable = false, typeOverride = FieldType.DynamicList)
  private String hearingDocumentsRespondentList;
  @CCD(label = "Which hearing?", searchable = false, typeOverride = FieldType.DynamicList)
  private String hearingDocumentsHearingList;
  @CCD(label = "Which hearing documents?", searchable = false)
  private HearingDocumentType manageDocumentsHearingDocumentType;
  @CCD(label = " ", searchable = false)
  private ManageDocumentTypeLA manageDocumentLA;
  @CCD(
          label = "There are no additional applications to add documents to",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String manageDocumentsNoC2sLabel;
  @CCD(
          label = "There are no issued notice of hearing for placements to add responses for",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String manageDocumentsNoPlacementNoticesLabel;
  @CCD(label = "Which Placement Application?", searchable = false, typeOverride = FieldType.DynamicList)
  private String manageDocumentsPlacementList;
  @CCD(label = "Choose a further evidence document type", searchable = false)
  private ManageDocumentSubtypeListLA manageDocumentSubtypeListLA;
  @CCD(label = "## Draft gatekeeping order", searchable = false, typeOverride = FieldType.Label)
  private String draftSDOLabel;
  @CCD(
          label = "## Urgent hearing order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String urgentHearingOrderLabel;
  @CCD(
          label = "If your case is urgent, you can upload an urgent hearing order and add the gatekeeping order later, if you still need to.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryRAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String sdoRouteInfo;
  @CCD(
          label = "### Next steps\n\nBefore the next hearing you must add the judge or magistrate allocated to this case in 'Allocated judge'.\n\nYou can also add a gatekeeping order later, if needed.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String urgentHearingOrderInfo;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo showNoticeOfProceedings;
  @CCD(
          label = "Use this page to alert court staff to a potentially violent person expected in hearings.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String addACaseFlagLabel;
  @CCD(label = "Only HMCTS staff can see this information.", searchable = false, typeOverride = FieldType.Label)
  private String onlyHMCTSStaffLabel;
  @CCD(label = "Respondent Name", searchable = false, access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class})
  private String respondentName;
  @CCD(
          label = "<br/>Use this feature to add or remove a legal representative who'll be taking this case for you.<br/><br/>Legal representatives must be registered to use the service before you can add them to a case.<br/><br/>After they're registered, they'll only have access to cases you add them to. They will not be able to see all your organisation's cases.<br/><br/>They can register at [https://manage-org.platform.hmcts.net/register-org/register](https://manage-org.platform.hmcts.net/register-org/register)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, BARRISTERSOLICITORRAccess.class}
  )
  private String addOrRemoveLegalCounselLabel;
  @CCD(label = "Has been migrated in the AM migration", access = {CaseworkerPubliclawSystemupdateCrudAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasBeenAMMigrated;
  @CCD(
          label = "Search for Judge",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String hearingJudicialUser;
  @CCD(
          label = "You are entering legal adviser details manually - this will allocate the case roles but any generic inbox email address detailed will create tasks to Available Tasks and not My Tasks.",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String manualJudgeAllocationWarning;
  @CCD(
          label = "You are entering legal adviser details manually - this will allocate the case roles but any generic inbox email address detailed will create tasks to Available Tasks and not My Tasks.",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String manualJudgeAllocationWarningHearingJudge;
  @CCD(
          label = "What type of judge do you want to allocate:",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private JudgeType hearingJudgeType;
  @CCD(
          label = "Select judge title",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private FeePaidJudgeTitle hearingFeePaidJudgeTitle;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private ManualLegalAdvisorDetail hearingManualJudgeDetails;
  @CCD(label = "Case name", searchable = false, access = {CaseworkerPubliclawSystemupdateCudAccess.class})
  private String taskList;
  @CCD(
          label = "${taskList}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private String taskListLabel;
  @CCD(label = "Is this case outsourced?", searchable = false, access = {CaseworkerPubliclawSolicitorCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isOutsourcedCase;
  @CCD(label = "-", searchable = false, access = {CaseworkerPubliclawSolicitorCrudAccess.class})
  private String sharingWithUsers;
  @CCD(
          label = "These users will gain access to the case when it is created: <br/> ${sharingWithUsers} <br/> If this does not look right, select No and share the case manually when created.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSolicitorCrudAccess.class}
  )
  private String shareCaseWarningMessage;
  @CCD(
          label = "This case will NOT be shared with anyone else. You can still share the case manually with others when it has been created.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSolicitorCrudAccess.class}
  )
  private String shareCaseWarningMessageNotShared;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class})
  private String caseHistory;
  @CCD(
          label = "EPO Reason show or hide",
          searchable = false,
          access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERRSOLICITORCruAccess.class}
  )
  private java.util.Set<ShowHide> EPO_REASONING_SHOW;
  @CCD(
          label = "You must upload these documents if possible. Give the reason and date you expect to provide it if you don't have a document yet.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRAccess.class, CaseworkerPubliclawCafcassRAccess.class, CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String uploadDocuments_paragraph_1;
  @CCD(
          label = "-------------------------------------------------------------------------------------------------------------",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CAFCASSSOLICITORLABARRISTERCaseworkerPubliclawCafcassRAccess.class}
  )
  private String documents_socialWorkOther_border_top;
  @CCD(
          label = "-------------------------------------------------------------------------------------------------------------",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CAFCASSSOLICITORLABARRISTERCaseworkerPubliclawCafcassRAccess.class}
  )
  private String documents_socialWorkOther_border_bottom;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CHILDSOLICITORACrudPlus3RolesApwidhAccess.class}
  )
  private String applicantContactLabel;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CHILDSOLICITORACrudPlus3RolesApwidhAccess.class}
  )
  private String applicantOtherContactLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, DefaultAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, LABARRISTERCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo submittedFormNeedTranslation;
  @CCD(
          label = "Sent for translation",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, EPSMANAGINGCruLABARRISTERRLAMANAGINGRLASHAREDRLASOLICITORRAccess.class}
  )
  private String submittedFormSentForTranslationLabel;
  @CCD(
          label = "Upload standard directions and other relevant documents, for example the C6 Notice of Proceedings or C9 statement of service.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String standardDirectionsLabel;
  @CCD(
          label = "## 1. Standard directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String standardDirectionsTitle;
  @CCD(
          label = "Upload a file",
          searchable = false,
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryRAccess.class, LABARRISTERSOLICITORRAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document standardDirectionsDocument;
  @CCD(
          label = "**Declaration** <br> <span class=\"text-16\">The applicant understands that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth.</span>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CHILDSOLICITORACruPlus3RolesQtjkvuAccess.class}
  )
  private String submissionDeclaration;
  @CCD(
          label = " ",
          searchable = false,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CHILDSOLICITORACruPlus3RolesQtjkvuAccess.class}
  )
  private String submissionConsentLabel;
  @CCD(
          label = "**Case name has been updated based on the answers you have given.** <br><span class=\"text-16\">The case will be submitted to the system with the name <strong>${caseName}</strong> <br>If there is an error in the case name such as misspelling, you can go back to the applicant and respondent section to change your answer. This will update the case name.</span>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CHILDSOLICITORASOLICITORACruAccess.class, LABARRISTERCruAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private String caseNameHintLabel;
  @CCD(
          label = "**Updated case name**<br> <span class=\"text-16\">Case name will be updated to: <strong>${caseName}</strong> <br> based on the current applicant and respondent's details.</span>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CHILDSOLICITORASOLICITORACruAccess.class, LABARRISTERCruAccess.class}
  )
  private String updatedCaseNameLabel;
  @CCD(
          label = "**Download application** <br>Use this link to download and check the application before <br>sending:",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String downloadApplicationForReviewHintLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, LASOLICITORCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document draftSupplement;
  @CCD(
          label = " ",
          searchable = false,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CHILDSOLICITORACruPlus3RolesQtjkvuAccess.class}
  )
  private java.util.Set<Consent> submissionConsent;
  @CCD(
          label = "### Let the gatekeeper know there's a new case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String gateKeeperLabel;
  @CCD(
          label = "You must add at least 1 gatekeeper",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String gatekeeperHintLabel;
  @CCD(
          label = "Gatekeeper's email address",
          hint = "For example, joe.bloggs@la.gov.uk",
          searchable = false,
          typeOverride = FieldType.Email
  )
  private String gateKeeperEmail;
  @CCD(label = "Date submitted", access = {CaseworkerPubliclawSystemupdateCudAccess.class})
  private java.time.LocalDateTime dateAndTimeSubmitted;
  @CCD(label = " ", searchable = false, access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class})
  private java.util.Set<DeletionConsent> deletionConsent;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private String proceedingLabel;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private String allocatedJudgeLabel;
  @CCD(
          label = "Hearing date",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private String allPartiesHearingDate;
  @CCD(
          label = "Hearing date",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private String localAuthorityDirectionsHearingDate;
  @CCD(
          label = "Hearing date",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private String respondentDirectionsHearingDate;
  @CCD(
          label = "Hearing date",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private String cafcassDirectionsHearingDate;
  @CCD(
          label = "Hearing date",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private String otherPartiesDirectionsHearingDate;
  @CCD(
          label = "Hearing date",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private String courtDirectionsHearingDate;
  @CCD(
          label = "If you send documents to a party's solicitor or a children's guardian, give their details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, LABARRISTERRAccess.class}
  )
  private String c9Declaration;
  @CCD(
          label = "Declaration",
          searchable = false,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, LABARRISTERRAccess.class}
  )
  private String serviceDeclarationLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, LABARRISTERRAccess.class}
  )
  private java.util.Set<Consent> serviceConsent;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, LABARRISTERRSOLICITORCruAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private String respondents_label;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, LABARRISTERRSOLICITORCruAccess.class, BARRISTERCruAccess.class}
  )
  private String others_label;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {EPSMANAGINGCruPlus4RolesFtwwcyAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementRespondentsLabel;
  @CCD(
          label = "Counsel/external solicitors must be registered to use the service before you can add them to a case.<br/><br/>After they're registered, they'll only have access to cases you add them to. They will not be able to see all your organisation's cases.<br/><br/>They will be able to upload:<ul><li>case documents</li><li>c2 applications and associated documents</li><li>pre- and post-hearing CMOs</li><li>placement</li></ul>They can register at [https://manage-org.platform.hmcts.net/register-org/register](https://manage-org.platform.hmcts.net/register-org/register)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class}
  )
  private String manageLegalRepresentativesLabel;
  @CCD(label = "Document to be sent", searchable = false, access = {CaseworkerPubliclawSystemupdateCrudAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document documentToBeSent;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private String pageShow;
  @CCD(
          label = "New hearing added",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String newHearing_label;
  @CCD(
          label = "Which child?",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, LABARRISTERCaseworkerPubliclawMagistrateRAccess.class, CaseworkerPubliclawSolicitorCruAccess.class}
  )
  private String childrenList;
  @CCD(
          label = "Single child",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLASHAREDCruAccess.class, LABARRISTERCaseworkerPubliclawMagistrateRAccess.class, LAMANAGINGCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo singleChild;
  @CCD(
          label = "Supplementary evidence handled",
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class, CaseworkerPubliclawBulkscanCruAccess.class, CaseworkerPubliclawBulkscansystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo evidenceHandled;
  @CCD(
          label = "Report",
          searchable = false,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<ExpertReport>> expertReport;
  @CCD(
          label = "## Date of issue",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private String dateOfIssue_label;
  @CCD(
          label = "A history of payments associated with a the case",
          searchable = false,
          typeOverride = FieldType.CasePaymentHistoryViewer,
          access = {CaseworkerPubliclawCourtadminCuAccess.class}
  )
  private String paymentHistory;
  @CCD(
          label = "Add comments",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCuAccess.class}
  )
  private String extensionComments;
  @CCD(
          label = "Case extension date",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class, CaseworkerPubliclawGatekeeperRAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private String extensionDateEightWeeks;
  @CCD(
          label = "You've select to extend the case by 8 weeks. The case should now be completed by ${extensionDateEightWeeks}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
  )
  private String shouldBeCompletedByConfirmationLabel;
  @CCD(
          label = "You can extend this date again, but you will not be able to revert back to the original date.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String caseFurtherExtensionLabel;
  @CCD(label = "--", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
  private String shouldBeCompletedByDate;
  @CCD(
          label = "## This case should be completed by ${shouldBeCompletedByDate}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class, CaseworkerPubliclawJudiciaryCrudAccess.class}
  )
  private String shouldBeCompletedByLabel;
  @CCD(
          label = "You can either extend this date by 8 weeks, or enter a different end date.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private String extendByEightWeeksOrOtherLabel;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class}
  )
  private String close_case_label;
  @CCD(
          label = "## Add decision details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String closeCaseDetailsHeadingLabel;
  @CCD(label = " ", searchable = false)
  private String childNameLabel;
  @CCD(label = " ", searchable = false)
  private ChildFinalDecisionDetails childFinalDecisionDetails;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class}
  )
  private String showCloseCaseFromOrderPage;
  @CCD(label = "## Application details", searchable = false, typeOverride = FieldType.Label)
  private String applicationDetailsHeadingLabel;
  @CCD(label = "## Grounds for the application", searchable = false, typeOverride = FieldType.Label)
  private String groundsForTheApplicationHeadingLabel;
  @CCD(label = "## Application documents", searchable = false, typeOverride = FieldType.Label)
  private String supportingDocumentsHeadingLabel;
  @CCD(label = "## Information about the parties", searchable = false, typeOverride = FieldType.Label)
  private String informationAboutThePartiesHeadingLabel;
  @CCD(label = "## Court requirements", searchable = false, typeOverride = FieldType.Label)
  private String courtRequirementsHeadingLabel;
  @CCD(label = "#### Orders and directions sought", searchable = false, typeOverride = FieldType.Label)
  private String ordersAndDirectionsSubHeadingLabel;
  @CCD(label = "#### Hearing urgency", searchable = false, typeOverride = FieldType.Label)
  private String hearingUrgencySubHeadingLabel;
  @CCD(label = "#### Threshold criteria", searchable = false, typeOverride = FieldType.Label)
  private String thresholdCriteriaSubHeadingLabel;
  @CCD(label = "#### Risk and harm to the children", searchable = false, typeOverride = FieldType.Label)
  private String riskAndHarmToTheChildrenSubHeadingLabel;
  @CCD(label = "#### Factors Affecting parenting", searchable = false, typeOverride = FieldType.Label)
  private String factorsAffectingParentingSubHeadingLabel;
  @CCD(label = "#### Additional information", searchable = false, typeOverride = FieldType.Label)
  private String additionalInformationGroundsSubHeadingLabel;
  @CCD(label = "#### Additional information", searchable = false, typeOverride = FieldType.Label)
  private String additionalInformationPartiesSubHeadingLabel;
  @CCD(label = "#### Additional information", searchable = false, typeOverride = FieldType.Label)
  private String additionalInformationCourtRequirementsSubHeadingLabel;
  @CCD(label = "#### Other proceedings", searchable = false, typeOverride = FieldType.Label)
  private String otherProceedingsSubHeadingLabel;
  @CCD(label = "#### International element", searchable = false, typeOverride = FieldType.Label)
  private String internationalElementSubHeadingLabel;
  @CCD(label = "#### Your organisation details", searchable = false, typeOverride = FieldType.Label)
  private String organisationDetailsSubHeadingLabel;
  @CCD(label = "#### The child's details", searchable = false, typeOverride = FieldType.Label)
  private String childDetailsSubHeadingLabel;
  @CCD(label = "#### Respondents' details", searchable = false, typeOverride = FieldType.Label)
  private String respondentsSubHeadingLabel;
  @CCD(label = "#### Other people in the case", searchable = false, typeOverride = FieldType.Label)
  private String othersSubHeadingLabel;
  @CCD(label = "#### Allocation proposal", searchable = false, typeOverride = FieldType.Label)
  private String allocationProposalSubHeadingLabel;
  @CCD(label = "#### Court services", searchable = false, typeOverride = FieldType.Label)
  private String facilitiesOrServiceNeededSubHeadingLabel;
  @CCD(label = " ", searchable = false)
  private String showManageOrgWarning;
  @CCD(label = "## Orders", searchable = false, typeOverride = FieldType.Label)
  private String orderTabHeading;
  @CCD(label = "## Hearings", searchable = false, typeOverride = FieldType.Label)
  private String hearingsTabHeading;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasSession;
  @CCD(
          label = "Use this feature to upload applications for additional orders. You might need to upload additional forms, depending on your application type.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {BARRISTERSOLICITORRAccess.class, SOLICITORARAccess.class}
  )
  private String additionalApplicationsLabel;
  @CCD(
          label = "### Confirm recipients",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {BARRISTERCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String otherRecipientsHeader;
  @CCD(
          label = "You must check to see if there are restrictions on who should be notified about the application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {BARRISTERCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String otherRecipientsSubHeader;
  @CCD(
          label = "#### Others in the case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {BARRISTERCaseworkerPubliclawCourtadminCrudAccess.class, CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryRAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String othersToNotifyTitle;
  @CCD(
          label = "#### People in the case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String peopleToNotifyTitle;
  @CCD(label = " ", searchable = false, access = {BARRISTERCrudPlus22RolesWyrnzzAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasOthers;
  @CCD(
          label = " ",
          searchable = false,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, LABARRISTERCruAccess.class, CaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasRespondents;
  @CCD(
          label = "## Add payment details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {BARRISTERSOLICITORRAccess.class, SOLICITORARAccess.class}
  )
  private String paymentDetailsLabel;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CHILDSOLICITORARAccess.class, SOLICITORARAccess.class}
  )
  private String submissionRequiredFieldsInfo;
  @CCD(
          label = "Use this page to remove case access from the third party that created this case for you.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {LASHAREDRAccess.class, LASOLICITORRAccess.class}
  )
  private String managingOrganisationRemovalInfo;
  @CCD(
          label = "Press 'Continue' to remove this organisation, or use the 'Cancel' link below to return to the case details page.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {LASHAREDRAccess.class, LASOLICITORRAccess.class}
  )
  private String managingOrganisationRemovalConfirmation;
  @CCD(
          label = "Current managing organisation:",
          searchable = false,
          access = {LASHAREDCrudAccess.class, LASOLICITORCrudAccess.class}
  )
  private String managingOrganisationName;
  @CCD(
          label = "Hidden field used to store the count of things to be shown/hidden",
          searchable = false,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawJudiciaryCrudAccess.class}
  )
  private String optionCount;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CAFCASSSOLICITORCruPlus3RolesUpxliqAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasRespondentsOrOthers;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, LASOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo otherOrderType;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGCaseworkerPubliclawSystemupdateCrudAccess.class, LASHAREDCrudAccess.class, LASOLICITORCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo secureAccommodationOrderType;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo refuseContactWithChildOrderType;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo contactWithChildInCareOrderType;
  @CCD(
          label = "## Confirm recipients",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGCruLABARRISTERRLAMANAGINGRLASHAREDRLASOLICITORRAccess.class, CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String placementRespondentsSectionHeader;
  @CCD(
          label = "You must check to see if there are restrictions on who should be notified about the placement application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGCruLABARRISTERRLAMANAGINGRLASHAREDRLASOLICITORRAccess.class, CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String placementRespondentsSectionSubHeader;
  @CCD(
          label = "<div class='govuk-tag govuk-tag--red'>Please don't use to assign respondent/child solicitor</div>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String warning_label;
  @JsonProperty("SearchCriteria")
  @CCD(
          label = " ",
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, GSProfileRPlus3RolesOxxhtyAccess.class, CaseworkerPubliclawSystemupdateRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.SearchCriteria searchCriteria;
  @CCD(label = " ", access = {GSProfileRPlus40RolesFkdfmiAccess.class})
  private String caseNameHmctsInternal;
  @CCD(
          label = " ",
          typeOverride = FieldType.DynamicList,
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, BARRISTERRPlus3RolesDckcthAccess.class, GSProfileRPlus3RolesOxxhtyAccess.class, CaseworkerPubliclawSystemupdateRAccess.class}
  )
  private String caseManagementCategory;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGCaseworkerPubliclawSystemupdateCrudAccess.class, LASHAREDCrudAccess.class, LASOLICITORCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo c1Application;
  @CCD(
          label = "Have draft orders been approved",
          searchable = false,
          access = {CaseworkerPubliclawSuperuserCruPlus1RolesYwyfdvAccess.class, CaseworkerPubliclawJudiciaryCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo draftOrdersApproved;
  @CCD(
          label = " ",
          searchable = false,
          access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class, CaseworkerPubliclawJudiciaryCrudAccess.class}
  )
  private java.util.Set<ConfirmPR> respondentsConfirmPR;
  @CCD(label = " ", access = {BARRISTERCruPlus43RolesIgpdzgAccess.class})
  private java.time.LocalDateTime lastGenuineUpdateTime;
  @CCD(
          label = "Set up TTL",
          searchable = false,
          access = {TTLProfileCruAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.TTL TTL;
  @CCD(
          label = "The case has a respondent LA",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesBetqimAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasRespondentLA;
  @CCD(
          label = "Please input your name (as you would like it to appear on the C2 order cover sheet)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String approveOrderLegalAdvisorFullName;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private String selectedHearingIdDraft;
  @CCD(
          label = "Component Launcher",
          searchable = false,
          typeOverride = FieldType.ComponentLauncher,
          access = {CaseworkerPubliclawCourtadminCuPlus2RolesKlflrfAccess.class, CaseworkerPubliclawMagistrateCuAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
  )
  private String componentLauncher;
  @JsonProperty("LinkedCasesComponentLauncher")
  @CCD(
          label = "Component Launcher",
          searchable = false,
          typeOverride = FieldType.ComponentLauncher,
          access = {GSProfileRPlus3RolesRkhhdlAccess.class}
  )
  private String linkedCasesComponentLauncher;
  @CCD(label = "COURT")
  private String courtCode;
  @CCD(
          label = "Case Progression Report",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
  )
  private String caseProgressionReportDetails;
  @CCD(
          label = "${caseProgressionReportDetails}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
  )
  private String caseProgressionReportView;
  @CCD(
          label = "<div class='govuk-tag govuk-tag--red'>WELSH CASE</div>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String caseSummaryWelshFlag;
  @CCD(
          label = "<div class='govuk-tag govuk-tag--red'>WELSH CASE</div>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String caseSummaryLAWelshFlag;
  @CCD(
          label = "<div class='govuk-tag govuk-tag--red'>HIGH COURT CASE</div>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String caseSummaryHighCourtCaseFlag;
  @CCD(
          label = "<div class='govuk-tag govuk-tag--red'>HIGH COURT CASE</div>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String caseSummaryLAHighCourtCaseFlag;
  @CCD(
          label = "<div class='govuk-tag govuk-tag--red'>Potentially violent person</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private String caseSummaryFlagAddedTag;
  @CCD(
          label = "<div class='panel panel-border-wide'>Current state: ${[STATE]}</div><br/>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String currentStateLabel;
  @CCD(
          label = "Content to be displayed above confirmChangeState, text type so that it can be dynamic",
          searchable = false,
          access = {CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String nextStateLabelContent;
  @CCD(
          label = "## Confirm legal representation details\n  ## Main representative: ${childrenMainRepresentative.firstName} ${childrenMainRepresentative.lastName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCruAccess.class}
  )
  private String childrenMainRepresentativeName;
  @CCD(
          label = "Your local authority is associated to more than 1 court. Choose which one you want to issue your application to.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String courtSelectionLabel;
  @CCD(
          label = "highCourtDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HighCourtDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private HighCourtDFJCourts highCourtDFJCourt;
  @CCD(
          label = "centralLondonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "CentralLondonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private CentralLondonDFJCourts centralLondonDFJCourt;
  @CCD(
          label = "eastLondonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "EastLondonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private EastLondonDFJCourts eastLondonDFJCourt;
  @CCD(
          label = "westLondonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "WestLondonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private WestLondonDFJCourts westLondonDFJCourt;
  @CCD(
          label = "birminghamDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "BirminghamDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private BirminghamDFJCourts birminghamDFJCourt;
  @CCD(
          label = "coventryDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "CoventryDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private CoventryDFJCourts coventryDFJCourt;
  @CCD(
          label = "derbyDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "DerbyDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private DerbyDFJCourts derbyDFJCourt;
  @CCD(
          label = "leicesterDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "LeicesterDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private LeicesterDFJCourts leicesterDFJCourt;
  @CCD(
          label = "lincolnDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "LincolnDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private LincolnDFJCourts lincolnDFJCourt;
  @CCD(
          label = "northamptonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "NorthamptonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private NorthamptonDFJCourts northamptonDFJCourt;
  @CCD(
          label = "nottinghamDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "NottinghamDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private NottinghamDFJCourts nottinghamDFJCourt;
  @JsonProperty("stoke-on-TrentDFJCourt")
  @CCD(
          label = "stoke-on-TrentDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "Stoke-on-TrentDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private String stoke_on_TrentDFJCourt;
  @CCD(
          label = "wolverhamptonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "WolverhamptonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private WolverhamptonDFJCourts wolverhamptonDFJCourt;
  @CCD(
          label = "worcesterDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "WorcesterDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private WorcesterDFJCourts worcesterDFJCourt;
  @CCD(
          label = "clevelandAndSouthDurhamDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "ClevelandAndSouthDurhamDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private ClevelandAndSouthDurhamDFJCourts clevelandAndSouthDurhamDFJCourt;
  @CCD(
          label = "humbersideDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HumbersideDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private HumbersideDFJCourts humbersideDFJCourt;
  @CCD(
          label = "northYorkshireDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "NorthYorkshireDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private NorthYorkshireDFJCourts northYorkshireDFJCourt;
  @CCD(
          label = "northumbriaAndNorthDurhamDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "NorthumbriaAndNorthDurhamDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private NorthumbriaAndNorthDurhamDFJCourts northumbriaAndNorthDurhamDFJCourt;
  @CCD(
          label = "southYorkshireDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "SouthYorkshireDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private SouthYorkshireDFJCourts southYorkshireDFJCourt;
  @CCD(
          label = "westYorkshireDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "WestYorkshireDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private WestYorkshireDFJCourts westYorkshireDFJCourt;
  @CCD(
          label = "blackburnLancasterDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "BlackburnLancasterDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private BlackburnLancasterDFJCourts blackburnLancasterDFJCourt;
  @CCD(
          label = "carlisleDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "CarlisleDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private CarlisleDFJCourts carlisleDFJCourt;
  @CCD(
          label = "liverpoolDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "LiverpoolDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private LiverpoolDFJCourts liverpoolDFJCourt;
  @CCD(
          label = "manchesterDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "ManchesterDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private ManchesterDFJCourts manchesterDFJCourt;
  @CCD(
          label = "brightonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "BrightonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private BrightonDFJCourts brightonDFJCourt;
  @CCD(
          label = "essexAndSuffolkDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "EssexAndSuffolkDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private EssexAndSuffolkDFJCourts essexAndSuffolkDFJCourt;
  @CCD(
          label = "guildfordDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "GuildfordDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private GuildfordDFJCourts guildfordDFJCourt;
  @CCD(
          label = "lutonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "LutonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private LutonDFJCourts lutonDFJCourt;
  @CCD(
          label = "medwayDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "MedwayDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private MedwayDFJCourts medwayDFJCourt;
  @CCD(
          label = "miltonKeynesDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "MiltonKeynesDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private MiltonKeynesDFJCourts miltonKeynesDFJCourt;
  @CCD(
          label = "norwichDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "NorwichDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private NorwichDFJCourts norwichDFJCourt;
  @CCD(
          label = "peterboroughDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "PeterboroughDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private PeterboroughDFJCourts peterboroughDFJCourt;
  @CCD(
          label = "readingDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "ReadingDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private ReadingDFJCourts readingDFJCourt;
  @CCD(
          label = "watfordDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "WatfordDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private WatfordDFJCourts watfordDFJCourt;
  @CCD(
          label = "bournemouthAndDorsetDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "BournemouthAndDorsetDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private BournemouthAndDorsetDFJCourts bournemouthAndDorsetDFJCourt;
  @CCD(
          label = "bristolDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "BristolDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private BristolDFJCourts bristolDFJCourt;
  @CCD(
          label = "devonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "DevonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private DevonDFJCourts devonDFJCourt;
  @CCD(
          label = "portsmouthDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "PortsmouthDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private PortsmouthDFJCourts portsmouthDFJCourt;
  @CCD(
          label = "swindonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "SwindonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private SwindonDFJCourts swindonDFJCourt;
  @CCD(
          label = "tauntonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "TauntonDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private TauntonDFJCourts tauntonDFJCourt;
  @CCD(
          label = "truroDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "TruroDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private TruroDFJCourts truroDFJCourt;
  @CCD(
          label = "northWalesDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "NorthWalesDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private NorthWalesDFJCourts northWalesDFJCourt;
  @CCD(
          label = "southEastWalesDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "SouthEastWalesDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private SouthEastWalesDFJCourts southEastWalesDFJCourt;
  @CCD(
          label = "swanseaDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "SwanseaDFJCourts",
          access = {CaseworkerPubliclawSolicitorCuCaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private SwanseaDFJCourts swanseaDFJCourt;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo showFinalOrderSingleChildPage;
  @CCD(
          label = "## Create order for",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String remainingChild_label;
  @CCD(
          label = "Child included in the order",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class}
  )
  private String remainingChild;
  @CCD(
          label = "### Other children in this case with final orders:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String otherFinalOrderChildren_label;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawMagistrateRAccess.class}
  )
  private String otherFinalOrderChildren;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private String singleCareOrder_label;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private String multipleCareOrder_label;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRAccess.class}
  )
  private String children_label;
  @CCD(
          label = "FamilyMan case number",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String readOnlyFamilyManCaseNumber;
  @CCD(
          label = "Children in the order",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String readOnlyChildren;
  @CCD(
          label = "### Check and issue order\nCheck you're uploading the right order to the right case. When you're happy, press continue to issue the order.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String readOnlyOrder_label;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document readOnlyOrder;
  @CCD(
          label = "## Check order details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String checkYourOrder_label;
  @CCD(label = "### Others in the case", searchable = false, typeOverride = FieldType.Label)
  private String othersTitle;
  @CCD(
          label = "[Make changes to orders and directions sought](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/ordersNeeded)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String ordersAndDirectionsLink;
  @CCD(
          label = "[Make changes to hearing urgency](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/hearingNeeded)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String hearingUrgencyLink;
  @CCD(
          label = "[Make changes to threshold criteria](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterGrounds)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String thresholdCriteriaLink;
  @CCD(
          label = "[Make changes to risk and harm to children](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterRiskHarm)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String riskAndHarmToChildrenLink;
  @CCD(
          label = "[Make changes to other proceedings for the child](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/otherProceedings)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String otherProceedingsLink;
  @CCD(
          label = "[Make changes to international element](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterInternationalElement)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String internationalElementLink;
  @CCD(
          label = "[Make changes to documents](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/uploadDocuments)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String documentsLink;
  @CCD(
          label = "[Make changes to local authority details](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterLocalAuthority)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String localAuthorityDetailsLink;
  @CCD(
          label = "[Make changes to the child's details](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterChildren)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String childrenLink;
  @CCD(
          label = "[Make changes to the respondents' details](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterRespondents)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String respondentsLink;
  @CCD(
          label = "[Make changes to other people in the case](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/enterOthers)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String othersLink;
  @CCD(
          label = "[Make changes to allocation proposal](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/otherProposal)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String allocationProposalLink;
  @CCD(
          label = "[Make changes to court services](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/attendingHearing)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String facilitiesOrServicesNeededLink;
  @CCD(
          label = "[Add the gatekeeping order](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/addGatekeepingOrder)\n\nThis is also known as the initial or standard directions order",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String draftSDOLinkOrdersTab;
  @CCD(
          label = "[Send or replace this order](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/addGatekeepingOrder)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String draftSDOLinkDraftOrdersTab;
  @CCD(
          label = "[Approve orders](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/reviewCMO)",
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserCuAccess.class}
  )
  private String reviewCMOLink;
  @CCD(
          label = "[Upload additional applications](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/uploadAdditionalApplications)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String uploadC2Link;
  @CCD(
          label = "[Review additional application](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/reviewAdditionalApplication)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String reviewAdditionalApplicationLink;
  @CCD(
          label = "[Manage orders](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/manageOrders)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String createOrderLink;
  @CCD(
          label = "[Upload CMO](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/uploadCMO)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String uploadCMOLink;
  @CCD(
          label = "[Add counsel/external solicitor](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/manageLegalRepresentatives)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String manageLegalRepresentativesLink;
  @CCD(
          label = "[Send messages](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/messageJudgeOrLegalAdviser)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String messageJudgeLegalAdviserLink;
  @CCD(
          label = "[Reply to messages](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/replyToMessageJudgeOrLegalAdviser)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String replyToMessageJudgeOrLegalAdviserLink;
  @CCD(
          label = "[Extend 26-week timeline](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/extend26WeekTimeline)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String extend26WeekTimelineLink;
  @CCD(
          label = "[Add or remove case flag](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/addCaseFlag)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSystemupdateCudAccess.class}
  )
  private String addCaseFlagEventLink;
  @CCD(
          label = "[List Gatekeeping Hearing](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/listGatekeepingHearing)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String listGatekeepingHearingLink;
  @CCD(
          label = "Extended timeline:",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPubliclawCourtadminCuAccess.class, CaseworkerPubliclawJudiciaryCudAccess.class}
  )
  private String caseSummaryExtensionDetails;
  @CCD(
          label = "## Add extension length and reason",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawJudiciaryCrudAccess.class}
  )
  private String extensionLabel;
  @JsonProperty("direction-REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE")
  @CCD(
          label = "Request permission for expert evidence",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE;
  @JsonProperty("direction-REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS")
  @CCD(
          label = "Request help to take part in proceedings",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_REQUEST_HELP_TO_TAKE_PART_IN_PROCEEDINGS;
  @JsonProperty("direction-ASK_FOR_DISCLOSURE")
  @CCD(
          label = "Ask for disclosure",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_ASK_FOR_DISCLOSURE;
  @JsonProperty("direction-ATTEND_HEARING")
  @CCD(
          label = "Attend the pre-hearing and hearing",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private EditableStandardDirection direction_ATTEND_HEARING;
  @JsonProperty("direction-CONTACT_ALTERNATIVE_CARERS")
  @CCD(
          label = "Contact alternative carers",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_CONTACT_ALTERNATIVE_CARERS;
  @JsonProperty("direction-SEND_DOCUMENTS_TO_ALL_PARTIES")
  @CCD(
          label = "Send documents to all parties",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private EditableStandardDirection direction_SEND_DOCUMENTS_TO_ALL_PARTIES;
  @JsonProperty("direction-SEND_MISSING_ANNEX")
  @CCD(
          label = "Send missing annex documents to the court and all parties",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private EditableStandardDirection direction_SEND_MISSING_ANNEX;
  @JsonProperty("direction-IDENTIFY_ALTERNATIVE_CARERS")
  @CCD(
          label = "Identify alternative carers",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_IDENTIFY_ALTERNATIVE_CARERS;
  @JsonProperty("direction-SEND_TRANSLATED_DOCUMENTS")
  @CCD(
          label = "Send translated case documents to respondents",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private EditableStandardDirection direction_SEND_TRANSLATED_DOCUMENTS;
  @JsonProperty("direction-LODGE_BUNDLE")
  @CCD(
          label = "Lodge a bundle",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_LODGE_BUNDLE;
  @JsonProperty("direction-SEND_CASE_SUMMARY")
  @CCD(
          label = "Send case summary to all parties",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_SEND_CASE_SUMMARY;
  @JsonProperty("direction-CONSIDER_JURISDICTION")
  @CCD(
          label = "Urgently consider jurisdiction and invite any representations",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private EditableStandardDirection direction_CONSIDER_JURISDICTION;
  @JsonProperty("direction-REDUCE_TIME_FOR_SERVICE")
  @CCD(
          label = "Reduce time for service of notice of the proceedings",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_REDUCE_TIME_FOR_SERVICE;
  @JsonProperty("direction-SEND_RESPONSE_TO_THRESHOLD_STATEMENT")
  @CCD(
          label = "Send response to threshold statement to all parties",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_SEND_RESPONSE_TO_THRESHOLD_STATEMENT;
  @JsonProperty("direction-ARRANGE_ADVOCATES_MEETING")
  @CCD(
          label = "Arrange an advocates' meeting",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_ARRANGE_ADVOCATES_MEETING;
  @JsonProperty("direction-SEND_GUARDIANS_ANALYSIS")
  @CCD(
          label = "Send the guardian's analysis to all parties",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_SEND_GUARDIANS_ANALYSIS;
  @JsonProperty("direction-APPOINT_CHILDREN_GUARDIAN")
  @CCD(
          label = "Appoint a children's guardian",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private EditableStandardDirection direction_APPOINT_CHILDREN_GUARDIAN;
  @JsonProperty("direction-APPOINT_CHILDREN_GUARDIAN_IMMEDIATE")
  @CCD(
          label = "Appoint a children's guardian",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private ImmediateStandardDirection direction_APPOINT_CHILDREN_GUARDIAN_IMMEDIATE;
  @JsonProperty("direction-OBJECT_TO_REQUEST_FOR_DISCLOSURE")
  @CCD(
          label = "Object to a request for disclosure",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private StandardDirection direction_OBJECT_TO_REQUEST_FOR_DISCLOSURE;
  @JsonProperty("direction-ARRANGE_INTERPRETERS")
  @CCD(
          label = "Arrange interpreters",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private EditableStandardDirection direction_ARRANGE_INTERPRETERS;
  @JsonProperty("direction-ARRANGE_INTERPRETERS_IMMEDIATE")
  @CCD(
          label = "Arrange interpreters",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private ImmediateStandardDirection direction_ARRANGE_INTERPRETERS_IMMEDIATE;
  @CCD(
          label = "<h2 class=\"govuk-!-margin-top-9\">All parties</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForAllPartiesLabelSmall;
  @CCD(
          label = "<h1 class=\"govuk-!-margin-top-9\">All parties</h1>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForAllPartiesLabel;
  @CCD(
          label = "<h2 class=\"govuk-!-margin-top-9\">Local authority</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForLocalAuthorityLabelSmall;
  @CCD(
          label = "<h1 class=\"govuk-!-margin-top-9\">Local authority</h1>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForLocalAuthorityLabel;
  @CCD(
          label = "<h2 class=\"govuk-!-margin-top-9\">Parents and respondents</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForRespondentsLabelSmall;
  @CCD(
          label = "<h1 class=\"govuk-!-margin-top-9\">Parents and respondents</h1>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryRAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForRespondentsLabel;
  @CCD(
          label = "<h2 class=\"govuk-!-margin-top-9\">Cafcass</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForCafcassLabelSmall;
  @CCD(
          label = "<h1 class=\"govuk-!-margin-top-9\">Cafcass</h1>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForCafcassLabel;
  @CCD(
          label = "<h2 class=\"govuk-!-margin-top-9\">Other parties</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForOthersLabelSmall;
  @CCD(
          label = "<h1 class=\"govuk-!-margin-top-9\">Other parties</h1>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForOthersLabel;
  @CCD(
          label = "<h2 class=\"govuk-!-margin-top-9\">Court</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForCourtLabelSmall;
  @CCD(
          label = "<h1 class=\"govuk-!-margin-top-9\">Court</h1>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String directionsForCourtLabel;
  @CCD(
          label = "## Choose standard directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String selectDirectionsLabel;
  @CCD(
          label = "## Choose urgent directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String selectUrgentDirectionsLabel;
  @CCD(
          label = "## Confirm details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String editDirectionsLabel;
  @CCD(
          label = "Hearing date",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private String gatekeepingOrderHearingDate1;
  @CCD(
          label = "Hearing date",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private String gatekeepingOrderHearingDate2;
  @CCD(
          label = "Has hearing date",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo gatekeepingOrderHasHearing1;
  @CCD(
          label = "Has hearing date",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo gatekeepingOrderHasHearing2;
  @CCD(
          label = "You must now complete the \"List gatekeeping hearing\" event to allocate the judge, list the hearing and serve the order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String listGatekeepingHearingLabel;
  @CCD(
          label = "<br /><div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>Check if this document is related to ${caseName}</strong></div><br>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String uploadCMOMessageAcknowledgeLabel;
  @CCD(label = "## Check your orders", searchable = false, typeOverride = FieldType.Label)
  private String ordersToSendHeader;
  @CCD(
          label = "There are no draft orders to review.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String noDraftCMOs;
  @CCD(
          label = "<div class='panel panel-border-wide'>You can add CMOs and C21s that relates to the same hearing in one go.<br/><br/>If they are for different hearings, or are not related to any hearings, add these separately.</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, BARRISTERSOLICITORRAccess.class, LABARRISTERRAccess.class, CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String hearingOrderDraftInfo;
  @CCD(
          label = "<h2>C21 Orders</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftBlankOrdersTitle;
  @CCD(
          label = "Local authority has sent the following orders for approval:",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRPlus2RolesQqgmyhAccess.class}
  )
  private String reviewDraftOrdersTitles;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRPlus2RolesQqgmyhAccess.class}
  )
  private String draftOrdersTitlesInBundle;
  @CCD(
          label = "<h2>Case Management Order</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String cmoDraftOrderTitleLabel;
  @CCD(
          label = "<h2>Order 1</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder1TitleLabel;
  @CCD(
          label = "<h2>Order 2</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder2TitleLabel;
  @CCD(
          label = "<h2>Order 3</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder3TitleLabel;
  @CCD(
          label = "<h2>Order 4</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder4TitleLabel;
  @CCD(
          label = "<h2>Order 5</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder5TitleLabel;
  @CCD(
          label = "<h2>Order 6</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder6TitleLabel;
  @CCD(
          label = "<h2>Order 7</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder7TitleLabel;
  @CCD(
          label = "<h2>Order 8</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder8TitleLabel;
  @CCD(
          label = "<h2>Order 9</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder9TitleLabel;
  @CCD(
          label = "<h2>Order 10</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawMagistrateRCaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String draftOrder10TitleLabel;
  @CCD(
          label = "If you click 'Continue', the draft order will be approved without any changes. A coversheet will be added showing your name and the date of approval, and a court seal will also be applied to the final version.<br/><br/>Please note that the preview shown below will not display the court seal, but it reflects the content that will be issued.<br/><br/>If you wish to make any amendments, please return to the previous screen and select either 'No, I need to make changes' or 'No, the applicant needs to make changes'.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String previewOrderLabel;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle1;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle2;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle3;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle4;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle5;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle6;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle7;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle8;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle9;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private String previewApprovedOrderTitle10;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder1;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder2;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder3;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder4;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder5;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder6;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder7;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder8;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder9;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document previewApprovedOrder10;
  @CCD(
          label = "Last listing request type",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String lastListingRequestType;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminLegalAdviserCrudAccess.class})
  private java.util.Set<ReviewedListingAction> markListingActionReviewed;
  @CCD(label = "## What do you need to do?", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentActionLabel;
  @CCD(label = "## Choose document types", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentUploadNewDocInstructionLabel;
  @CCD(
          label = "Documents must be:<br /><ul><li>Named using the document type, name of the party and their role - for example, Position-statement-jane-smith-respondent</li><li>No more than 100MB (split and clearly name larger files)</li><li>uploaded separately, not in one large file</li></ul>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {BARRISTERCrudPlus39RolesKamvspAccess.class}
  )
  private String manageDocumentRequirementLabel;
  @CCD(label = "### What type of document do you want to remove?", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentRemoveDocInstructionLabelOne;
  @CCD(label = "### Select which document you want to remove", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentRemoveDocInstructionLabelTwo;
  @CCD(label = "### Why are you removing this document?", searchable = false, typeOverride = FieldType.Label)
  private String manageDocumentRemoveDocReasonLabel;
  @CCD(
          label = "[Manage hearings](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/manageHearings)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String manageHearingsLink;
  @CCD(
          label = "Hearings to be re-listed",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String toReListHearingsLabel;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo showVacatePastHearingWarning;
  @CCD(
          label = "You've chosen to vacate a hearing that's now in the past. If the judge ruled at the hearing that it could not happen, return to the previous page and select 'Adjourn hearing'. \n\n If the hearing was stopped before it was due to take place, continue to mark it as vacated.\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String vacatePastHearingWarning;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasHearingsToAdjourn;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasPastHearings;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasFutureHearings;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasHearingsToVacate;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasHearingsToReList;
  @CCD(
          label = "There are no past hearings to edit",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String noPastHearingsToEditLabel;
  @CCD(
          label = "There are no future hearings to edit",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String noFutureHearingsToEditLabel;
  @CCD(
          label = "There are no past hearings to adjourn",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String noHearingsToAdjournLabel;
  @CCD(
          label = "There are no hearings to vacate",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String noHearingsToVacateLabel;
  @CCD(
          label = "Is this correct?",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo confirmHearingDate;
  @CCD(label = "The hearing date you entered is in the past", searchable = false, typeOverride = FieldType.Label)
  private String confirmHearingDateLabel;
  @CCD(
          label = "Start date and time",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String hearingStartDateLabel;
  @CCD(
          label = "Hearing duration",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String hearingDurationLabel;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private String startDateFlag;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private String endDateFlag;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private String showConfirmPastHearingDatesPage;
  @CCD(
          label = "There are no adjourned or vacated hearings to re-list",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String noHearingsToReListLabel;
  @CCD(
          label = "Use this feature to: <ul><li>give case access to another local authority</li><li>remove case access from local authority</li><li>transfer a case to another local authority - they'll become the designated authority</li><li>transfer to another court</li><ul/><br/>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String localAuthorityActionLabel;
  @CCD(
          label = "Use this feature to: <ul><li>give case access to another local authority</li><li>remove case access from local authority</li><ul/><br/>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String localAuthorityActionLabelLA;
  @CCD(
          label = "<h2>Add or confirm local authority contact</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String localAuthorityEmailLabel;
  @CCD(
          label = "<h2>Remove additional local authority</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String localAuthorityToRemoveLabel;
  @CCD(
          label = "<h2>Select a local authority to transfer the case to</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String localAuthorityToTransferLabel;
  @CCD(
          label = "The case will be transferred to <strong>${sharedLocalAuthority}</strong>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String sharedLocalAuthorityLabel;
  @CCD(
          label = "<h2>Confirm court</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String confirmCourtLabel;
  @CCD(
          label = "Was the order approved at a hearing?",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo manageOrdersApprovedAtHearing;
  @CCD(
          label = "Is there an application for the order on the system?",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo manageOrdersShouldLinkApplication;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudAccess.class, CaseworkerPubliclawGatekeeperCrAccess.class, CaseworkerPubliclawJudiciaryCrAccess.class, CaseworkerPubliclawSystemupdateCrAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document orderPreview;
  @CCD(
          label = "The case will remain open for 21 days to allow for appeal.\n\n In a closed case, you can still: \n * add a case note \n * upload a document \n * issue a C21 (blank order) \n * submit a C2 application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private String manageOrdersCloseCaseWarning;
  @CCD(
          label = "Is there an exclusion requirement?",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo manageOrdersHasExclusionRequirement;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String appointedGuardians_label;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String respondentsRefused_label;
  @CCD(
          label = "Does a supervision order (C35a) already exist for this case?",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo manageOrdersC35aOrderExists;
  @CCD(
          label = "There is no existing supervision order (C35a) so you cannot proceed.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private String manageOrdersC35aOrderDoesntExistMessage;
  @CCD(
          label = "The court varies this supervision order.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private String manageOrdersSupervisionOrderVariationHeading;
  @CCD(
          label = "The court extends this supervision order.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private String manageOrdersSupervisionOrderExtensionHeading;
  @CCD(
          label = "No person may publish any information relating to the proceedings to the public or a section of it, which includes: \n\n * The name or date of birth of any subject child in the case. \n * The name of any parent or family member who is a party or who is mentioned in the case, or whose name may lead to the child(ren) being identified. \n * The name of any person who is a party to, or intervening in, the proceedings. \n * The address of any child or family member. \n * The name or address of any foster carer. \n * The school/hospital/placement name or address, or any identifying features of a school of the child. \n * Photographs or images of the child, their parents, carer or any other identifying person, or any of the locations specified above in conjunction with other information relating to the proceedings. \n * The names of any medical professional who is or has been treating any of the children or family member. \n * In cases involving alleged sexual abuse, the details of such alleged abuse. \n * For the purposes of s.97(2) Children Act 1989, any other information likely to identify the child as a subject child or former subject child.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private String manageOrdersTransparencyOrderPublishInformationLabel;
  @CCD(
          label = "For the avoidance of doubt, no body, agency or professionals may be identified in any information relating to the proceedings published to the general public or a section of it by a pilot reporter, save for: \n\n * The local authority/authorities involved in the proceedings. \n * The director and assistant director of Children's Services within the LA (but no other person from the local authority, including the social worker, without express permission of the court). \n * Cafcass (but not the children's guardian or reporting officer without express permission of the court). \n * Any NHS Trust. \n * Court appointed experts (but not treating clinicians or medical professionals). \n * Legal representatives and judges. \n * Anyone else named in a published judgment.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private String manageOrdersTransparencyOrderPublishIdentityLabel;
  @CCD(
          label = "A pilot reporter who attends a hearing in family proceedings in accordance with FPR r.27.11, or who indicates in advance that they wish to attend a hearing, is entitled to see, quote from, or publish: \n\n * Documents drafted by advocates (or litigants if a party is self-representing): i.e. Case outlines, skeleton arguments, summaries, position statements threshold documents and chronologies. \n * Any indices from the Court bundle. \n * Any suitably anonymised Orders within the case.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private String manageOrdersTransparencyOrderPublishDocumentsLabel;
  @CCD(
          label = "## Create an order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String createOrderSectionHeader;
  @CCD(
          label = "## Upload an order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String uploadOrderSectionHeader;
  @CCD(
          label = "## Add issuing details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String hearingDetailsSectionHeader;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private String hearingDetailsSectionSubHeader;
  @CCD(
          label = "## Add issuing details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String issuingDetailsSectionHeader;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private String issuingDetailsSectionSubHeader;
  @CCD(
          label = "## Add children's details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String childrenDetailsSectionHeader;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private String childrenDetailsSectionSubHeader;
  @CCD(
          label = "## Add child's details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String childDetailsSectionHeader;
  @CCD(
          label = "## Add order details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String orderDetailsSectionHeader;
  @CCD(
          label = "## Upload order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String uploadOrderSectionSubHeader;
  @CCD(label = " ", searchable = false, access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruAccess.class})
  private String orderDetailsSectionSubHeader;
  @CCD(
          label = "## Check your order\nA preview of your order is below.\n\nCheck the details now. You can use the next page to correct any errors",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String orderPreviewSectionHeader;
  @CCD(
          label = "## Confirm recipients",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String otherDetailsSectionHeader;
  @CCD(
          label = "You must check to see if there are restrictions on who should be sent the order.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String otherDetailsSectionSubHeader;
  @CCD(
          label = "## Download order\nOpen the attached order in PDF-Xchange Editor to make changes.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String amendmentDownloadSectionHeader;
  @CCD(
          label = "## Replace old order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRPlus2RolesYvsczuAccess.class}
  )
  private String amendmentUploadSectionHeader;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasAdditionalApplications;
  @CCD(
          label = "Attach a document or application",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPubliclawCourtadminCrdPlus2RolesJepnldAccess.class}
  )
  private String attachDocumentLabel;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrdPlus2RolesJepnldAccess.class})
  private String nextHearingLabel;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrdPlus2RolesJepnldAccess.class})
  private String replyToMessageJudgeNextHearingLabel;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasJudicialMessages;
  @CCD(
          label = "There is no judicial message to reply.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
  )
  private String noJudicialMessageLabel;
  @CCD(label = " ", access = {CaseworkerPubliclawSystemupdateCrudAccess.class})
  private String migrationId;
  @CCD(
          label = "# ${placementChildName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRPlus2RolesZbkigdAccess.class}
  )
  private String placementChildNameLabel;
  @CCD(
          label = "# ${placementChildName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRPlus2RolesZbkigdAccess.class}
  )
  private String placementChildNameLabel2;
  @CCD(
          label = "# ${placementChildName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRPlus2RolesZbkigdAccess.class}
  )
  private String placementChildNameLabel3;
  @CCD(
          label = "# ${placementChildName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRAccess.class}
  )
  private String placementChildNameLabel4;
  @CCD(label = "Child", searchable = false, access = {BARRISTERUPlus20RolesUqsdckAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Placement>> placementsNonConfidential;
  @CCD(label = "Child", searchable = false, access = {CHILDSOLICITORAUPlus23RolesAtfmazAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Placement>> placementsNonConfidentialNotices;
  @CCD(
          label = "<div class='govuk-notification-banner'><div class='govuk-notification-banner__header'><div class='govuk-notification-banner__title'>No further Placement payments required</div></div><div class='govuk-notification-banner__content'><p class='govuk-notification-banner__heading'>Payment has already been received for a previously created Placement Application today, therefore there is no additional payments required for any additional Placement Applications made today.</p></div></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, LABARRISTERRPlus2RolesZbkigdAccess.class}
  )
  private String placementPaymentNotRequiredLabel;
  @CCD(
          label = "<h2 class='govuk-!-margin-top-9'>Placement notice for local authority</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {LASOLICITORCaseworkerPubliclawCourtadminRAccess.class}
  )
  private String placementNoticeForLocalAuthorityLabel;
  @CCD(
          label = "Local authority notice of placement",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document placementNoticeForLocalAuthority;
  @CCD(
          label = "Local authority notice of placement description",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeForLocalAuthorityDescription;
  @CCD(
          label = "<h2 class='govuk-!-margin-top-9'>Placement notice for Cafcass</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {LASOLICITORCaseworkerPubliclawCourtadminRAccess.class}
  )
  private String placementNoticeForCafcassLabel;
  @CCD(
          label = "Cafcass notice of placement",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document placementNoticeForCafcass;
  @CCD(
          label = "Cafcass notice of placement description",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeForCafcassDescription;
  @CCD(
          label = "<h2 class='govuk-!-margin-top-9'>Placement notice for first parent</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {LASOLICITORCaseworkerPubliclawCourtadminRAccess.class}
  )
  private String placementNoticeForFirstParentLabel;
  @CCD(
          label = "First parent notice of placement",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document placementNoticeForFirstParent;
  @CCD(
          label = "First parent",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeForFirstParentParentsList;
  @CCD(
          label = "First parent notice of placement description",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeForFirstParentDescription;
  @CCD(
          label = "<h2 class='govuk-!-margin-top-9'>Placement notice for second parent</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {LASOLICITORCaseworkerPubliclawCourtadminRAccess.class}
  )
  private String placementNoticeForSecondParentLabel;
  @CCD(
          label = "Second parent notice of placement",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document placementNoticeForSecondParent;
  @CCD(
          label = "Second parent",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeForSecondParentParentsList;
  @CCD(
          label = "Second parent notice of placement description",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeForSecondParentDescription;
  @CCD(
          label = "Do you require notice of placement for the second parent?",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo placementNoticeForSecondParentRequired;
  @CCD(
          label = "Do you require notice of placement for the first parent?",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo placementNoticeForFirstParentRequired;
  @CCD(
          label = "Do you require notice of placement for the Local Authority?",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo placementNoticeForLocalAuthorityRequired;
  @CCD(
          label = "Do you require notice of placement for the Cafcass?",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo placementNoticeForCafcassRequired;
  @CCD(
          label = "Did you received local authority response to notice of placement?",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo placementNoticeResponseFromLocalAuthorityReceived;
  @CCD(
          label = "Local authority response",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document placementNoticeResponseFromLocalAuthority;
  @CCD(
          label = "Local authority response description",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeResponseFromLocalAuthorityDescription;
  @CCD(
          label = "Did you received Cafcass response to notice of placement??",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo placementNoticeResponseFromCafcassReceived;
  @CCD(
          label = "Cafcass response",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document placementNoticeResponseFromCafcass;
  @CCD(
          label = "Cafcass response description",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeResponseFromCafcassDescription;
  @CCD(
          label = "Did you received first parent response to notice of placement?",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo placementNoticeResponseFromFirstParentReceived;
  @CCD(
          label = "First parent response",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document placementNoticeResponseFromFirstParent;
  @CCD(
          label = "First parent response description",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeResponseFromFirstParentDescription;
  @CCD(
          label = "Did you received second parent response to notice of placement?",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo placementNoticeResponseFromSecondParentReceived;
  @CCD(
          label = "Second parent notice of placement response",
          searchable = false,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document placementNoticeResponseFromSecondParent;
  @CCD(
          label = "Second parent response description",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
  )
  private String placementNoticeResponseFromSecondParentDescription;
  @CCD(label = "### Respondents in the case", searchable = false, typeOverride = FieldType.Label)
  private String placementRespondentsTitle;
  @CCD(
          label = "There are no current placement applications to issue a notice of hearing for.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String noPlacementApplicationsLabel;
  @JsonProperty("QueryManagementComponentLauncher")
  @CCD(
          label = "Component Launcher",
          searchable = false,
          typeOverride = FieldType.ComponentLauncher,
          access = {BARRISTERCrudPlus38RolesIhmdtsAccess.class}
  )
  private String queryManagementComponentLauncher;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CaseworkerPubliclawCafcassCrudAccess.class}
  )
  private String qmCaseQueriesCollectionCafcass;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, LASOLICITORCrudCaseworkerPubliclawSystemupdateCuAccess.class}
  )
  private String qmCaseQueriesCollectionLASol;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, EPSMANAGINGCrudAccess.class}
  )
  private String qmCaseQueriesCollectionEPSManaging;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, LAMANAGINGCrudAccess.class}
  )
  private String qmCaseQueriesCollectionLAManaging;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, LABARRISTERCrudAccess.class}
  )
  private String qmCaseQueriesCollectionLABarrister;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, LASHAREDCrudAccess.class}
  )
  private String qmCaseQueriesCollectionLAShared;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, BARRISTERCrudAccess.class}
  )
  private String qmCaseQueriesCollectionBarrister;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORCrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitor;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORACrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorA;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORBCrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorB;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORCCrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorC;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORDCrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorD;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORECrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorE;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORFCrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorF;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORGCrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorG;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORHCrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorH;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORICrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorI;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, SOLICITORJCrudAccess.class}
  )
  private String qmCaseQueriesCollectionSolicitorJ;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORACrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolA;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORBCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolB;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORCCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolC;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORDCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolD;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORECrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolE;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORFCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolF;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORGCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolG;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORHCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolH;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORICrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolI;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORJCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolJ;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORKCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolK;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORLCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolL;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORMCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolM;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITORNCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolN;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CHILDSOLICITOROCrudAccess.class}
  )
  private String qmCaseQueriesCollectionChildSolO;
  @CCD(
          label = "Queries",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCrudPlus2RolesDwddutAccess.class, CAFCASSSOLICITORCrudAccess.class}
  )
  private String qmCaseQueriesCollectionCafcassSol;
  @CCD(
          label = "<div class='panel panel-border-wide'><h2>Check you're removing the right order</h2><p>You will not be able to reinstate it after it’s removed</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String removeOrderWarning;
  @CCD(
          label = "<div class='panel panel-border-wide'><h2>Check you're removing the right document sent to parties</h2><p>You will not be able to reinstate it after it’s removed</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String removeDocumentsSentToPartiesWarning;
  @CCD(
          label = "<div class='panel panel-border-wide'><h2>Check you're removing the right application</h2><p>You will not be able to reinstate it after it’s removed</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String removeApplicationWarning;
  @CCD(label = "Link to order", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document orderToBeRemoved;
  @CCD(
          label = "### Link to application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String applicationToBeRemovedLabel;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document c2ApplicationToBeRemoved;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document otherApplicationToBeRemoved;
  @CCD(label = "Order title", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private String orderTitleToBeRemoved;
  @CCD(label = "Party name", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private String partyNameToBeRemoved;
  @CCD(label = "Date and time sent", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private String sentAtToBeRemoved;
  @CCD(
          label = "ID from Send Letter Service",
          searchable = false,
          access = {CaseworkerPubliclawSuperuserCruAccess.class}
  )
  private String letterIdToBeRemoved;
  @CCD(label = "File", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.Document sentDocumentToBeRemoved;
  @CCD(label = "Application type", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private String applicationTypeToBeRemoved;
  @CCD(label = "Date of issue", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private String orderIssuedDateToBeRemoved;
  @CCD(label = "Date and time of upload", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private String orderDateToBeRemoved;
  @CCD(label = "Hearing", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private String hearingToUnlink;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo showRemoveCMOFieldsFlag;
  @CCD(
          label = "The case state will return to Gatekeeping",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserRAccess.class}
  )
  private String removeStandardDirectionOrderWarning;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo showRemoveSDOWarningFlag;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawSuperuserCruAccess.class})
  private String showReasonFieldFlag;
  @CCD(
          label = "<div class='panel panel-border-wide'><h2>You are about to remove the generated application form for this case (C110a or C1).</h2><p>This should only be performed if the document needs to be hidden from all parties as confidential information has been disclosed. You will not be able to reinstate it after it’s removed.</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private String removeApplicationFormLabel;
  @CCD(
          label = "<div class='panel panel-border-wide'><h2>Check you're removing the right placement application</h2><p>You will not be able to reinstate it after it’s removed</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawSuperuserCrudAccess.class}
  )
  private String removePlacementApplicationWarning;
  @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawSuperuserCrudAccess.class})
  private Placement placementApplicationToBeRemoved;
  @CCD(
          label = "No additional application bundles to review",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawJudiciaryCaseworkerPubliclawMagistrateCrudAccess.class}
  )
  private String confirmApplicationReviewedNoApplicationLabel;
  @CCD(
          label = "Are any hearings missing orders?",
          searchable = false,
          access = {CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasHearingsMissingOrders;
  @CCD(label = "-", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
  private String listOfHearingsMissingOrders;
  @CCD(
          label = "These concluded hearings do not have CMOs attached (in draft or sealed): <br/> ${listOfHearingsMissingOrders} <br/>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String hearingsMissingOrdersMessage;
  @CCD(
          label = "There are no concluded hearings missing CMOs on this case.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminCruAccess.class}
  )
  private String noHearingsMissingOrdersMessage;
  @CCD(label = "## Judicial messages", searchable = false, typeOverride = FieldType.Label)
  private String judicialMessagesTabHeading;
  @CCD(label = "## Closed messages", searchable = false, typeOverride = FieldType.Label)
  private String closedJudicialMessagesTabHeading;
  @CCD(label = "## Case information", searchable = false, typeOverride = FieldType.Label)
  private String caseSummaryTabHeading;
  @CCD(label = "## Case information", searchable = false, typeOverride = FieldType.Label)
  private String caseSummaryLATabHeading;
  @CCD(label = "## Unresolved messages", searchable = false, typeOverride = FieldType.Label)
  private String caseSummaryUnresolvedMessagesHeading;
  @CCD(label = "There are some unresolved messages", searchable = false, typeOverride = FieldType.Label)
  private String caseSummaryUnresolvedMessagesLabel;
  @CCD(label = "## Next hearing", searchable = false, typeOverride = FieldType.Label)
  private String caseSummaryNextHearingHeading;
  @CCD(label = "## Previous hearing", searchable = false, typeOverride = FieldType.Label)
  private String caseSummaryPreviousHearingHeading;
  @CCD(label = "## Nominated final hearing", searchable = false, typeOverride = FieldType.Label)
  private String caseSummaryNominatedFinalHearingHeading;
  @CCD(label = "## People in the case", searchable = false, typeOverride = FieldType.Label)
  private String caseSummaryPeopleInTheCaseHeading;
  @CCD(
          label = "## Refused Orders",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawJudiciaryCuAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
  )
  private String refusedOrdersTabHeading;
  @CCD(
          label = "Use this feature to add orders, notices and other documents that have been translated from English to Welsh or Welsh to English.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPubliclawCourtadminRAccess.class}
  )
  private String uploadTranslationsIntro;
  @CCD(
          label = "Last created work allocation task",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "WorkAllocationTaskType",
          access = {CaseworkerPubliclawSystemupdateCruAccess.class}
  )
  private WorkAllocationTaskType lastCreatedWATask;
  // ==== end synthesised definition-only fields ====
}
