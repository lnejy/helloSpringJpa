package kr.ac.hansung.cse.service;

import kr.ac.hansung.cse.model.Category;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.repository.CategoryRepository;
import kr.ac.hansung.cse.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * ProductService - 비즈니스 로직 계층 (Service Layer)
 * =====================================================================
 */
@Service
@Transactional(readOnly = true) // 클래스 기본값: 읽기 전용 트랜잭션
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * 카테고리 이름(String) → Category 엔티티 변환
     */
    public Category resolveCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) return null;
        return categoryRepository.findByName(categoryName).orElse(null);
    }

    /**
     * 모든 상품 조회
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * ID로 상품 조회
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 새 상품 등록
     */
    @Transactional
    public Product createProduct(Product product) {
        if (product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다.");
        }
        return productRepository.save(product);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public Product updateProduct(Product product) {
        if (product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다.");
        }
        return productRepository.update(product);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(id);
    }

    /**
     * 상품 검색 및 카테고리 필터링
     * * @param keyword    검색어 (상품명 포함 여부)
     * @param categoryId 필터링할 카테고리 ID
     * @return 검색 조건에 맞는 상품 리스트
     */
    public List<Product> searchProducts(String keyword, Long categoryId) {
        // Repository의 복합 검색 메서드를 호출합니다.
        return productRepository.search(keyword, categoryId);
    }
}