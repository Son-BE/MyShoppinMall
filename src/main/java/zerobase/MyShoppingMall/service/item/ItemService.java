package zerobase.MyShoppingMall.service.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.domain.ItemImage;
import zerobase.MyShoppingMall.domain.WishList;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.repository.cart.CartItemRepository;
import zerobase.MyShoppingMall.repository.cart.CartRepository;
import zerobase.MyShoppingMall.repository.item.ItemImageRepository;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemImageService itemImageService;
    private final ItemImageRepository itemImageRepository;
    private final CartItemRepository cartItemRepository;
//    private final WishListRepository wishListRepository;

    //상품 생성
    public ItemResponseDto createItem(ItemRequestDto dto, MultipartFile imageFile) {
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
                .build();
        itemRepository.save(item);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                itemImageService.saveItemImage(item.getId(), imageFile);
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 중 오류 발생", e);
            }
        }

        return ItemResponseDto.fromEntity(item);
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
            itemImageService.deleteItemImage(item.getId());
            itemImageService.saveItemImage(item.getId(), imageFile);
        }

        return ItemResponseDto.fromEntity(item);
    }

    //상품 삭제
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));

        // 1. ItemImage 여러 개 조회
        List<ItemImage> itemImages = itemImageRepository.findAllByItemId(itemId);

        // 2. 이미지 파일 및 DB 삭제
        for (ItemImage itemImage : itemImages) {
            if (itemImage.getItemPath() != null) {
                File file = new File(itemImage.getItemPath());
                if (file.exists()) {
                    file.delete();
                }
            }
            itemImageRepository.delete(itemImage);
        }

        // 3. CartItem, WishList 명시적 삭제
        cartItemRepository.deleteById(itemId);
//        wishListRepository.deleteById(itemId);
        // 4. 실제 아이템 삭제
        itemRepository.delete(item);
    }

    //상품 조회
    public ItemResponseDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));
        if (item.getDeleteType() == 'Y') {
            throw new RuntimeException("삭제된 상품입니다.");
        }
        return ItemResponseDto.fromEntity(item);
    }

    //전체 목록 조회
    public List<ItemResponseDto> getAllItems() {
        List<Item> items = itemRepository.findAll()
                .stream()
                .filter(item -> item.getDeleteType() == 'N')
                .collect(Collectors.toList());

        return items.stream()
                .map(ItemResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public Page<ItemResponseDto> getItemsByCategory(ItemCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return itemRepository.findByCategory(category, pageable)
                .map(ItemResponseDto::fromEntity);
    }

    @Transactional
    public List<ItemResponseDto> getItemsByGender(Gender gender) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getDeleteType() == 'N')
                .filter(item -> item.getCategory().getGender() == gender)
                .map(ItemResponseDto::fromEntity)
                .collect(Collectors.toList());

    }

    public Page<ItemResponseDto> getAllItemsPageable(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Item> itemsPage = itemRepository.findAll(pageable);
        return itemsPage.map(ItemResponseDto::fromEntity);
    }

    public List<ItemResponseDto> getLatestItemsByGender(Gender gender, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return itemRepository.findByGender(gender, pageable).stream()
                .map(ItemResponseDto::fromEntity)
                .toList();
    }


    public List<ItemResponseDto> getSortedItemsByGender(Gender gender, String sortType) {
        Sort sort;

        switch (sortType.toLowerCase()) {
            case "pricelow":
                sort = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "pricehigh":
                sort = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "scorehigh":
                sort = Sort.by(Sort.Direction.DESC, "rating");
                break;
            case "mostreviews":
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

        Pageable pageable = PageRequest.of(0, 100, sort); // 첫 페이지, 최대 100개 항목
        Page<Item> itemPage = itemRepository.findByGender(gender, pageable);

        return itemPage.getContent().stream()
                .map(ItemResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
