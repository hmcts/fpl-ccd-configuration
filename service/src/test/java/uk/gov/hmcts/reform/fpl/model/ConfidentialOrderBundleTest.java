package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

class ConfidentialOrderBundleTest {

    static ConfidentialGeneratedOrders buildTestConfidentialOrderBundle() {
        return ConfidentialGeneratedOrders.builder()
            .orderCollectionCTSC(buildSampleGeneratedOrder("CTSC"))
            .orderCollectionLA(buildSampleGeneratedOrder("LA"))
            .orderCollectionResp0(buildSampleGeneratedOrder("Resp0"))
            .orderCollectionResp1(buildSampleGeneratedOrder("Resp1"))
            .orderCollectionResp2(buildSampleGeneratedOrder("Resp2"))
            .orderCollectionResp3(buildSampleGeneratedOrder("Resp3"))
            .orderCollectionResp4(buildSampleGeneratedOrder("Resp4"))
            .orderCollectionResp5(buildSampleGeneratedOrder("Resp5"))
            .orderCollectionResp6(buildSampleGeneratedOrder("Resp6"))
            .orderCollectionResp7(buildSampleGeneratedOrder("Resp7"))
            .orderCollectionResp8(buildSampleGeneratedOrder("Resp8"))
            .orderCollectionResp9(buildSampleGeneratedOrder("Resp9"))
            .orderCollectionChild0(buildSampleGeneratedOrder("Child0"))
            .orderCollectionChild1(buildSampleGeneratedOrder("Child1"))
            .orderCollectionChild2(buildSampleGeneratedOrder("Child2"))
            .orderCollectionChild3(buildSampleGeneratedOrder("Child3"))
            .orderCollectionChild4(buildSampleGeneratedOrder("Child4"))
            .orderCollectionChild5(buildSampleGeneratedOrder("Child5"))
            .orderCollectionChild6(buildSampleGeneratedOrder("Child6"))
            .orderCollectionChild7(buildSampleGeneratedOrder("Child7"))
            .orderCollectionChild8(buildSampleGeneratedOrder("Child8"))
            .orderCollectionChild9(buildSampleGeneratedOrder("Child9"))
            .orderCollectionChild10(buildSampleGeneratedOrder("Child10"))
            .orderCollectionChild11(buildSampleGeneratedOrder("Child11"))
            .orderCollectionChild12(buildSampleGeneratedOrder("Child12"))
            .orderCollectionChild13(buildSampleGeneratedOrder("Child13"))
            .orderCollectionChild14(buildSampleGeneratedOrder("Child14"))
            .build();
    }

    static List<Element<GeneratedOrder>> buildSampleGeneratedOrder(String name) {
        return wrapElementsWithUUIDs(GeneratedOrder.builder().title(name).build());
    }

    @Test
    void shouldReturnBaseFieldName() {
        assertThat(ConfidentialRefusedOrders.builder().build().getFieldBaseName())
            .isEqualTo("refusedHearingOrders");
        assertThat(ConfidentialGeneratedOrders.builder().build().getFieldBaseName())
            .isEqualTo("orderCollection");
        assertThat(HearingOrdersBundle.builder().build().getFieldBaseName())
            .isEqualTo("orders");
    }

    @Test
    void shouldReturnGetterBaseName() {
        assertThat(ConfidentialRefusedOrders.builder().build().getGetterBaseName())
            .isEqualTo("getRefusedHearingOrders");
        assertThat(ConfidentialGeneratedOrders.builder().build().getGetterBaseName())
            .isEqualTo("getOrderCollection");
        assertThat(HearingOrdersBundle.builder().build().getGetterBaseName())
            .isEqualTo("getOrders");
    }

