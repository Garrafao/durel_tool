package durel;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
@EnableTransactionManagement
public class MvcConfig implements WebMvcConfigurer {

    /**
     * The Layout-Dialect is used to deal with the thymeleaf template layouts.
     */
    @Bean
    public static LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

/*
    /**
     * Sets up the correspondence between html webpages and URLs. Add only those
     * that are not in any other TaskController.
     * /
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        // registry.addViewController("/test").setViewName("/pages/test");
    }
*/

    /**
     * Configures the multi-language locale.
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new CookieLocaleResolver();
    }

    /**
     * Registers and executes changes in the locale.
     */
    @Bean
    public static LocaleChangeInterceptor localeInterceptor() {
        LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
        localeInterceptor.setParamName("lang");
        return localeInterceptor;
    }

    @Override
    public final void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor());
    }

    /**
     * Sets up the directories in which Spring will look up for the literals in all
     * available languages.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:locale/annotation/texts",
                "classpath:locale/guidelines/texts",
                "classpath:locale/home/texts",
                "classpath:locale/menu/texts",
                "classpath:locale/contact/texts",
                "classpath:locale/register/texts",
                "classpath:locale/about/texts",
                "classpath:locale/tutorial/texts",
                "classpath:locale/footer/texts",
                "classpath:locale/login/texts",
                "classpath:locale/manageProjects",
                "classpath:locale/viewData",
                "classpath:locale/statistics");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    @Override
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }
}
