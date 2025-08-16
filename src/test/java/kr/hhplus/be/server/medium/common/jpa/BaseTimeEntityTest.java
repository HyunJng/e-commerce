package kr.hhplus.be.server.medium.common.jpa;

import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BaseTimeEntityTest extends AbstractIntegrationTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    void BaseTimeEntity를_상속하면_자동으로_시간정보가_입력된다() throws Exception {
        // given
        Product product = new Product(null, "상품1", 1000L, 10);

        // when
        Product result = productJpaRepository.save(product);

        // then
        assertThat(result.getCreateAt()).isNotNull();
        assertThat(result.getUpdateAt()).isNotNull();
    }

}