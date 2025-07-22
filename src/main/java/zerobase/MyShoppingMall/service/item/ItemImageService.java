//package zerobase.MyShoppingMall.service.item;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import zerobase.MyShoppingMall.entity.Item;
//import zerobase.MyShoppingMall.entity.ItemImage;
//import zerobase.MyShoppingMall.repository.item.ItemImageRepository;
//import zerobase.MyShoppingMall.repository.item.ItemRepository;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class ItemImageService {
//
//    private final ItemRepository itemRepository;
//    private final ItemImageRepository itemImageRepository;
//
//    @Value("${file.upload-dir}")
//    private String uploadDir;
//
//    public ItemImage saveItemImage(Long itemId, MultipartFile imageFile) throws IOException {
//        Item item = itemRepository.findById(itemId)
//                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
//
//        String originalFilename = imageFile.getOriginalFilename();
//        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        String uniqueFilename = UUID.randomUUID() + extension;
//
//        // 저장 경로 설정
//        Path savePath = Paths.get(uploadDir + uniqueFilename);
//        Files.createDirectories(savePath.getParent());
//        Files.copy(imageFile.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
//
//        // DB 저장
//        ItemImage image = ItemImage.builder()
//                .item(item)
//                .itemPath(uniqueFilename)
//                .build();
//
//        return itemImageRepository.save(image);
//    }
//
//    public void deleteItemImage(Long itemId) {
//        List<ItemImage> images = itemImageRepository.findAllByItemId(itemId);
//        for (ItemImage image : images) {
//            File file = new File(uploadDir + image.getItemPath());
//            if (file.exists()) {
//                file.delete();
//            }
//            itemImageRepository.delete(image);
//        }
//    }
//}
