package zerobase.MyShoppingMall.utils.factory;

import java.util.List;

public interface DataFactory<T> {
    T createSingle();
    List<T> createBatch(int count);
    T createWithSpecificData(Object... params);
}