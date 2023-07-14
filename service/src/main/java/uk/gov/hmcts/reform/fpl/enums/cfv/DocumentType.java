package uk.gov.hmcts.reform.fpl.enums.cfv;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@AllArgsConstructor
public enum DocumentType {
    BUNDLE("Bundle", courtBundleResolver(), true, true, true, 10),
    CASE_SUMMARY("Case Summary", standardResolver("caseSummaryList"), true, true, true, 20),
    THRESHOLD("Threshold", standardResolver("thresholdList"), true, true, true, 30),
    SKELETON_ARGUMENTS("Skeleton arguments", standardResolver("skeletonArgumentList"), true, true, true, 40);

    @Getter
    private String description;
    @Getter
    private Function<ConfidentialLevel, String> baseFieldNameResolver;
    @Getter
    private boolean uploadable;
    @Getter
    private boolean uploadableByLA;
    @Getter
    private boolean uploadableByCTSC;
    @Getter
    private final int displayOrder;

    private static final Function<ConfidentialLevel, String> courtBundleResolver() {
        return confidentialLevel -> {
            switch (confidentialLevel) {
                case NON_CONFIDENTIAL:
                    return "courtBundleListV2";
                case LA:
                case CTSC:
                    return standardNaming(confidentialLevel, "courtBundleList");
                default:
                    throw new IllegalArgumentException("unrecognised confidential level:" + confidentialLevel);
            }
        };
    }

    private static final String standardNaming(ConfidentialLevel confidentialLevel, String baseFieldName) {
        switch (confidentialLevel) {
            case NON_CONFIDENTIAL:
                return baseFieldName;
            case LA:
                return baseFieldName + "LA";
            case CTSC:
                return baseFieldName + "CTSC";
            default:
                throw new IllegalArgumentException("unrecognised confidential level:" + confidentialLevel);
        }
    }

    private static final Function<ConfidentialLevel, String> standardResolver(String baseFieldName) {
        return confidentialLevel -> standardNaming(confidentialLevel, baseFieldName);
    }
}
