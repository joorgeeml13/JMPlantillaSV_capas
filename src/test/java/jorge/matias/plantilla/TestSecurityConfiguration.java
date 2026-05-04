package jorge.matias.plantilla;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration(proxyBeanMethods = false)
class TestSecurityConfiguration {

    @Bean
    UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("test")
            .password("{noop}test")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
