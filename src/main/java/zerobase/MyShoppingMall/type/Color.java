package zerobase.MyShoppingMall.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Color {
    BLACK("블랙", "#000000"),
    WHITE("화이트", "#FFFFFF"),
    GRAY("그레이", "#808080"),
    NAVY("네이비", "#000080"),
    BROWN("브라운", "#8B4513"),
    BEIGE("베이지", "#F5F5DC"),
    RED("레드", "#FF0000"),
    PINK("핑크", "#FFC0CB"),
    ORANGE("오렌지", "#FFA500"),
    YELLOW("옐로우", "#FFFF00"),
    GREEN("그린", "#008000"),
    BLUE("블루", "#0000FF"),
    PURPLE("퍼플", "#800080"),
    MULTI("멀티컬러", "#000000"); // 멀티는 대표색이 없으니 기본값으로 처리

    private final String displayName;
    private final String hexCode;

    Color(String displayName, String hexCode) {
        this.displayName = displayName;
        this.hexCode = hexCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonValue
    public String getHexCode() {
        return hexCode;
    }

    @JsonCreator
    public static Color fromHex(String hex) {
        for (Color c : Color.values()) {
            if (c.hexCode.equalsIgnoreCase(hex)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown color hex: " + hex);
    }
}
