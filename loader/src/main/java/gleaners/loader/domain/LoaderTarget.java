package gleaners.loader.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dccollect")
public record LoaderTarget(
        @Id
        String id,
        String value
) {
}
