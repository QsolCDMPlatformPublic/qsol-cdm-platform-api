package qsol.qsolcdmplatformapigithub.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qsol.qsolcdmplatformapigithub.config.data.LoginUser;
import qsol.qsolcdmplatformapigithub.config.data.UserSession;
import qsol.qsolcdmplatformapigithub.config.data.Validator;
import qsol.qsolcdmplatformapigithub.dto.request.CreateUserRequest;
import qsol.qsolcdmplatformapigithub.dto.request.LoginUserRequest;
import qsol.qsolcdmplatformapigithub.dto.response.CreateUserResponse;
import qsol.qsolcdmplatformapigithub.dto.response.LoginUserResponse;
import qsol.qsolcdmplatformapigithub.dto.response.UserResponse;
import qsol.qsolcdmplatformapigithub.exception.user.UnauthorizedException;
import qsol.qsolcdmplatformapigithub.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final Validator validator;

    @PostMapping("/api/users")
    public ResponseEntity<CreateUserResponse> saveUser(@RequestBody @Valid CreateUserRequest request) {
        CreateUserResponse response = userService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<UserResponse>> findUsers(@LoginUser UserSession userSession) {
        validator.validateLogin(userSession);

        List<UserResponse> response = userService.getUsers();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginUserResponse> login(@RequestBody @Valid LoginUserRequest request, HttpServletRequest httpServletRequest) {
        LoginUserResponse response = userService.login(request);

        /*
            세션 저장.
         */
        HttpSession session = httpServletRequest.getSession();
        UserSession userSession = new UserSession(response);
        session.setAttribute("loginUser", userSession);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/auth/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
