//package zerobase.weather.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import zerobase.weather.domain.Product;
//import zerobase.weather.dto.product.ProductCreateRequestDTO;
//import zerobase.weather.dto.product.ProductResponseDTO;
//import zerobase.weather.repository.ProductRepository;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RequiredArgsConstructor
//@Service
//@Transactional
//public class ProductService {
//    private final ProductRepository productRepository;
//
//    //상품 생성
//    public ProductResponseDTO createProduct(ProductCreateRequestDTO requestDTO) {
//        Product product = Product.builder()
//                .name(requestDTO.getName())
//                .price(requestDTO.getPrice())
//                .description(requestDTO.getDescription())
//                .stock(requestDTO.getStock())
//                .build();
//
//        Product savedProduct = productRepository.save(product);
//        return convertToDTO(savedProduct);
//    }
//
//    //상품 1개 조회
//    @Transactional(readOnly = true)
//    public ProductResponseDTO getProduct(Long productId) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. id=" + productId));
//        return convertToDTO(product);
//    }
//
//    //상품 전체 조회
//    @Transactional(readOnly = true)
//    public List<ProductResponseDTO> getAllProducts() {
//        return productRepository.findAll()
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    //상품 수정
//    public ProductResponseDTO updateProduct(Long productId,ProductCreateRequestDTO requestDTO) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. 상품 id =" + productId));
//        product.setName(requestDTO.getName());
//        product.setPrice(requestDTO.getPrice());
//        product.setDescription(requestDTO.getDescription());
//        product.setStock(requestDTO.getStock());
//
//        return convertToDTO(product);
//    }
//
//    //상품삭제
//    public void deleteProduct(Long productId) {
//        productRepository.deleteById(productId);
//    }
//
//    private ProductResponseDTO convertToDTO(Product product) {
//        return ProductResponseDTO.builder()
//                .id(product.getId())
//                .name(product.getName())
//                .price(product.getPrice())
//                .description(product.getDescription())
//                .stock(product.getStock())
//                .imageUrl(product.getImageUrl())
//                .build();
//    }
//}
