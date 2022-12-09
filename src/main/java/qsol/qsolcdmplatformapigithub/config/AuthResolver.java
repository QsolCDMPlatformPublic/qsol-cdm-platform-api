package qsol.qsolcdmplatformapigithub.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import qsol.qsolcdmplatformapigithub.config.data.LoginUser;
import qsol.qsolcdmplatformapigithub.config.data.UserSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Slf4j
@RequiredArgsConstructor
@Component
public class AuthResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        log.info("===== supportsParameter =====");

        boolean hasParameterAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        boolean hasUserSessionType = UserSession.class.isAssignableFrom(parameter.getParameterType());

        return hasParameterAnnotation && hasUserSessionType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        log.info("===== resolveArgument =====");

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return session.getAttribute("loginUser");
    }
}
