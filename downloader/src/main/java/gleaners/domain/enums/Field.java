package gleaners.domain.enums;

import lombok.Getter;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum Field {
    ID(List.of("id", "pid")),
    TITLE(List.of("title")),
    PRICE_PC(List.of("price_pc")),
    LINK(List.of("link")),
    IMAGE_LINK(List.of("image_link")),
    CATEGORY_NAME1(List.of("category_name1")),
    SHIPPING(List.of("shipping")),
    CLASS(List.of("class"));

    private final List<String> fields;

    Field(List<String> fields) {
        this.fields = fields;
    }

    private static final Map<String, Field> FIELD_KEY_MAP =
        Arrays.stream(values())
            .flatMap(f -> f.fields.stream()
                .map(k -> new AbstractMap.SimpleEntry<>(k, f)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static Map<String, Field> getFieldKeyMap() {
        return FIELD_KEY_MAP;
    }
}
