package deso.future_bot.service;

import deso.future_bot.mapper.UserMapper;
import deso.future_bot.model.dto.UserDto;
import deso.future_bot.model.rest.AddUser;
import deso.future_bot.repository.UserRepository;
import deso.future_bot.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    protected final UserRepository userRepository;

    protected final PasswordEncoder passwordEncoder;

    protected final UserMapper mapper;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    public UserDto createUser(AddUser user) {
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        return mapper.toDto(userRepository.save(mapper.create(user)));
    }

    public Optional<UserDto> updatePassword(String password) {
        if (SecurityUtils.getId() == null) {
            return Optional.empty();
        }
        return userRepository
                .findById(SecurityUtils.getId())
                .map(
                        user -> {
                            user.setPassword(passwordEncoder.encode(password));
                            userRepository.save(user);
                            return mapper.toDto(user);
                        }
                );
    }

    public Optional<UserDto> update(UserDto userDTO) {
        if (SecurityUtils.getId() == null) {
            return Optional.empty();
        }
        return Optional
                .of(userRepository.findById(SecurityUtils.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(
                        user -> {
                            mapper.partialUpdate(user, userDTO);
                            userRepository.save(user);
                            log.debug("Changed Information for User: {}", user);
                            return user;
                        }
                ).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper::toDto);
    }

}
