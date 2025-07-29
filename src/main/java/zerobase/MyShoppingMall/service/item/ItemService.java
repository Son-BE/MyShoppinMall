package zerobase.MyShoppingMall.service.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.repository.cart.CartItemRepository;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.wishList.WishListRepository;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final S3UploadService s3UploadService;
    private final CartItemRepository cartItemRepository;
    private final WishListRepository wishListRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String ITEM_CACHE_PREFIX = "item:";

    // 캐시 저장
    private void cacheItem(ItemResponseDto itemDto) {
        String key = ITEM_CACHE_PREFIX + itemDto.getId();
        redisTemplate.opsForValue().set(key, itemDto, 30, TimeUnit.MINUTES);
        log.info("아이템 캐시에 저장 완료, key: {}", key);
    }

    // 캐시 조회
    public ItemResponseDto getCachedItem(Long itemId) {
        String key = ITEM_CACHE_PREFIX + itemId;
        Object cached = redisTemplate.opsForValue().get(key);

        try {
            if (cached != null) {
                if (cached instanceof ItemResponseDto) {
                    log.info("캐시에서 아이템 조회 성공, itemId: {}", itemId);
                    return (ItemResponseDto) cached;
                } else {
                    ItemResponseDto dto = objectMapper.convertValue(cached, ItemResponseDto.class);
                    log.info("캐시에서 아이템 조회 성공 (ObjectMapper 변환), itemId: {}", itemId);
                    return dto;
                }
            }

            log.info("캐시에 아이템 없음, DB에서 조회 itemId: {}", itemId);
            ItemResponseDto itemDto = getItem(itemId);
            cacheItem(itemDto);
            return itemDto;
        } catch (Exception e) {
            log.error("getCachedItem() 실패 - itemId: {}, 예외: {}", itemId, e.getMessage(), e);
            throw new RuntimeException("아이템 조회 중 오류 발생", e);
        }
    }

    private void evictCache(Long itemId) {
        String key = ITEM_CACHE_PREFIX + itemId;
        redisTemplate.delete(key);
    }


    //상품 생성
//    public ItemResponseDto createItem(ItemRequestDto dto, MultipartFile imageFile) {
    public ItemResponseDto createItem(ItemRequestDto dto) throws IOException {
        String imageUrl = null;
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            imageUrl = s3UploadService.uploadFile(dto.getImageFile());
        }

        Item item = Item.builder()
                .itemName(dto.getItemName())
                .itemComment(dto.getItemComment())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .deleteType('N')
                .createdAt(LocalDateTime.now())
                .category(dto.getCategory())
                .subCategory(dto.getSubCategory())
                .gender(dto.getGender())
                .imageUrl(imageUrl)
                .build();
        itemRepository.save(item);

//        if (imageFile != null && !imageFile.isEmpty()) {
//            try {
//                itemImageService.saveItemImage(item.getId(), imageFile);
//            } catch (IOException e) {
//                throw new RuntimeException("이미지 저장 중 오류 발생", e);
//            }
//        }

        ItemResponseDto responseDto = ItemResponseDto.fromEntity(item);
        cacheItem(responseDto); // 생성 후 캐싱
        return responseDto;
    }


    //상품 수정
    @Transactional
    public ItemResponseDto updateItemWithImage(Long itemId, ItemRequestDto dto, MultipartFile imageFile) throws IOException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));

        item.setItemName(dto.getItemName());
        item.setItemComment(dto.getItemComment());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setUpdatedAt(LocalDateTime.now());
        item.setGender(dto.getGender());
        item.setCategory(dto.getCategory());
        item.setSubCategory(dto.getSubCategory());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = s3UploadService.uploadFile(imageFile);
            item.setImageUrl(imageUrl);
        }

        ItemResponseDto updatedDto = ItemResponseDto.fromEntity(item);
        cacheItem(updatedDto); // 수정 후 캐싱 갱신
        return updatedDto;
    }

    //상품 삭제
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));

