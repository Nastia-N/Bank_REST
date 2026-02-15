package com.nastian.bankcards.service;

import com.nastian.bankcards.dto.AuthRequest;
import com.nastian.bankcards.entity.User;
import com.nastian.bankcards.entity.UserRole;
import com.nastian.bankcards.exception.DuplicateResourceException;
import com.nastian.bankcards.exception.UserNotFoundException;
import com.nastian.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления пользователями.
 * <p>
 * Предоставляет функциональность для:
 * <ul>
 *   <li>Регистрации пользователей</li>
 *   <li>Поиска пользователей по ID и username</li>
 *   <li>Проверки уникальности данных</li>
 * </ul>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request данные регистрации
     * @return созданный пользователь
     * @throws DuplicateResourceException если username или email уже заняты
     */
    @Transactional
    public User register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email", request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.USER);

        return userRepository.save(user);
    }

    /**
     * Поиск пользователя по ID.
     *
     * @param id ID пользователя
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Поиск пользователя по имени.
     *
     * @param username имя пользователя
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username, "username"));
    }

    /**
     * Проверка существования пользователя по имени.
     *
     * @param username имя пользователя
     * @return true если существует
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}