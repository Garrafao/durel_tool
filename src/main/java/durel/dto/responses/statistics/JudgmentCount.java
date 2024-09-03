package durel.dto.responses.statistics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JudgmentCount {

    private Float judgment;
    private Long count;

    public JudgmentCount(Float judgment, Long count) {
        this.judgment = judgment;
        this.count = count;
    }

    @Override
    public String toString() {
        return judgment + ":" + count + " ";
    }
}