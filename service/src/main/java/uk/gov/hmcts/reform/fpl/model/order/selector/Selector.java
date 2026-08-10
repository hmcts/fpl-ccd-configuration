package uk.gov.hmcts.reform.fpl.model.order.selector;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.fpl.model.HearingSelector0;
import uk.gov.hmcts.reform.fpl.model.HearingSelector1;
import uk.gov.hmcts.reform.fpl.model.HearingSelector2;
import uk.gov.hmcts.reform.fpl.model.HearingSelector3;
import uk.gov.hmcts.reform.fpl.model.HearingSelector4;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HearingSelector", generate = true)
@Data
@Builder
public class Selector {
    @CCD(ignore = true)
    @Builder.Default
    private String count = "";

    @CCD(ignore = true)
    @Builder.Default
    protected List<Integer> selected = new ArrayList<>();

    @CCD(ignore = true)
    @Builder.Default
    protected List<Integer> hidden = new ArrayList<>();

    public Selector setNumberOfOptions(Integer max) {
        setCount(IntStream.rangeClosed(1, defaultIfNull(max, 0))
            .mapToObj(Integer::toString)
            .collect(joining()));
        return this;
    }

    public Selector setNumberOfOptions(Integer min, Integer max) {
        setCount(IntStream.rangeClosed(min, defaultIfNull(max, 0))
            .mapToObj(Integer::toString)
            .collect(joining()));
        return this;
    }

    public static Selector newSelector(Integer size) {
        return Selector.builder().build().setNumberOfOptions(size);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "placeholder", showCondition = "optionCount=\"DO NOT SHOW\"")
  private String optionCount;
  @CCD(label = "HHearing 1", showCondition = "optionCount=\"HIDDEN FIELD FOR SHOW HIDE\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo option0Hidden;
  @CCD(label = "HHearing 2", showCondition = "optionCount=\"HIDDEN FIELD FOR SHOW HIDE\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo option1Hidden;
  @CCD(label = "HHearing 3", showCondition = "optionCount=\"HIDDEN FIELD FOR SHOW HIDE\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo option2Hidden;
  @CCD(label = "HHearing 4", showCondition = "optionCount=\"HIDDEN FIELD FOR SHOW HIDE\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo option3Hidden;
  @CCD(label = "HHearing 5", showCondition = "optionCount=\"HIDDEN FIELD FOR SHOW HIDE\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo option4Hidden;
  @CCD(label = " ", showCondition = "optionCount=\"1*\" AND option0Hidden!=\"Yes\"")
  private java.util.Set<HearingSelector0> option0;
  @CCD(label = " ", showCondition = "optionCount=\"12*\" AND option1Hidden!=\"Yes\"")
  private java.util.Set<HearingSelector1> option1;
  @CCD(label = " ", showCondition = "optionCount=\"123*\" AND option2Hidden!=\"Yes\"")
  private java.util.Set<HearingSelector2> option2;
  @CCD(label = " ", showCondition = "optionCount=\"1234*\" AND option3Hidden!=\"Yes\"")
  private java.util.Set<HearingSelector3> option3;
  @CCD(label = " ", showCondition = "optionCount=\"12345*\" AND option4Hidden!=\"Yes\"")
  private java.util.Set<HearingSelector4> option4;
  // ==== end synthesised definition-only fields ====
}
