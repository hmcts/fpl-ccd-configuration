package uk.gov.hmcts.reform.fpl.service.cmo;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class HearingOrderKindEventDataBuilderTest {

    private static final UUID SELECTED_HEARING_ID = UUID.randomUUID();
    private static final DocumentReference DOCUMENT_REFERENCE_0 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_1 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_2 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_3 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_4 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_5 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_6 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_7 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_8 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_9 = mock(DocumentReference.class);
    private static final UUID UUID_1 = UUID.randomUUID();

    private final IdentityService identityService = mock(IdentityService.class);

    private final HearingOrderKindEventDataBuilder underTest = new HearingOrderKindEventDataBuilder(identityService,
        new OptionCountBuilder());

    @Test
    void testEmptyHearingOrderBundleDraft() {
        when(identityService.generateId()).thenReturn(UUID_1);

        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder = UploadDraftOrdersData.builder();

        underTest.build(SELECTED_HEARING_ID, CaseData.builder().build(), UploadDraftOrdersData.builder().build(),
            newEventDataBuilder);

        assertThat(newEventDataBuilder.build()).isEqualTo(UploadDraftOrdersData.builder()
            .currentHearingOrderDrafts(List.of(element(UUID_1, HearingOrder.builder().build())))
            .orderToSendOptionCount("0")
            .orderToSend0(null)
            .orderToSend1(null)
            .orderToSend2(null)
            .orderToSend3(null)
            .orderToSend4(null)
            .orderToSend5(null)
            .orderToSend6(null)
            .orderToSend7(null)
            .orderToSend8(null)
            .orderToSend9(null)
            .build()
        );
    }

    @Test
    void testExistingHearingOrderBundleDraftWithC21() {

        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder = UploadDraftOrdersData.builder();

        Element<HearingOrder> c21 = element(HearingOrder.builder()
            .order(DOCUMENT_REFERENCE_0)
            .type(HearingOrderType.C21)
            .build());

        underTest.build(SELECTED_HEARING_ID, CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(element(UUID_1, HearingOrdersBundle.builder()
                    .hearingId(SELECTED_HEARING_ID)
                    .orders(Lists.newArrayList(c21))
                    .build())))
                .build(), UploadDraftOrdersData.builder().build(),
            newEventDataBuilder);

        assertThat(newEventDataBuilder.build()).isEqualTo(UploadDraftOrdersData.builder()
            .currentHearingOrderDrafts(Lists.newArrayList(c21))
            .orderToSendOptionCount("0")
            .orderToSend0(DOCUMENT_REFERENCE_0)
            .orderToSend1(null)
            .orderToSend2(null)
            .orderToSend3(null)
            .orderToSend4(null)
            .orderToSend5(null)
            .orderToSend6(null)
            .orderToSend7(null)
            .orderToSend8(null)
            .orderToSend9(null)
            .build()
        );
    }

    @Test
    void testExistingHearingOrderBundleDraftWithMultipleC21() {

        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder = UploadDraftOrdersData.builder();

        Element<HearingOrder> c21 = element(HearingOrder.builder()
            .order(DOCUMENT_REFERENCE_0)
            .type(HearingOrderType.C21)
            .build());

        Element<HearingOrder> anotherC21 = element(HearingOrder.builder()
            .order(DOCUMENT_REFERENCE_1)
            .type(HearingOrderType.C21)
            .build());

        underTest.build(SELECTED_HEARING_ID, CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(element(UUID_1, HearingOrdersBundle.builder()
                    .hearingId(SELECTED_HEARING_ID)
                    .orders(Lists.newArrayList(c21, anotherC21))
                    .build())))
                .build(), UploadDraftOrdersData.builder().build(),
            newEventDataBuilder);

        assertThat(newEventDataBuilder.build()).isEqualTo(UploadDraftOrdersData.builder()
            .currentHearingOrderDrafts(Lists.newArrayList(c21, anotherC21))
            .orderToSendOptionCount("01")
            .orderToSend0(DOCUMENT_REFERENCE_0)
            .orderToSend1(DOCUMENT_REFERENCE_1)
            .orderToSend2(null)
            .orderToSend3(null)
            .orderToSend4(null)
            .orderToSend5(null)
            .orderToSend6(null)
            .orderToSend7(null)
            .orderToSend8(null)
            .orderToSend9(null)
            .build()
        );
    }

    @Test
    void testExistingHearingOrderBundleDraftWithMixedC21AndCMOs() {

        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder = UploadDraftOrdersData.builder();

        Element<HearingOrder> c21 = element(HearingOrder.builder()
            .order(DOCUMENT_REFERENCE_0)
            .type(HearingOrderType.C21)
            .build());

        Element<HearingOrder> cmo = element(HearingOrder.builder()
            .order(DOCUMENT_REFERENCE_1)
            .type(HearingOrderType.AGREED_CMO)
            .build());

        underTest.build(SELECTED_HEARING_ID, CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(element(UUID_1, HearingOrdersBundle.builder()
                    .hearingId(SELECTED_HEARING_ID)
                    .orders(Lists.newArrayList(c21, cmo))
                    .build())))
                .build(), UploadDraftOrdersData.builder().build(),
            newEventDataBuilder);

        assertThat(newEventDataBuilder.build()).isEqualTo(UploadDraftOrdersData.builder()
            .currentHearingOrderDrafts(Lists.newArrayList(c21))
            .orderToSendOptionCount("0")
            .orderToSend0(DOCUMENT_REFERENCE_0)
            .orderToSend2(null)
            .orderToSend3(null)
            .orderToSend4(null)
            .orderToSend5(null)
            .orderToSend6(null)
            .orderToSend7(null)
            .orderToSend8(null)
            .orderToSend9(null)
            .build()
        );
    }

    @Test
    void testExistingDraftsSetInEventWithMaxNumberOfDocuments() {
        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder = UploadDraftOrdersData.builder();

        List<Element<HearingOrder>> orderDrafts = List.of(
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_0).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_1).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_2).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_3).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_4).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_5).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_6).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_7).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_8).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_9).build())
        );

        underTest.build(SELECTED_HEARING_ID, CaseData.builder().build(), UploadDraftOrdersData.builder()
                .currentHearingOrderDrafts(orderDrafts).build(),
            newEventDataBuilder);

        assertThat(newEventDataBuilder.build()).isEqualTo(UploadDraftOrdersData.builder()
            .currentHearingOrderDrafts(orderDrafts)
            .orderToSendOptionCount("0123456789")
            .orderToSend0(DOCUMENT_REFERENCE_0)
            .orderToSend1(DOCUMENT_REFERENCE_1)
            .orderToSend2(DOCUMENT_REFERENCE_2)
            .orderToSend3(DOCUMENT_REFERENCE_3)
            .orderToSend4(DOCUMENT_REFERENCE_4)
            .orderToSend5(DOCUMENT_REFERENCE_5)
            .orderToSend6(DOCUMENT_REFERENCE_6)
            .orderToSend7(DOCUMENT_REFERENCE_7)
            .orderToSend8(DOCUMENT_REFERENCE_8)
            .orderToSend9(DOCUMENT_REFERENCE_9)
            .build()
        );
    }

    @Test
    void testExistingDraftsSetInEventWithMaxNumberOfDocumentsMissingDocRef() {
        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder = UploadDraftOrdersData.builder();

        List<Element<HearingOrder>> orderDrafts = List.of(
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_0).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_1).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_2).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_3).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_4).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_5).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_6).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_7).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_8).build()),
            element(HearingOrder.builder().build())
        );

        underTest.build(SELECTED_HEARING_ID, CaseData.builder().build(), UploadDraftOrdersData.builder()
                .currentHearingOrderDrafts(orderDrafts).build(),
            newEventDataBuilder);

        assertThat(newEventDataBuilder.build()).isEqualTo(UploadDraftOrdersData.builder()
            .currentHearingOrderDrafts(orderDrafts)
            .orderToSendOptionCount("0123456789")
            .orderToSend0(DOCUMENT_REFERENCE_0)
            .orderToSend1(DOCUMENT_REFERENCE_1)
            .orderToSend2(DOCUMENT_REFERENCE_2)
            .orderToSend3(DOCUMENT_REFERENCE_3)
            .orderToSend4(DOCUMENT_REFERENCE_4)
            .orderToSend5(DOCUMENT_REFERENCE_5)
            .orderToSend6(DOCUMENT_REFERENCE_6)
            .orderToSend7(DOCUMENT_REFERENCE_7)
            .orderToSend8(DOCUMENT_REFERENCE_8)
            .orderToSend9(null)
            .build()
        );

    }

    @Test
    void testExistingDraftsSetInEventWithLessThanMaxNumberOfDocuments() {
        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder = UploadDraftOrdersData.builder();

        List<Element<HearingOrder>> orderDrafts = List.of(
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_0).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_1).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_2).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_3).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_4).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_5).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_6).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_7).build()),
            element(HearingOrder.builder().order(DOCUMENT_REFERENCE_8).build())
        );

        underTest.build(SELECTED_HEARING_ID, CaseData.builder().build(), UploadDraftOrdersData.builder()
                .currentHearingOrderDrafts(orderDrafts).build(),
            newEventDataBuilder);

        assertThat(newEventDataBuilder.build()).isEqualTo(UploadDraftOrdersData.builder()
            .currentHearingOrderDrafts(orderDrafts)
            .orderToSendOptionCount("012345678")
            .orderToSend0(DOCUMENT_REFERENCE_0)
            .orderToSend1(DOCUMENT_REFERENCE_1)
            .orderToSend2(DOCUMENT_REFERENCE_2)
            .orderToSend3(DOCUMENT_REFERENCE_3)
            .orderToSend4(DOCUMENT_REFERENCE_4)
            .orderToSend5(DOCUMENT_REFERENCE_5)
            .orderToSend6(DOCUMENT_REFERENCE_6)
            .orderToSend7(DOCUMENT_REFERENCE_7)
            .orderToSend8(DOCUMENT_REFERENCE_8)
            .orderToSend9(null)
            .build()
        );
    }
}
