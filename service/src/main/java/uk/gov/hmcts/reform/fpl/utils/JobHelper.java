package uk.gov.hmcts.reform.fpl.utils;

import java.math.BigDecimal;

import static java.math.RoundingMode.UP;
import static uk.gov.hmcts.reform.fpl.service.search.SearchService.ES_DEFAULT_SIZE;

public class JobHelper {

    private JobHelper() {}

    public static String buildStats(int total, int skipped, int updated, int failed) {
        double percentUpdated = updated * 100.0 / total;
        double percentSkipped = skipped * 100.0 / total;
        double percentFailed = failed * 100.0 / total;

        return String.format("total cases: %1$d, "
                + "updated cases: %2$d/%1$d (%5$.0f%%), "
                + "skipped cases: %3$d/%1$d (%6$.0f%%), "
                + "failed cases: %4$d/%1$d (%7$.0f%%)",
            total, updated, skipped, failed, percentUpdated, percentSkipped, percentFailed
        );
    }

    public static int paginate(int total) {
        return new BigDecimal(total).divide(new BigDecimal(ES_DEFAULT_SIZE), UP).intValue();
    }

}
