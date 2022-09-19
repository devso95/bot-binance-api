package deso.future_bot.security;

import deso.future_bot.model.entity.User;
import deso.future_bot.repository.UserRepository;
import deso.future_bot.security.jwt.CustomUserDetails;
import deso.future_bot.security.jwt.UserNotActivatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Authenticate a user from the database.
 */
@Component
public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);

        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        Optional<User> userOptional = userRepository
                .findOneByLoginOrPhoneNumber(lowercaseLogin, lowercaseLogin);

        return userOptional.map(DomainUserDetailsService::createSpringSecurityUser)
                .orElseThrow(() -> new UsernameNotFoundException("User " + login + " was not found in the database"));
    }

    public static CustomUserDetails createSpringSecurityUser(User user) {
        if (!user.getActivated()) {
            throw new UserNotActivatedException("User " + user.getLogin() + " was not activated");
        }
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        return new CustomUserDetails(user.getId(), user.getLogin(), user.getPassword(), grantedAuthorities);
    }
}
