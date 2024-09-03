package durel.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents language data with its name, code, and locale information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LanguageDTO {

    private String name;
    private String code;
    private boolean isLocale;
}
