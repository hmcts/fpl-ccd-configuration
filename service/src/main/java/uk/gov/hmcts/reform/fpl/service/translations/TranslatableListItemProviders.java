package uk.gov.hmcts.reform.fpl.service.translations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableListItemProviders {

    private final TranslatableGeneratedOrderListItemProvider translatableGeneratedOrderListItemProvider;

    public List<TranslatableListItemProvider> getAll() {
        return List.of(
            translatableGeneratedOrderListItemProvider
        );
    }

}
