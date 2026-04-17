package kr.ac.hansung.cse.controller;

import jakarta.validation.Valid;
import kr.ac.hansung.cse.exception.ProductNotFoundException;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.model.ProductForm;
import kr.ac.hansung.cse.service.CategoryService;
import kr.ac.hansung.cse.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * =====================================================================
 * ProductController - 웹 요청 처리 계층 (Controller Layer)
 * =====================================================================
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService; // 카테고리 목록 조회를 위해 추가

    // 생성자 주입: ProductService와 CategoryService를 모두 주입받습니다.
    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }


    // ─────────────────────────────────────────────────────────────────
    // GET /products - 상품 목록 조회 (검색 및 카테고리 필터링 포함)
    // ─────────────────────────────────────────────────────────────────

    /**
     * @RequestParam:
     * - search: 상품명 검색 키워드
     * - categoryId: 필터링할 카테고리 ID
     * 1. 이름 검색: search 파라미터가 있을 경우 LIKE 검색 수행
     * 2. 카테고리 필터: categoryId가 있을 경우 해당 카테고리 상품만 필터링
     * 3. 드롭다운 데이터: 검색 창의 카테고리 목록 구성을 위해 모든 카테고리 정보를 전달
     */
    @GetMapping
    public String listProducts(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "categoryId", required = false) Long categoryId,
                               Model model) {

        // 검색어와 카테고리 ID를 이용한 복합 검색 수행
        List<Product> products = productService.searchProducts(search, categoryId);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories()); // 검색창 드롭다운용

        // 검색 상태 유지를 위해 검색어와 선택된 카테고리 ID를 다시 전달 (UI 유지)
        model.addAttribute("searchKeyword", search);
        model.addAttribute("selectedCatId", categoryId);

        return "productList";
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /products/{id} - 상품 상세 조회
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        model.addAttribute("product", product);
        return "productView";
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /products/create - 상품 등록 폼 표시
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        return "productForm";
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /products/create - 상품 등록 처리
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "productForm";
        }

        Product product = productForm.toEntity();
        product.setCategory(productService.resolveCategory(productForm.getCategory()));
        Product savedProduct = productService.createProduct(product);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + savedProduct.getName() + "' 상품이 성공적으로 등록되었습니다.");

        return "redirect:/products";
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /products/{id}/edit - 상품 수정 폼 표시
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        model.addAttribute("productForm", ProductForm.from(product));
        return "productEditForm";
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /products/{id}/edit - 상품 수정 처리
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "productEditForm";
        }

        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(productForm.getName());
        product.setCategory(productService.resolveCategory(productForm.getCategory()));
        product.setPrice(productForm.getPrice());
        product.setDescription(productForm.getDescription());

        productService.updateProduct(product);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + product.getName() + "' 상품 정보가 수정되었습니다.");
        return "redirect:/products/" + id;
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /products/{id}/delete - 상품 삭제 처리
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String productName = product.getName();
        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + productName + "' 상품이 삭제되었습니다.");
        return "redirect:/products";
    }
}