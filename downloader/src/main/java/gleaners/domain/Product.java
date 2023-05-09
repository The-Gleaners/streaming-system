package gleaners.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    String idHash;
    String id;
    String title;
    int pricePc;
    String link;
    String imageLink;
    String categoryName1;
    String shipping;
    String flag;
    String hash;

    @Builder
    public Product(
        String idHash,
        String id,
        String title,
        int pricePc,
        String link,
        String imageLink,
        String categoryName1,
        String shipping,
        String flag,
        String hash) {
        this.idHash = idHash;
        this.id = id;
        this.title = title;
        this.pricePc = pricePc;
        this.link = link;
        this.imageLink = imageLink;
        this.categoryName1 = categoryName1;
        this.shipping = shipping;
        this.flag = flag;
        this.hash = hash;
    }
}
