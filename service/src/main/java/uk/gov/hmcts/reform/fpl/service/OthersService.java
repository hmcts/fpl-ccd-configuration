package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Others;

import java.util.concurrent.atomic.AtomicInteger;

import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class OthersService {
    private static String EMPTY_PLACEHOLDER = "BLANK - Please complete";

    public String buildOthersLabel(Others others) {
        StringBuilder sb = new StringBuilder();

        if (otherExists(others)) {
            if (isNotEmpty(others.getFirstOther())) {
                sb.append("Person 1")
                    .append(" ")
                    .append("-")
                    .append(" ")
                    .append(defaultIfNull(others.getFirstOther().getName(), EMPTY_PLACEHOLDER))
                    .append("\n");
            }

            if (isNotEmpty(others.getAdditionalOthers())) {
                AtomicInteger i = new AtomicInteger(1);

                others.getAdditionalOthers().forEach(other -> {
                    sb.append("Other person")
                        .append(" ")
                        .append(i)
                        .append(" ")
                        .append("-")
                        .append(" ")
                        .append(defaultIfNull(other.getValue().getName(), EMPTY_PLACEHOLDER))
                        .append("\n");

                    i.incrementAndGet();
                });
            }

        } else {
            sb.append("No others on the case");
        }

        return sb.toString();
    }

    private boolean otherExists(Others others) {
        return isNotEmpty(others) && isNotEmpty(others.getFirstOther())
            || isNotEmpty(others) && isNotEmpty(others.getAdditionalOthers());
    }
}
