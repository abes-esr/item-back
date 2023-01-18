package fr.abes.item.security;

import fr.abes.item.constant.Constant;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        this.sendErrorGenericAuthorization();

        if (e.getMessage().contains(Constant.ERROR_ACCESS_DATABASE)) {
            this.sendErrorTechnicalProblems(httpServletResponse);
        } else if (e.getMessage().contains(Constant.BLOCKED)) {
            this.sendErrorAccountBlocked(httpServletResponse);
        } else {
            this.sendErrorAccessResourceNotAllowed(httpServletResponse);
        }
    }

    private void sendErrorGenericAuthorization(){
		log.error(Constant.ERROR_RESPONDING_WITH_UNAUTHORIZED_ERROR);
	}

    private void sendErrorTechnicalProblems(HttpServletResponse httpServletResponse) throws IOException{
		httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Constant.ERROR_GENERIC_TECHNICAL_PROBLEMS);
	}

	private void sendErrorAccountBlocked(HttpServletResponse httpServletResponse) throws IOException{
		httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, Constant.ERROR_ACCOUNT_BLOCKED);
	}

	private void sendErrorAccessResourceNotAllowed(HttpServletResponse httpServletResponse) throws IOException{
		httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, Constant.ERROR_ACCESS_RESSOURCE_NOT_ALLOWED);
	}

}
