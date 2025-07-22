package zerobase.MyShoppingMall.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;

@Component
@RequiredArgsConstructor
public class DataInit {
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

//    @PostConstruct
//    void init() {
//        List<Member> memberList = new ArrayList<>();
//        IntStream.range(1, 51).forEach(i -> {
//            String randomPassword = UUID.randomUUID().toString();
//            String encodedPassword = encoder.encode(randomPassword);
//
//            memberList.add(Member.builder()
//                    .nickName("회원" + i)
//                    .role(Role.USER)
//                    .email("user" + i + "@test.com")
//                    .password("1234")
//                    .phoneNumber("010-0000-" + String.format("%04d", i))
//                    .build());
//        });
//        memberRepository.saveAll(memberList);
//    }

//    @PostConstruct
//    void init() {
//        List<Item> itemList = new ArrayList<>();
//        Random random = new Random();
//
//        IntStream.range(1, 11).forEach(i -> {
//            Gender gender = (i % 2 == 0) ? Gender.MALE : Gender.FEMALE;
//            ItemCategory category = (gender == Gender.MALE) ? ItemCategory.MENS_ACCESSORY : ItemCategory.WOMENS_ACCESSORY;
//            ItemSubCategory itemSubCategory = (gender == Gender.MALE) ? ItemSubCategory.M_NECKLACE : ItemSubCategory.W_NECKLACE;
//            int price = 10000 + (random.nextInt(11) * 1000);
//
//            ItemImage defaultImage = ItemImage.builder()
//                    .itemPath("/images/default.png")
//                    .build();
//
//            itemList.add(Item.builder()
//                    .gender(gender)
//                    .deleteType('N')
//                    .itemName("목걸이 No." + i)
//                    .category(category)
//                    .subCategory(itemSubCategory)
//                    .price(price)
//                    .itemComment("목걸이 No." + i + "입니다.")
//                    .createdAt(LocalDateTime.now())
//                    .itemImages(List.of(defaultImage))
//                    .quantity(random.nextInt(6) + 5)
//                    .build());
//        });
//        itemRepository.saveAll(itemList);
//    }

}
