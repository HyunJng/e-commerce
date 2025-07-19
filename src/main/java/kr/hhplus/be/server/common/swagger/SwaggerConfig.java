package kr.hhplus.be.server.common.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("이커머스 API")
                        .version("v1")
                        .description(
                                "본 API는 항해 플러스 교육 과정 중 이커머스 도메인을 기반으로 설계된 연습용 프로젝트입니다."
                        ));
    }
}
