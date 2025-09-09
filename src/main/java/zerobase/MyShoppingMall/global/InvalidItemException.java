package zerobase.MyShoppingMall.global;

public class InvalidItemException extends ItemServiceException {
    public InvalidItemException(String message) {
        super(message);
    }
}
