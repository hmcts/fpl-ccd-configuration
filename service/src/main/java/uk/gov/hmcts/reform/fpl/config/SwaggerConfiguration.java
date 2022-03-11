package uk.gov.hmcts.reform.fpl.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("default")
@Configuration
public class SwaggerConfiguration {

// TODO - remove this before merge
//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2)
//            .useDefaultResponseMessages(false)
//            .select()
//            .apis(RequestHandlerSelectors.basePackage(Application.class.getPackage().getName() + ".controllers"))
//            .paths(PathSelectors.any())
//            .build();
//    }

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("FPL Case Service")
                    .description("FPL Case Service")
            );
    }

}
