package zerobase.MyShoppingMall.global;


public class ItemServiceException extends RuntimeException {
    public ItemServiceException(String message) {
        super(message);
    }

    public ItemServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
