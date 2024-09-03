package durel.services;

import org.springframework.beans.BeanUtils;
import durel.domain.model.Language;
import durel.domain.repository.LanguageDAO;
import durel.dto.responses.LanguageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class LanguageService {

    private final LanguageDAO languageDAO;

    @Autowired
    public LanguageService(LanguageDAO languageDAO) {
        this.languageDAO = languageDAO;
    }

    public Language getLanguage(String code) throws EntityNotFoundException {
        return languageDAO.getById(code);
    }

    public List<LanguageDTO> getLocales() {
        List<Language> languageList = languageDAO.findByIsLocaleTrue();
        return listToLanguageData(languageList);
    }

    public List<LanguageDTO> getLanguages() {
        List<Language> languageList = languageDAO.findAll();
        return listToLanguageData(languageList);
    }

    public List<LanguageDTO> getTutorialLanguages() {
        List<Language> languageList = languageDAO.findByTutorialNotNull();
        return listToLanguageData(languageList);
    }

    private List<LanguageDTO> listToLanguageData(List<Language> languageList) {
        List<LanguageDTO> languageDTOList = new ArrayList<>();
        for (Language language : languageList) {
            LanguageDTO languageDTO = new LanguageDTO();
            BeanUtils.copyProperties(language, languageDTO);
            languageDTOList.add(languageDTO);
        }
        return languageDTOList;
    }

    /**
     * Gets user's current language.
     */
    public LanguageDTO getCurrentLocale()  {
        Locale locale = LocaleContextHolder.getLocale();
        return new LanguageDTO(locale.getDisplayLanguage(),locale.getLanguage(),true) ;
    }
}
