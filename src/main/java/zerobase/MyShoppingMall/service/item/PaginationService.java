package zerobase.MyShoppingMall.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.dto.item.neo.PaginationInfo;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaginationService {

    private final ItemRepository itemRepository;

    private static final int DEFAULT_BLOCK_SIZE = 10;

    public Page<ItemResponseDto> findItemsWithPagination(
            Gender gender,
            String sortType,
            String itemSubCategory,
            int page,
            int size) {

        Sort sort = createSortFromType(sortType);
        Pageable pageable = PageRequest.of(page, size, sort);

        ItemSubCategory subCategory = parseSubCategory(itemSubCategory);
        Page<Item> itemsPage = getItemsPageByFilters(gender, subCategory, pageable);

        log.info("아이템 페이징 조회 완료 - 필터: gender={}, subCategory={}, sort={}, page={}, size={}, 총 {}건",
                gender, subCategory, sortType, page, size, itemsPage.getTotalElements());

        return itemsPage.map(ItemResponseDto::fromEntity);
    }
    public Page<ItemResponseDto> getItemsByCategoryWithPagination(
            ItemCategory category,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Item> itemsPage = itemRepository.findByCategory(category, pageable);

        log.info("카테고리별 아이템 페이징 조회 완료 - 카테고리: {}, page: {}, size: {}, 총 {}건",
                category, page, size, itemsPage.getTotalElements());

        return itemsPage.map(ItemResponseDto::fromEntity);
    }
    public Page<ItemResponseDto> getAllItemsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Item> itemsPage = itemRepository.findAll(pageable);

        log.info("전체 아이템 페이징 조회 완료 - page: {}, size: {}, 총 {}건",
                page, size, itemsPage.getTotalElements());

        return itemsPage.map(ItemResponseDto::fromEntity);
    }
    public PaginationInfo createPaginationInfo(Page<?> page) {
        return createPaginationInfo(page, DEFAULT_BLOCK_SIZE);
    }

    public PaginationInfo createPaginationInfo(Page<?> page, int blockSize) {
        int totalPages = page.getTotalPages();
        int currentPage = page.getNumber() + 1; // 1-based

        int currentBlock = (currentPage - 1) / blockSize;
        int startPage = currentBlock * blockSize + 1;
        int endPage = Math.min(startPage + blockSize - 1, totalPages);

        List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
                .boxed()
                .collect(Collectors.toList());

        return PaginationInfo.builder()
                .currentPage(currentPage)
                .totalPages(totalPages)
                .totalElements(page.getTotalElements())
                .pageNumbers(pageNumbers)
                .hasPrevBlock(startPage > 1)
                .hasNextBlock(endPage < totalPages)
                .prevBlockPage(startPage - 2)
                .nextBlockPage(endPage)
                .startPage(startPage)
                .endPage(endPage)
                .blockSize(blockSize)
                .build();
    }
    private Sort createSortFromType(String sortType) {
        if (sortType == null || sortType.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        switch (sortType.toLowerCase()) {
            case "price low":
                return Sort.by(Sort.Direction.ASC, "price");
            case "price high":
                return Sort.by(Sort.Direction.DESC, "price");
            case "score high":
                return Sort.by(Sort.Direction.DESC, "itemRating");
            case "most reviews":
                return Sort.by(Sort.Direction.DESC, "reviewCount");
            case "popular":
                return Sort.by(Sort.Direction.DESC, "viewCount", "orderCount", "cartCount");
            case "latest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
    private ItemSubCategory parseSubCategory(String itemSubCategory) {
        if (itemSubCategory == null || itemSubCategory.isEmpty()) {
            return null;
        }

        try {
            String enumKey = "M_" + itemSubCategory.toUpperCase();
            return ItemSubCategory.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 서브카테고리: {}", itemSubCategory);
            return null;
        }
    }
    private Page<Item> getItemsPageByFilters(Gender gender, ItemSubCategory subCategory, Pageable pageable) {
        if (gender != null && subCategory != null) {
            return itemRepository.findByGenderAndSubCategory(gender, subCategory, pageable);
        } else if (gender != null) {
            return itemRepository.findByGender(gender, pageable);
        } else if (subCategory != null) {
            return itemRepository.findBySubCategory(subCategory, pageable);
        } else {
            return itemRepository.findAll(pageable);
        }
    }

    public int validateAndCorrectPageSize(int size) {
        if (size <= 0) {
            log.warn("잘못된 페이지 크기: {}, 기본값 16으로 설정", size);
            return 16;
        }
        if (size > 100) {
            log.warn("페이지 크기가 너무 큼: {}, 최대값 100으로 설정", size);
            return 100;
        }
        return size;
    }
}