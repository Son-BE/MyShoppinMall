package zerobase.MyShoppingMall.utils;


import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import zerobase.MyShoppingMall.type.Color;

import java.lang.annotation.Annotation;

@Component
public class StringToColorConverter implements Converter<String, Color> {
    @Override
    public Color convert(String source) {
        // #000000 형태의 문자열을 Color 객체로 변환
        return Color.fromHex(source);
    }
}
