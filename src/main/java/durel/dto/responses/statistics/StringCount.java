package durel.dto.responses.statistics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StringCount {

    private String identifier;
    private Long count;

    public StringCount(String identifier, Long count) {
        this.identifier = identifier;
        this.count = count;
    }

    @Override
    public String toString() {
        return identifier + ":" + count + " ";
    }
}