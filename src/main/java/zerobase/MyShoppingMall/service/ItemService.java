package zerobase.MyShoppingMall.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.repository.ItemRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    //상품 생성
    public ItemResponseDto createItem(ItemRequestDto dto) {
        Item item = Item.builder()
                .itemName(dto.getItemName())
                .itemComment(dto.getItemComment())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .deleteType('N')
                .createdAt(LocalDate.now())
                .build();
        itemRepository.save(item);
        return new ItemResponseDto(item);
    }

    //상품 수정
    @Transactional
    public ItemResponseDto updateItem(Long itemId, ItemRequestDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));

        item.setItemName(dto.getItemName());
        item.setItemComment(dto.getItemComment());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setUpdatedAt(LocalDate.now());

        return new ItemResponseDto(item);
    }

    //상품 삭제
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));

        item.setDeleteType('Y');
        item.setUpdatedAt(LocalDate.now());
    }

    //상품 조회
    public ItemResponseDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));
        if (item.getDeleteType() == 'Y') {
            throw new RuntimeException("삭제된 상품입니다.");
        }
        return new ItemResponseDto(item);
    }

    //전체 목록 조회
    public List<ItemResponseDto> getAllItems() {
        List<Item> items = itemRepository.findAll()
                .stream()
                .filter(item -> item.getDeleteType() == 'N')
                .collect(Collectors.toList());

        return items.stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }

}