//        List<ItemImage> itemImages = itemImageRepository.findAllByItemId(itemId);
//
//        for (ItemImage itemImage : itemImages) {
//            if (itemImage.getItemPath() != null) {
//                File file = new File(itemImage.getItemPath());
//                if (file.exists()) {
//                    file.delete();
//                }
//            }
//            itemImageRepository.delete(itemImage);
//        }

        cartItemRepository.deleteById(itemId);
        wishListRepository.deleteById(itemId);
        itemRepository.delete(item);

        evictCache(itemId); // 삭제 후 캐시 제거
    }

    //상품 조회 (DB 직접 조회, 캐시 x)
    public ItemResponseDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));
        if (item.getDeleteType() == 'Y') {
            throw new RuntimeException("삭제된 상품입니다.");
        }
        return ItemResponseDto.fromEntity(item);
    }

    // 상품 조회 (캐시 우선 조회)
    public ItemResponseDto getItemWithCache(Long itemId, Long memberId) {
        try {
            ItemResponseDto dto = getCachedItem(itemId);
            log.info("getItemWithCache() → itemId: {} 정상 조회 완료", itemId);
            boolean isWish = wishListRepository.existsByMemberIdAndItemId(memberId, itemId);
            dto.setIsWish(isWish);

            return dto;
        } catch (Exception e) {
            log.error("getItemWithCache() → itemId: {} 조회 중 예외 발생: {}", itemId, e.getMessage(), e);
            throw e;
        }
    }


    //상품 전체 조회 (캐싱 없이 DB 직접 조회)
    public List<ItemResponseDto> getAllItems() {
        List<Item> items = itemRepository.findAll()
                .stream()
                .filter(item -> item.getDeleteType() == 'N')
                .collect(Collectors.toList());

        return items.stream()
                .map(ItemResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    //관리자 페이지 카테고리화
    @Transactional
    public Page<ItemResponseDto> getItemsByCategory(ItemCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return itemRepository.findByCategory(category, pageable)
                .map(ItemResponseDto::fromEntity);
    }

    //관리자 페이지 페이징처리
    public Page<ItemResponseDto> getAllItemsPageable(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Item> itemsPage = itemRepository.findAll(pageable);
        return itemsPage.map(ItemResponseDto::fromEntity);
    }


    public Page<ItemResponseDto> findItems(Gender gender, String sortType, String itemSubCategory, int page, int size) {
        Sort sort;

        switch (sortType.toLowerCase()) {
            case "price low":
                sort = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "price high":
                sort = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "score high":
                sort = Sort.by(Sort.Direction.DESC, "rating");
                break;
            case "most reviews":
                sort = Sort.by(Sort.Direction.DESC, "reviewCount");
                break;
            case "popular":
                sort = Sort.by(Sort.Direction.DESC, "salesVolume");
                break;
            case "latest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Item> itemsPage;

        ItemSubCategory subCategory = null;
        if (itemSubCategory != null && !itemSubCategory.isEmpty()) {
            try {
                String enumKey = "M_" + itemSubCategory.toUpperCase();
                subCategory = ItemSubCategory.valueOf(enumKey);
            } catch (IllegalArgumentException e) {
                System.err.println("[ItemService] 잘못된 서브카테고리: " + itemSubCategory);
                e.printStackTrace();
            }
        }

        if (gender != null && subCategory != null) {
            itemsPage = itemRepository.findByGenderAndSubCategory(gender, subCategory, pageable);
        } else if (gender != null) {
            itemsPage = itemRepository.findByGender(gender, pageable);
        } else if (subCategory != null) {
            itemsPage = itemRepository.findBySubCategory(subCategory, pageable);
        } else {
            itemsPage = itemRepository.findAll(pageable);
        }

        return itemsPage.map(ItemResponseDto::fromEntity);
    }

}
