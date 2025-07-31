package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProductDetailService {

    public record Input(Long productId) {
    }

    public record Output(Long id, String name, Long price, Integer quantity) {

        public Output(Product product) {
            this(product.getId(), product.getName(), product.getPrice(), product.getQuantity());
        }
    }

    private final ProductJpaRepository productJpaRepository;

    public Output execute(Input input) {
        Product product = productJpaRepository.findById(input.productId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "상품"));

        return new Output(product);
    }
}
