package fr.abes.item.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

	private final LoginAttemptService loginAttemptService;

	private final HttpServletRequest request;

	public AuthenticationFailureListener(HttpServletRequest request, LoginAttemptService loginAttemptService) {
		this.request = request;
		this.loginAttemptService = loginAttemptService;
	}

	@Override
    public void onApplicationEvent(final AuthenticationFailureBadCredentialsEvent e) {
		final String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null) {
			loginAttemptService.loginFailed(request.getRemoteAddr());
		} else {
			loginAttemptService.loginFailed(xfHeader.split(",")[0]);
		}
    }

}
