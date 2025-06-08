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
import zerobase.MyShoppingMall.repository.wishList.WishListRepository;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

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
    private final WishListRepository wishListRepository;

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

        List<ItemImage> itemImages = itemImageRepository.findAllByItemId(itemId);

        for (ItemImage itemImage : itemImages) {
            if (itemImage.getItemPath() != null) {
                File file = new File(itemImage.getItemPath());
                if (file.exists()) {
                    file.delete();
                }
            }
            itemImageRepository.delete(itemImage);
        }

        cartItemRepository.deleteById(itemId);
        wishListRepository.deleteById(itemId);
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
                subCategory = ItemSubCategory.valueOf("M_" + itemSubCategory.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("카테고리 파싱 오류");
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
