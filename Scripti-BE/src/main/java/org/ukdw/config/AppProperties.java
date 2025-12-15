package org.ukdw.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Creator: dendy
 * Date: 6/30/2024
 * Time: 12:08 PM
 * Description : application properties taken from application.yml app: tag
 */

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @Getter
    private final Auth auth = new Auth();

    @Getter
    private final OAuth2 oauth2 = new OAuth2();

    @Getter
    private final EmailRegex emailRegex = new EmailRegex();

    @Getter
    private final NotificationConfig notificationConfig = new NotificationConfig();

    @Getter
    @Setter
    private List<String> excludeFilter;

    @Data
    public static class Auth {
        private String tokenSecret;
        private long tokenExpirationDay;
    }

    @Data
    public static final class OAuth2 {
        private String googleClientId;
        private String googleSecret;
    }

    @Data
    public static final class EmailRegex {
        private String ti;
        private String si;
        private String staff;
    }

    @Data
    public static final class NotificationConfig {
        private String sound;
        private String color;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}