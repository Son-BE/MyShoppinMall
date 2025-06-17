package zerobase.MyShoppingMall.type;

public enum OrderStatus {
    WAITING,
    PAID,
    ORDERED, // 주문 완료
    SHIPPED, // 배송 중
    DELIVERED, // 배송 완료
    CANCELLED, // 주문 취소
    RETURNED, // 반품
}
