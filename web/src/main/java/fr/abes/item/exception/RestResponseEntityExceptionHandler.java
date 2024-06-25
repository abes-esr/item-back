package fr.abes.item.exception;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.support.ExceptionMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ForbiddenException.class, UserExistException.class})
	public ResponseEntity<?> handleForbiddenFailures(Throwable t) {
		return errorResponse(t, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<?> handleBadCredentials(Throwable t) { return errorResponse(t, HttpStatus.UNAUTHORIZED); }

	@ExceptionHandler({ IllegalArgumentException.class, FileCheckingException.class, FileTypeException.class})
	public ResponseEntity<?> handleMiscFailures(Throwable t) {
		return errorResponse(t, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({ DemandeCheckingException.class })
	public ResponseEntity<?> handleBadConditionsFailures(Throwable t) { return errorResponse(t, HttpStatus.PRECONDITION_FAILED);}

	@ExceptionHandler({ CBSException.class, ZoneException.class, QueryToSudocException.class })
	public ResponseEntity<?> handleBadRequestFailures(Throwable t) {return errorResponse(t, HttpStatus.BAD_REQUEST);}

	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<?> handleConstraintFailures(Throwable t) {return errorResponse(t, HttpStatus.BAD_REQUEST);}

	@ExceptionHandler({StorageFileNotFoundException.class})
	public ResponseEntity<?> handleStorageFileNotFound(Throwable t) { return errorResponse(t, HttpStatus.NOT_FOUND);}

	@ExceptionHandler({WsAuthException.class})
	public ResponseEntity<?> handleWsAuthException(Throwable t) { return errorResponse(t, HttpStatus.BAD_GATEWAY);}

	protected ResponseEntity<ExceptionMessage> errorResponse(Throwable throwable, HttpStatus status) {
		if (null != throwable) {
			log.error(Constant.ERROR_CAUGHT + throwable.getMessage());
			return response(new ExceptionMessage(throwable), status);
		} else {
			log.error(Constant.ERROR_UNKNOWN_REST_CONTROLLER, status);
			return response(null, status);
		}
	}

	protected <T> ResponseEntity<T> response(T body, HttpStatus status) {
		log.debug(Constant.REST_RESPONDING_WITH_STATUS, status);
		return new ResponseEntity<>(body, new HttpHeaders(), status);
	}
}
