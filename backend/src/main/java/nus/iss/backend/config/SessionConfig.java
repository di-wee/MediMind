package nus.iss.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
@EnableScheduling
@EnableJdbcHttpSession
public class SessionConfig {

    /**
     * Disable session cleanup if spring.session.jdbc.cleanup.enabled=false
     * This prevents the error when session tables don't exist
     */
    @Bean
    @ConditionalOnProperty(name = "spring.session.jdbc.cleanup.enabled", havingValue = "false", matchIfMissing = false)
    public Object disableSessionCleanup() {
        // This bean will only be created if cleanup is explicitly disabled
        return new Object();
    }
}
