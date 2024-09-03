package durel.session;


import durel.dto.responses.AnnotationDTO;
import durel.dto.responses.UseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@NoArgsConstructor
@Getter
@Setter
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DataSessionData {

    private ArrayList<UseDTO> useDTOs;

    private ArrayList<AnnotationDTO> annotationViewData;
}
