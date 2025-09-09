//package zerobase.MyShoppingMall.repository;
//
//import com.fasterxml.jackson.databind.util.ArrayBuilders;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Repository;
//import zerobase.MyShoppingMall.entity.Item;
//import zerobase.MyShoppingMall.type.Gender;
//import zerobase.MyShoppingMall.type.ItemSubCategory;
//
//import java.util.List;
//
//@Repository
//@RequiredArgsConstructor
//@Slf4j
//public class ItemQueryRepository {
//    private final JPAQueryFactory queryFactory;
//
//    public Page<Item> findItemsByCondition(Gender gender, String subCategory, Pageable pageable) {
//        QItem item = QItem.item;
//
//        ArrayBuilders.BooleanBuilder builder = new ArrayBuilders.BooleanBuilder();
//        builder.and(item.deleteType.eq('N'));
//
//        if (gender != null) {
//            builder.and(item.gender.eq(gender));
//        }
//
//        if (subCategory != null && !subCategory.isEmpty()) {
//            ItemSubCategory enumSubCategory = parseSubCategory(subCategory);
//            if (enumSubCategory != null) {
//                builder.and(item.subCategory.eq(enumSubCategory));
//            }
//        }
//
//        List<Item> content = queryFactory
//                .selectFrom(item)
//                .where(builder)
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .orderBy(createOrderSpecifier(pageable.getSort(), item))
//                .fetch();
//
//        Long total = queryFactory
//                .select(item.count())
//                .from(item)
//                .where(builder)
//                .fetchOne();
//
//        return new PageImpl<>(content, pageable, total != null ? total : 0);
//    }
//
//    private ItemSubCategory parseSubCategory(String subCategory) {
//        try {
//            String enumKey = "M_" + subCategory.toUpperCase();
//            return ItemSubCategory.valueOf(enumKey);
//        } catch (IllegalArgumentException e) {
//            log.warn("잘못된 서브카테고리: {}", subCategory);
//            return null;
//        }
//    }
//
//    private OrderSpecifier<?> createOrderSpecifier(Sort sort, QItem item) {
//        for (Sort.Order order : sort) {
//            switch (order.getProperty()) {
//                case "price":
//                    return order.isAscending() ? item.price.asc() : item.price.desc();
//                case "rating":
//                    return item.rating.desc();
//                case "reviewCount":
//                    return item.reviewCount.desc();
//                case "salesVolume":
//                    return item.salesVolume.desc();
//                default:
//                    return item.createdAt.desc();
//            }
//        }
//        return item.createdAt.desc();
//    }
//}
