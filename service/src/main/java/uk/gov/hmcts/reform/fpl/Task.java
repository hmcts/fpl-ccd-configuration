package uk.gov.hmcts.reform.fpl;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class Task {

    private final FplEvent event;
    private final TaskState state;
    private String hint;

    public Task withHint(String hint) {
        this.setHint(hint);
        return this;
    }

    public Optional<String> getHint() {
        return Optional.ofNullable(hint);
    }
}
