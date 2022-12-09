package qsol.qsolcdmplatformapigithub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qsol.qsolcdmplatformapigithub.domain.User;
import qsol.qsolcdmplatformapigithub.dto.request.CreateUserRequest;
import qsol.qsolcdmplatformapigithub.dto.request.LoginUserRequest;
import qsol.qsolcdmplatformapigithub.dto.response.CreateUserResponse;
import qsol.qsolcdmplatformapigithub.dto.response.LoginUserResponse;
import qsol.qsolcdmplatformapigithub.dto.response.UserResponse;
import qsol.qsolcdmplatformapigithub.exception.user.InvalidLoginException;
import qsol.qsolcdmplatformapigithub.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public CreateUserResponse create(CreateUserRequest request) {

        User user = User.builder()
                .username(request.getUsername())
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .createdDate(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return new CreateUserResponse(savedUser);

    }

    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public LoginUserResponse login(LoginUserRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(InvalidLoginException::new);

        if (BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            return new LoginUserResponse(user);
        } else {
            throw new InvalidLoginException();
        }
    }

}
