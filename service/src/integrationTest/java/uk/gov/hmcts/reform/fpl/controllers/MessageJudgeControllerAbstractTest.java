package uk.gov.hmcts.reform.fpl.controllers;

import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;
import java.util.stream.Stream;

public abstract class MessageJudgeControllerAbstractTest extends AbstractCallbackTest {

    protected MessageJudgeControllerAbstractTest(String eventName) {
        super(eventName);
    }

    protected DynamicList buildRecipientDynamicListNoJudges() {
        return buildRecipientDynamicList(
            List.of(JudicialMessageRoleType.CTSC.toString(), JudicialMessageRoleType.LOCAL_COURT_ADMIN.toString()),
            null, null);
    }

    protected DynamicList buildRecipientDynamicListCTSCLocalCourtOther() {
        return buildRecipientDynamicList(
            List.of(JudicialMessageRoleType.CTSC.toString(),
                JudicialMessageRoleType.LOCAL_COURT_ADMIN.toString(),
                JudicialMessageRoleType.OTHER.toString()),
            null, null);
    }


    protected DynamicList buildRecipientDynamicList(List<String> codesToInclude,
                                          String allocatedLabel,
                                          String hearingLabel) {
        return DynamicList.builder()
            .listItems(Stream.of(DynamicListElement.builder()
                    .code(JudicialMessageRoleType.CTSC.toString())
                    .label(JudicialMessageRoleType.CTSC.getLabel())
                    .build(),
                DynamicListElement.builder()
                    .code(JudicialMessageRoleType.LOCAL_COURT_ADMIN.toString())
                    .label(JudicialMessageRoleType.LOCAL_COURT_ADMIN.getLabel())
                    .build(),
                DynamicListElement.builder()
                    .code(JudicialMessageRoleType.ALLOCATED_JUDGE.toString())
                    .label(allocatedLabel)
                    .build(),
                DynamicListElement.builder()
                    .code(JudicialMessageRoleType.HEARING_JUDGE.toString())
                    .label(hearingLabel)
                    .build(),
                DynamicListElement.builder()
                    .code(JudicialMessageRoleType.OTHER.toString())
                    .label(JudicialMessageRoleType.OTHER.getLabel())
                    .build()
            ).filter(el -> codesToInclude.contains(el.getCode())).toList())
            .build();
    }
}
