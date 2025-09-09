package zerobase.MyShoppingMall.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.utils.factory.ItemDataFactory;
import zerobase.MyShoppingMall.utils.factory.MemberDataFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(name = "app.data.management.enabled", havingValue = "true", matchIfMissing = false)
public class DataManagementController {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final MemberDataFactory memberDataFactory;
    private final ItemDataFactory itemDataFactory;

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDataStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 회원 통계
        Map<String, Object> memberStats = new HashMap<>();
        memberStats.put("total", memberRepository.count());
        memberStats.put("admin", memberRepository.countByRole(zerobase.MyShoppingMall.type.Role.ADMIN));
        memberStats.put("user", memberRepository.countByRole(zerobase.MyShoppingMall.type.Role.USER));

        // 상품 통계
        Map<String, Object> itemStats = new HashMap<>();
        itemStats.put("total", itemRepository.count());
        itemStats.put("male", itemRepository.countByGender(zerobase.MyShoppingMall.type.Gender.MALE));
        itemStats.put("female", itemRepository.countByGender(zerobase.MyShoppingMall.type.Gender.FEMALE));

        // 가격 통계
        Map<String, Object> priceStats = new HashMap<>();
        priceStats.put("average", itemRepository.findAveragePrice());
        priceStats.put("min", itemRepository.findMinPrice());
        priceStats.put("max", itemRepository.findMaxPrice());

        stats.put("members", memberStats);
        stats.put("items", itemStats);
        stats.put("prices", priceStats);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/generate/members")
    public ResponseEntity<Map<String, Object>> generateMembers(@RequestParam(defaultValue = "10") int count) {
        if (count > 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "한 번에 최대 100명까지만 생성 가능합니다."));
        }

        try {
            var members = memberDataFactory.createBatch(count);
            memberRepository.saveAll(members);

            log.info("관리자 요청으로 {}명의 회원 데이터 생성", count);

            return ResponseEntity.ok(Map.of(
                    "message", count + "명의 회원이 생성되었습니다.",
                    "count", count,
                    "total", memberRepository.count()
            ));

        } catch (Exception e) {
            log.error("회원 데이터 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "회원 데이터 생성에 실패했습니다."));
        }
    }

    @PostMapping("/generate/items")
    public ResponseEntity<Map<String, Object>> generateItems(@RequestParam(defaultValue = "10") int count) {
        if (count > 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "한 번에 최대 100개까지만 생성 가능합니다."));
        }

        try {
            var items = itemDataFactory.createBatch(count);
            itemRepository.saveAll(items);

            log.info("관리자 요청으로 {}개의 상품 데이터 생성", count);

            return ResponseEntity.ok(Map.of(
                    "message", count + "개의 상품이 생성되었습니다.",
                    "count", count,
                    "total", itemRepository.count()
            ));

        } catch (Exception e) {
            log.error("상품 데이터 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "상품 데이터 생성에 실패했습니다."));
        }
    }

    @DeleteMapping("/cleanup/test-data")
    public ResponseEntity<Map<String, Object>> cleanupTestData() {
        try {
            // 테스트 데이터만 삭제 (이메일 패턴으로 구분)
            long deletedMembers = memberRepository.deleteByEmailContaining("@test.com");
            long deletedItems = itemRepository.deleteByItemNameContaining("테스트");

            log.warn("관리자 요청으로 테스트 데이터 정리 - 회원: {}명, 상품: {}개", deletedMembers, deletedItems);

            return ResponseEntity.ok(Map.of(
                    "message", "테스트 데이터가 정리되었습니다.",
                    "deletedMembers", deletedMembers,
                    "deletedItems", deletedItems
            ));

        } catch (Exception e) {
            log.error("테스트 데이터 정리 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "테스트 데이터 정리에 실패했습니다."));
        }
    }
}