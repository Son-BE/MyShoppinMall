    package zerobase.MyShoppingMall.service.item;

    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
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

    import java.io.IOException;
    import java.time.LocalDateTime;
    import java.util.Collections;
    import java.util.List;
    import java.util.Objects;
    import java.util.stream.Collectors;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class ItemService {

        private final ItemRepository itemRepository;
        private final CartItemRepository cartItemRepository;
        private final WishListRepository wishListRepository;

        private final ItemCacheService itemCacheService;
        private final ItemCountService itemCountService;
        private final ItemFileService itemFileService;
        private final PaginationService paginationService;

        /**
         * 아이템 생성
         */
        @Transactional
        public ItemResponseDto createItem(ItemRequestDto dto) throws IOException {
            String imageUrl = itemFileService.uploadItemImage(dto.getImageFile());

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
                    .ageGroup(dto.getAgeGroup())
                    .style(dto.getStyle())
                    .season(dto.getSeason())
                    .imageUrl(imageUrl)
                    .primaryColor(dto.getPrimaryColor())
                    .secondaryColor(dto.getSecondaryColor())
                    .build();

            Item savedItem = itemRepository.save(item);
            ItemResponseDto responseDto = ItemResponseDto.fromEntity(savedItem);

            // 생성 후 캐싱
            itemCacheService.cacheItem(responseDto);

            log.info("아이템 생성 완료, itemId: {}, itemName: {}", savedItem.getId(), savedItem.getItemName());
            return responseDto;
        }

        /**
         * 아이템 수정 (이미지 포함)
         */
        @Transactional
        public ItemResponseDto updateItemWithImage(Long itemId, ItemRequestDto dto, MultipartFile imageFile)
                throws IOException {
            Item item = getItemEntity(itemId);

            // 이미지 교체 처리
            String newImageUrl = itemFileService.replaceItemImage(item.getImageUrl(), imageFile);

            // 아이템 정보 업데이트
            updateItemFields(item, dto, newImageUrl);

            ItemResponseDto updatedDto = ItemResponseDto.fromEntity(item);

            // 수정 후 캐싱 갱신
            itemCacheService.cacheItem(updatedDto);

            log.info("아이템 수정 완료, itemId: {}", itemId);
            return updatedDto;
        }

        /**
         * 아이템 삭제
         */
        @Transactional
        public void deleteItem(Long itemId) {
            Item item = getItemEntity(itemId);

            // 관련 데이터 삭제
            cartItemRepository.deleteByItemId(itemId);
            wishListRepository.deleteByItemId(itemId);

            // 이미지 파일 삭제
            itemFileService.deleteItemImage(item.getImageUrl());

            // 아이템 삭제
            itemRepository.delete(item);

            // 캐시에서 제거
            itemCacheService.evictCache(itemId);

            log.info("아이템 삭제 완료, itemId: {}", itemId);
        }

        /**
         * 아이템 조회 (DB 직접 조회, 캐시 사용 안함)
         */
        public ItemResponseDto getItem(Long itemId) {
            Item item = getItemEntity(itemId);
            validateItemNotDeleted(item);
            return ItemResponseDto.fromEntity(item);
        }

        /**
         * 아이템 조회 (캐시 우선 조회)
         */
        public ItemResponseDto getItemWithCache(Long itemId, Long memberId) {
            try {
                // 캐시에서 먼저 조회
                ItemResponseDto dto = itemCacheService.getCachedItem(itemId);

                if (dto == null) {
                    // 캐시에 없으면 DB에서 조회 후 캐싱
                    dto = getItem(itemId);
                    itemCacheService.cacheItem(dto);
                }

                // 위시리스트 여부 설정
                if (memberId != null) {
                    boolean isWish = wishListRepository.existsByMemberIdAndItemId(memberId, itemId);
                    dto.setIsWish(isWish);
                }

                log.info("아이템 조회 완료, itemId: {}, 캐시 사용: {}", itemId, dto != null);
                return dto;
            } catch (Exception e) {
                log.error("아이템 조회 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
                throw new RuntimeException("아이템 조회 중 오류 발생", e);
            }
        }

        /**
         * 전체 아이템 조회 (삭제되지 않은 아이템만)
         */
        public List<ItemResponseDto> getAllItems() {
            List<Item> items = itemRepository.findAll()
                    .stream()
                    .filter(item -> item.getDeleteType() == 'N')
                    .collect(Collectors.toList());

            return items.stream()
                    .map(ItemResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }

        /**
         * 아이템 조회수 증가
         */
        public void increaseViewCount(Long itemId) {
            itemCountService.increaseViewCount(itemId);
        }

        /**
         * 아이템 주문 수 증가
         */
        public void increaseOrderCount(Long itemId) {
            itemCountService.increaseOrderCount(itemId);
        }


        /**
         * 페이징된 아이템 목록 조회 (필터 포함)
         */
        public Page<ItemResponseDto> findItems(Gender gender, String sort, String subCategory, int page, int size) {
            return paginationService.findItemsWithPagination(gender, sort, subCategory, page, size);
        }

        /**
         * 카테고리별 아이템 조회 (관리자용)
         */
        public Page<ItemResponseDto> getItemsByCategory(ItemCategory category, int page, int size) {
            return paginationService.getItemsByCategoryWithPagination(category, page, size);
        }

        /**
         * 전체 아이템 페이징 조회 (관리자용)
         */
        public Page<ItemResponseDto> getAllItemsPageable(int page, int size) {
            return paginationService.getAllItemsWithPagination(page, size);
        }

        private Item getItemEntity(Long itemId) {
            return itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));
        }

        private void validateItemNotDeleted(Item item) {
            if (item.getDeleteType() == 'Y') {
                throw new RuntimeException("삭제된 상품입니다.");
            }
        }

        private void updateItemFields(Item item, ItemRequestDto dto, String imageUrl) {
            item.setItemName(dto.getItemName());
            item.setItemComment(dto.getItemComment());
            item.setPrice(dto.getPrice());
            item.setQuantity(dto.getQuantity());
            item.setUpdatedAt(LocalDateTime.now());
            item.setGender(dto.getGender());
            item.setCategory(dto.getCategory());
            item.setSubCategory(dto.getSubCategory());
            item.setAgeGroup(dto.getAgeGroup());
            item.setStyle(dto.getStyle());
            item.setSeason(dto.getSeason());
            item.setPrimaryColor(dto.getPrimaryColor());
            item.setSecondaryColor(dto.getSecondaryColor());

            if (imageUrl != null) {
                item.setImageUrl(imageUrl);
            }
        }

        public Page<ItemResponseDto> searchItems(String query, String sort, Pageable pageable) {

            Sort sortSpec = switch (sort == null ? "latest" : sort) {
                case "priceAsc"  -> Sort.by("price").ascending();
                case "priceDesc" -> Sort.by("price").descending();
                case "popular"   -> Sort.by("salesCount").descending();
                case "rating"    -> Sort.by("avgRating").descending();
                default          -> Sort.by("createdAt").descending();
            };

            Pageable pageReq = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortSpec);

            Page<Item> page = itemRepository.searchByKeyword(query, pageReq);

            return page.map(ItemResponseDto::fromEntity);
        }


    }