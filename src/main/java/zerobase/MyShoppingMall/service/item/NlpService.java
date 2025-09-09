package zerobase.MyShoppingMall.service.item;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NlpService {

    private final WebClient recommendWebClient;

//    public List<ItemResponseDto> recommendByKeyword(String query) {
//        return recommendWebClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/nlp-recommend")
//                        .queryParam("query", query)
//                        .build())
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<ItemResponseDto>>() {})
//                .block();
//    }

    /**
     * AI/NLP 서버에 스타일 질의어를 보내고,
     * 추천 상품 ID 목록을 받아 파싱해서 리턴
     */
    public List<Long> recommendByNlp(String query) {
        // 예: 서버가 [1,5,20,35] 같은 JSON 배열을 반환한다고 가정
        List<Long> ids = recommendWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/nlp-recommend")
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Long>>() {})
                .block();   // 간단 구현 예시, 실제론 리액티브 방식 권장

        return ids != null ? ids : Collections.emptyList();
    }

}
