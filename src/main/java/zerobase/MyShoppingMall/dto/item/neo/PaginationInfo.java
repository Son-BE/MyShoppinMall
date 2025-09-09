package zerobase.MyShoppingMall.dto.item.neo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class PaginationInfo {
    private final int currentPage;          // 현재 페이지 (1-based)
    private final int totalPages;           // 전체 페이지 수
    private final long totalElements;       // 전체 요소 수
    private final List<Integer> pageNumbers; // 페이지 번호 리스트
    private final boolean hasPrevBlock;     // 이전 블록 존재 여부
    private final boolean hasNextBlock;     // 다음 블록 존재 여부
    private final int prevBlockPage;        // 이전 블록의 마지막 페이지
    private final int nextBlockPage;        // 다음 블록의 첫 페이지
    private final int startPage;            // 현재 블록의 시작 페이지
    private final int endPage;              // 현재 블록의 끝 페이지
    private final int blockSize;            // 블록 크기

    // 편의 메서드들
    public boolean hasPrevious() {
        return currentPage > 1;
    }

    public boolean hasNext() {
        return currentPage < totalPages;
    }

    public int getPreviousPage() {
        return Math.max(1, currentPage - 1);
    }

    public int getNextPage() {
        return Math.min(totalPages, currentPage + 1);
    }

    public boolean isEmpty() {
        return totalElements == 0;
    }

    public boolean isFirstPage() {
        return currentPage == 1;
    }

    public boolean isLastPage() {
        return currentPage == totalPages;
    }
}