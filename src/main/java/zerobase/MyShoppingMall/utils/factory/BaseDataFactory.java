package zerobase.MyShoppingMall.utils.factory;

import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import zerobase.MyShoppingMall.utils.factory.data.ProductNameGenerator;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.IntStream;

public abstract class BaseDataFactory<T> implements DataFactory<T> {

    protected final Faker faker = new Faker(Locale.KOREA);
    protected final Random random = new Random();

    @Autowired
    protected ProductNameGenerator productNameGenerator;

    @Override
    public List<T> createBatch(int count) {
        List<T> results = new ArrayList<>(count);
        IntStream.range(0, count).forEach(i -> results.add(createSingle()));
        return results;
    }

    protected <E extends Enum<E>> E randomEnum(Class<E> enumClass) {
        E[] values = enumClass.getEnumConstants();
        return values[random.nextInt(values.length)];
    }

    protected int randomPrice(int min, int max) {
        int basePrice = min + random.nextInt(max - min);
        return (basePrice / 1000) * 1000; // 1000원 단위로 조정
    }
}
