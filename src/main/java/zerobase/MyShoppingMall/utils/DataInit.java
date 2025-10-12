package zerobase.MyShoppingMall.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.type.Role;
import zerobase.MyShoppingMall.utils.factory.ItemDataFactory;
import zerobase.MyShoppingMall.utils.factory.MemberDataFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data.init.enabled", havingValue = "true", matchIfMissing = false)
public class DataInit {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final MemberDataFactory memberDataFactory;
    private final ItemDataFactory itemDataFactory;

    private int memberCount = 100;
    private int itemCount = 500;
    private int batchSize = 50;
    private boolean skipIfExists = false;
    private boolean createAdmin = false;

    @PostConstruct
    public void init() {
        long startTime = System.currentTimeMillis();
        log.info("=== 더미 데이터 생성 시작 ===");

        try {
            // 기존 데이터 확인
            if (skipIfExists && hasExistingData()) {
                log.info("기존 데이터가 존재하여 더미 데이터 생성을 건너뜁니다.");
                return;
            }

            createMembers();
            createItems();
            // 병렬로 데이터 생성
            CompletableFuture<Void> memberFuture = CompletableFuture.runAsync(this::createMembers);
            CompletableFuture<Void> itemFuture = CompletableFuture.runAsync(this::createItems);

            // 모든 작업 완료 대기
            CompletableFuture.allOf(memberFuture, itemFuture).join();
//            CompletableFuture.allOf(itemFuture).join();

            long endTime = System.currentTimeMillis();
            log.info("=== 더미 데이터 생성 완료 === (소요시간: {}ms)", endTime - startTime);

            printDataStatistics();

        } catch (Exception e) {
            log.error("더미 데이터 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private boolean hasExistingData() {
        long memberCount = memberRepository.count();
        long itemCount = itemRepository.count();

        log.info("기존 데이터 확인 - 회원: {}명, 상품: {}개", memberCount, itemCount);

        return memberCount > 0 || itemCount > 0;
    }

    private void createMembers() {
        log.info("회원 데이터 생성 시작 - 목표: {}명", memberCount);

        try {
            // 관리자 계정 생성
            if (createAdmin) {
                createAdminAccount();
            }

            // 일반 회원 배치 생성
            int remainingCount = memberCount;
            int batchNumber = 1;

            while (remainingCount > 0) {
                int currentBatchSize = Math.min(batchSize, remainingCount);

                List<Member> members = memberDataFactory.createBatch(currentBatchSize);
                memberRepository.saveAll(members);

                remainingCount -= currentBatchSize;
                log.info("회원 배치 {} 저장 완료 - {}명 (남은 수: {}명)",
                        batchNumber++, currentBatchSize, remainingCount);
                log.info("createBatch({}) 호출 결과: {}명", currentBatchSize, members.size());
            }

            log.info("회원 데이터 생성 완료 - 총 {}명", memberCount);

        } catch (Exception e) {
            log.error("회원 데이터 생성 실패: {}", e.getMessage(), e);
        }

    }

    private void createAdminAccount() {
        try {
            // 기본 관리자 계정이 없는 경우에만 생성
            if (!memberRepository.existsByEmail("admin@zerobase.com")) {
                Member admin = memberDataFactory.createWithSpecificData("admin@zerobase.com", Role.ADMIN);
                admin.setPassword("$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvuyE+95Zu8eJzY9k.7K8s2si"); // admin123
                admin.setNickName("관리자");
                admin.setCreatedAt(LocalDateTime.now());

                memberRepository.save(admin);
                log.info("관리자 계정 생성 완료: admin@zerobase.com");
            } else {
                log.info("관리자 계정이 이미 존재합니다.");
            }
        } catch (Exception e) {
            log.error("관리자 계정 생성 실패: {}", e.getMessage(), e);
        }
    }

    private void createItems() {
        log.info("상품 데이터 생성 시작 - 목표: {}개", itemCount);

        try {
            int remainingCount = itemCount;
            int batchNumber = 1;

            while (remainingCount > 0) {
                int currentBatchSize = Math.min(batchSize, remainingCount);

                List<Item> items = itemDataFactory.createBatch(currentBatchSize);
                log.info("createBatch({}) → 반환 수: {}", currentBatchSize, items.size());
                itemRepository.saveAll(items);
                itemRepository.flush();
                log.info("→ 저장 시도한 아이템 수: {}, 저장 후 총 수량: {}", items.size(), itemRepository.count());

                remainingCount -= currentBatchSize;
                log.info("상품 배치 {} 저장 완료 - {}개 (남은 수: {}개)",
                        batchNumber++, currentBatchSize, remainingCount);
            }

            log.info("상품 데이터 생성 완료 - 총 {}개", itemCount);

        } catch (Exception e) {
            log.error("상품 데이터 생성 실패: {}", e.getMessage(), e);
        }
    }

    private void printDataStatistics() {
        try {
            log.info("=== 데이터 통계 ===");

            // 회원 통계
            long totalMembers = memberRepository.count();
            long adminCount = memberRepository.countByRole(Role.ADMIN);
            long userCount = memberRepository.countByRole(Role.USER);

            log.info("회원 통계:");
            log.info("  - 전체: {}명", totalMembers);
            log.info("  - 관리자: {}명", adminCount);
            log.info("  - 일반회원: {}명", userCount);

            // 상품 통계
            long totalItems = itemRepository.count();
            long maleItems = itemRepository.countByGender(zerobase.MyShoppingMall.type.Gender.MALE);
            long femaleItems = itemRepository.countByGender(zerobase.MyShoppingMall.type.Gender.FEMALE);

            log.info("상품 통계:");
            log.info("  - 전체: {}개", totalItems);
            log.info("  - 남성용: {}개", maleItems);
            log.info("  - 여성용: {}개", femaleItems);

            // 가격 통계
            Double avgPrice = itemRepository.findAveragePrice();
            Integer minPrice = itemRepository.findMinPrice();
            Integer maxPrice = itemRepository.findMaxPrice();

            log.info("가격 통계:");
            log.info("  - 평균: {:.0f}원", avgPrice != null ? avgPrice : 0);
            log.info("  - 최저: {}원", minPrice != null ? minPrice : 0);
            log.info("  - 최고: {}원", maxPrice != null ? maxPrice : 0);

        } catch (Exception e) {
            log.error("통계 출력 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}







