//package zerobase.MyShoppingMall;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.cglib.core.Local;
//import zerobase.MyShoppingMall.domain.Item;
//import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
//import zerobase.MyShoppingMall.repository.item.ItemRepository;
//import zerobase.MyShoppingMall.service.item.ItemService;
//import zerobase.MyShoppingMall.type.ItemCategory;
//import zerobase.MyShoppingMall.type.ItemSubCategory;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.times;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class ItemServiceTest {
//
//    @Mock
//    private ItemRepository itemRepository;
//
//    @InjectMocks
//    private ItemService itemService;
//
//    @Test
//    void createItemTest() {
//        // Given
//        ItemRequestDto dto = new ItemRequestDto();
//        dto.setItemName("Test Item");
//        dto.setItemComment("Test Comment");
//        dto.setPrice(1000);
//        dto.setQuantity(10);
//        dto.setCategory(ItemCategory.MENS_TOP);
//        dto.setSubCategory(ItemSubCategory.M_TSHIRT);
//
//        Item savedItem = Item.builder()
//                .id(1L)
//                .itemName(dto.getItemName())
//                .itemComment(dto.getItemComment())
//                .price(dto.getPrice())
//                .quantity(dto.getQuantity())
//                .deleteType('N')
//                .createdAt(LocalDateTime.now())
//                .category(dto.getCategory())
//                .subCategory(dto.getSubCategory())
//                .build();
//
//        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);
//
//        // When
//        var result = itemService.createItem(dto);
//
//        // Then
//        assertEquals("Test Item", result.getItemName());
//        assertEquals(1000, result.getPrice());
//        verify(itemRepository, times(1)).save(any(Item.class));
//    }
//
//    @Test
//    void updateItemTest() {
//        Long itemId = 1L;
//        Item existingItem = Item.builder()
//                .id(itemId)
//                .itemName("Old Name")
//                .price(500)
//                .deleteType('N')
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        ItemRequestDto dto = new ItemRequestDto();
//        dto.setItemName("New Name");
//        dto.setItemComment("Updated comment");
//        dto.setPrice(1500);
//        dto.setQuantity(20);
//        dto.setCategory(ItemCategory.MENS_TOP);
//        dto.setSubCategory(ItemSubCategory.M_TSHIRT);
//
//        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
//
//        var result = itemService.updateItem(itemId, dto);
//
//        assertEquals("New Name", result.getItemName());
//        assertEquals(1500, result.getPrice());
//        verify(itemRepository, times(1)).findById(itemId);
//    }
//
//    @Test
//    void deleteItemTest() {
//        Long itemId = 1L;
//        Item item = Item.builder()
//                .id(itemId)
//                .deleteType('N')
//                .build();
//
//        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
//
//        itemService.deleteItem(itemId);
//
//        assertEquals('Y', item.getDeleteType());
//        verify(itemRepository, times(1)).findById(itemId);
//    }
//
//    @Test
//    void getItemDeletedExceptionTest() {
//        Long itemId = 1L;
//        Item item = Item.builder()
//                .id(itemId)
//                .deleteType('Y')
//                .build();
//
//        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            itemService.getItem(itemId);
//        });
//
//        assertEquals("삭제된 상품입니다.", exception.getMessage());
//    }
//}
