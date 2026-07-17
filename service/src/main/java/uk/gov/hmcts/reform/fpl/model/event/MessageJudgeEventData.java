package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.MessageRegardingDocuments;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrdPlus2RolesJepnldAccess;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MessageJudgeEventData {
    @CCD(
            label = "Which application?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    DynamicList additionalApplicationsDynamicList;
    @CCD(
            label = "Your messages",
            hint = "Message subject comprises application type and urgency, if relevant, and date requested.",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    Object judicialMessageDynamicList;
    @CCD(
            label = "Message",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    String judicialMessageNote;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
    JudicialMessageMetaData judicialMessageMetaData;
    @CCD(
            label = "Related documents",
            hint = "Go to the C2 tab to view the files.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrdPlus2RolesJepnldAccess.class}
    )
    String relatedDocumentsLabel;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
    JudicialMessage judicialMessageReply;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    YesNo isJudiciary;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    YesNo isSendingEmailsInCourt;
    @CCD(
            label = "Document type",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    DynamicList documentTypesDynamicList;
    @CCD(
            label = "Which document?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    DynamicList documentDynamicList;
    @CCD(
            label = "Is it about an Application or Document?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    MessageRegardingDocuments isMessageRegardingDocuments;

    public static String[] transientFields() {
        return new String[]{
            "hasAdditionalApplications", "isMessageRegardingDocuments", "additionalApplicationsDynamicList",
            "documentTypesDynamicList", "documentDynamicList", "relatedDocumentsLabel","attachDocumentLabel",
            "nextHearingLabel", "judicialMessageMetaData", "judicialMessageNote", "judicialMessageDynamicList",
            "judicialMessageReply", "replyToMessageJudgeNextHearingLabel", "isJudiciary", "isSendingEmailsInCourt",
            "messageHistoryTemp"
        };
    }
}
