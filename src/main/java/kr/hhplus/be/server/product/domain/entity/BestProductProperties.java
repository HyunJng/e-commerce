package kr.hhplus.be.server.product.domain.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.best-products")
public class BestProductProperties {

    private Integer aggregatePastCandidate;
    private Integer aggregateCurrentCandidate;
    private Integer aggregateDays;
    private Integer topCount;

}
