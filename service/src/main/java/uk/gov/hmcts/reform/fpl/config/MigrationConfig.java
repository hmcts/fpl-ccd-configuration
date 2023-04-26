package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.jobs.MigrationJobMode;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ExistsQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Filter;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Data
@Configuration
public class MigrationConfig {

    public final String migrationId;
    public final MigrationJobMode migrationJobMode;
    public final Map<String, Supplier<ESQuery>> migrationQueries;
    public final Map<String, List<Long>> caseIdMapping;

    public MigrationConfig(@Value("${fpl.migration.migrationId}") String migrationId,
                           @Value("${fpl.migration.migrationType}") String jobMode) {
        this.migrationId = migrationId;
        this.migrationJobMode = MigrationJobMode.valueOf(jobMode);
        this.migrationQueries = Map.of("DFPL-test", this::query1);
        // TODO - find a better way of loading this in, perhaps can borrow key vault like onboarding
        this.caseIdMapping = Map.of("DFPL-test", List.of(1678969312257533L));
    }

    public List<Long> getCaseIds() {
        return this.caseIdMapping.getOrDefault(this.migrationId, List.of());
    }

    public ESQuery query1() {
        return BooleanQuery.builder()
            .filter(Filter.builder()
                .clauses(List.of(ExistsQuery.of("data.court")))
                .build())
            .build();
    }


}
