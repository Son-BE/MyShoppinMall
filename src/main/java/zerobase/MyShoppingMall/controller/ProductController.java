//package zerobase.weather.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import zerobase.weather.dto.product.ProductCreateRequestDTO;
//import zerobase.weather.dto.product.ProductResponseDTO;
//import zerobase.weather.service.ProductService;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/api/products")
//public class ProductController {
//    private final ProductService productService;
//
//    //상품 생성
//    @PostMapping
//    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductCreateRequestDTO requestDTO) {
//        ProductResponseDTO response = productService.createProduct(requestDTO);
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }
//
//    //단품 조회
//    @GetMapping("/{id}")
//    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id) {
//        ProductResponseDTO response = productService.getProduct(id);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    //전체 조회
//    @GetMapping
//    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
//        List<ProductResponseDTO> products = productService.getAllProducts();
//        return ResponseEntity.ok(products);
//    }
//
//    //상품 삭제
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable Long id) {
//        productService.deleteProduct(id);
//        return ResponseEntity.noContent().build();
//    }
//}