    @Test
    void shouldReturnSetterBaseName() {
        assertThat(ConfidentialRefusedOrders.builder().build().getSetterBaseName())
            .isEqualTo("setRefusedHearingOrders");
        assertThat(ConfidentialGeneratedOrders.builder().build().getSetterBaseName())
            .isEqualTo("setOrderCollection");
        assertThat(HearingOrdersBundle.builder().build().getSetterBaseName())
            .isEqualTo("setOrders");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        ConfidentialOrderBundle.SUFFIX_CTSC, ConfidentialOrderBundle.SUFFIX_LA,
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "0", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "1",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "2", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "3",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "4", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "5",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "6", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "7",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "8", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "9",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "8", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "9",
        ConfidentialOrderBundle.SUFFIX_CHILD + "0", ConfidentialOrderBundle.SUFFIX_CHILD + "1",
        ConfidentialOrderBundle.SUFFIX_CHILD + "2", ConfidentialOrderBundle.SUFFIX_CHILD + "3",
        ConfidentialOrderBundle.SUFFIX_CHILD + "4", ConfidentialOrderBundle.SUFFIX_CHILD + "5",
        ConfidentialOrderBundle.SUFFIX_CHILD + "6", ConfidentialOrderBundle.SUFFIX_CHILD + "7",
        ConfidentialOrderBundle.SUFFIX_CHILD + "8", ConfidentialOrderBundle.SUFFIX_CHILD + "9",
        ConfidentialOrderBundle.SUFFIX_CHILD + "10", ConfidentialOrderBundle.SUFFIX_CHILD + "11",
        ConfidentialOrderBundle.SUFFIX_CHILD + "12", ConfidentialOrderBundle.SUFFIX_CHILD + "13",
        ConfidentialOrderBundle.SUFFIX_CHILD + "14"
    })
    void shouldReturnConfidentialOrdersBySuffix(String suffix) {
        ConfidentialGeneratedOrders test = buildTestConfidentialOrderBundle();

        List<Element<GeneratedOrder>> expected;
        switch (suffix) {
            case ConfidentialOrderBundle.SUFFIX_CTSC:
                expected = test.getOrderCollectionCTSC();
                break;
            case ConfidentialOrderBundle.SUFFIX_LA:
                expected = test.getOrderCollectionLA();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "0":
                expected = test.getOrderCollectionResp0();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "1":
                expected = test.getOrderCollectionResp1();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "2":
                expected = test.getOrderCollectionResp2();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "3":
                expected = test.getOrderCollectionResp3();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "4":
                expected = test.getOrderCollectionResp4();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "5":
                expected = test.getOrderCollectionResp5();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "6":
                expected = test.getOrderCollectionResp6();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "7":
                expected = test.getOrderCollectionResp7();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "8":
                expected = test.getOrderCollectionResp8();
                break;
            case ConfidentialOrderBundle.SUFFIX_RESPONDENT + "9":
                expected = test.getOrderCollectionResp9();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "0":
                expected = test.getOrderCollectionChild0();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "1":
                expected = test.getOrderCollectionChild1();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "2":
                expected = test.getOrderCollectionChild2();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "3":
                expected = test.getOrderCollectionChild3();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "4":
                expected = test.getOrderCollectionChild4();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "5":
                expected = test.getOrderCollectionChild5();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "6":
                expected = test.getOrderCollectionChild6();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "7":
                expected = test.getOrderCollectionChild7();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "8":
                expected = test.getOrderCollectionChild8();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "9":
                expected = test.getOrderCollectionChild9();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "10":
                expected = test.getOrderCollectionChild10();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "11":
                expected = test.getOrderCollectionChild11();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "12":
                expected = test.getOrderCollectionChild12();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "13":
                expected = test.getOrderCollectionChild13();
                break;
            case ConfidentialOrderBundle.SUFFIX_CHILD + "14":
                expected = test.getOrderCollectionChild14();
                break;
            default:
                expected = null;
        }

        assertThat(test.getConfidentialOrdersBySuffix(suffix)).containsAll(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        ConfidentialOrderBundle.SUFFIX_CTSC, ConfidentialOrderBundle.SUFFIX_LA,
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "0", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "1",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "2", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "3",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "4", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "5",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "6", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "7",
        ConfidentialOrderBundle.SUFFIX_RESPONDENT + "8", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "9",
        ConfidentialOrderBundle.SUFFIX_CHILD + "0", ConfidentialOrderBundle.SUFFIX_CHILD + "1",
        ConfidentialOrderBundle.SUFFIX_CHILD + "2", ConfidentialOrderBundle.SUFFIX_CHILD + "3",
        ConfidentialOrderBundle.SUFFIX_CHILD + "4", ConfidentialOrderBundle.SUFFIX_CHILD + "5",
        ConfidentialOrderBundle.SUFFIX_CHILD + "6", ConfidentialOrderBundle.SUFFIX_CHILD + "7",
        ConfidentialOrderBundle.SUFFIX_CHILD + "8", ConfidentialOrderBundle.SUFFIX_CHILD + "9",
        ConfidentialOrderBundle.SUFFIX_CHILD + "10", ConfidentialOrderBundle.SUFFIX_CHILD + "11",
        ConfidentialOrderBundle.SUFFIX_CHILD + "12", ConfidentialOrderBundle.SUFFIX_CHILD + "13",
        ConfidentialOrderBundle.SUFFIX_CHILD + "14"
    })
    void shouldSetConfidentialOrdersBySuffix(String suffix) {
        ConfidentialGeneratedOrders test = ConfidentialGeneratedOrders.builder().build();
        List<Element<GeneratedOrder>> orders = buildSampleGeneratedOrder(suffix);
        test.setConfidentialOrdersBySuffix(suffix, orders);
        assertThat(test.getConfidentialOrdersBySuffix(suffix)).containsAll(orders);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnAllConfidentialOrders() {
        ConfidentialGeneratedOrders test = buildTestConfidentialOrderBundle();

        List<Element<GeneratedOrder>> expectedAllOrders = Arrays.stream(ConfidentialGeneratedOrders.class.getMethods())
            .filter(method -> method.getName().contains(test.getGetterBaseName()))
            .map(method -> {
                try {
                    return (List<Element<GeneratedOrder>>) method.invoke(test);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            })
            .flatMap(List::stream)
            .toList();

        assertThat(test.getAllConfidentialOrders()).containsAll(expectedAllOrders);
    }

    @Test
    void shouldProcessAllConfidentialField() {
        ConfidentialGeneratedOrders test = buildTestConfidentialOrderBundle();

        List<String> expected = List.of(
            ConfidentialOrderBundle.SUFFIX_CTSC, ConfidentialOrderBundle.SUFFIX_LA,
            ConfidentialOrderBundle.SUFFIX_RESPONDENT + "0", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "1",
            ConfidentialOrderBundle.SUFFIX_RESPONDENT + "2", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "3",
            ConfidentialOrderBundle.SUFFIX_RESPONDENT + "4", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "5",
            ConfidentialOrderBundle.SUFFIX_RESPONDENT + "6", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "7",
            ConfidentialOrderBundle.SUFFIX_RESPONDENT + "8", ConfidentialOrderBundle.SUFFIX_RESPONDENT + "9",
            ConfidentialOrderBundle.SUFFIX_CHILD + "0", ConfidentialOrderBundle.SUFFIX_CHILD + "1",
            ConfidentialOrderBundle.SUFFIX_CHILD + "2", ConfidentialOrderBundle.SUFFIX_CHILD + "3",
            ConfidentialOrderBundle.SUFFIX_CHILD + "4", ConfidentialOrderBundle.SUFFIX_CHILD + "5",
            ConfidentialOrderBundle.SUFFIX_CHILD + "6", ConfidentialOrderBundle.SUFFIX_CHILD + "7",
            ConfidentialOrderBundle.SUFFIX_CHILD + "8", ConfidentialOrderBundle.SUFFIX_CHILD + "9",
            ConfidentialOrderBundle.SUFFIX_CHILD + "10", ConfidentialOrderBundle.SUFFIX_CHILD + "11",
            ConfidentialOrderBundle.SUFFIX_CHILD + "12", ConfidentialOrderBundle.SUFFIX_CHILD + "13",
            ConfidentialOrderBundle.SUFFIX_CHILD + "14");

        List<String> actual = new ArrayList<>();
        test.processAllConfidentialOrders((suffix, orders) -> actual.add(suffix));
        assertThat(actual).containsAll(expected);
    }
}
