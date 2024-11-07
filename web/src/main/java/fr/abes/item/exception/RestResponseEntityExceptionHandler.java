package fr.abes.item.exception;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.exception.*;
import lombok.extern.slf4j.Slf4j;
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
	private ResponseEntity<Object> buildResponseEntity(ApiReturnError apiReturnError) {
		return new ResponseEntity<>(apiReturnError, apiReturnError.getStatus());
	}

	@ExceptionHandler({ForbiddenException.class, UserExistException.class})
	public ResponseEntity<?> handleForbiddenFailures(Throwable t) {
		return buildResponseEntity(new ApiReturnError(HttpStatus.FORBIDDEN, t.getMessage(), t));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<?> handleBadCredentials(Throwable t) { return buildResponseEntity(new ApiReturnError(HttpStatus.UNAUTHORIZED, t.getMessage(), t)); }

	@ExceptionHandler({ IllegalArgumentException.class, FileCheckingException.class, FileTypeException.class})
	public ResponseEntity<?> handleMiscFailures(Throwable t) {
		return buildResponseEntity(new ApiReturnError(HttpStatus.BAD_REQUEST, Constant.ERR_FILE_WRONGCONTENT, t));
	}

	@ExceptionHandler({ DemandeCheckingException.class })
	public ResponseEntity<?> handleBadConditionsFailures(Throwable t) { return buildResponseEntity(new ApiReturnError(HttpStatus.PRECONDITION_FAILED, t.getMessage(), t));}

	@ExceptionHandler({ UnknownDemandeException.class })
	public ResponseEntity<?> handleUnknownDemande(Throwable t) { return buildResponseEntity(new ApiReturnError(HttpStatus.BAD_REQUEST, t.getMessage(), t));}

	@ExceptionHandler({ CBSException.class, ZoneException.class, QueryToSudocException.class })
	public ResponseEntity<?> handleBadRequestFailures(Throwable t) {return buildResponseEntity(new ApiReturnError(HttpStatus.BAD_REQUEST, t.getMessage(), t));}

	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<?> handleConstraintFailures(Throwable t) {return buildResponseEntity(new ApiReturnError(HttpStatus.BAD_REQUEST, t.getMessage(), t));}

	@ExceptionHandler({StorageFileNotFoundException.class})
	public ResponseEntity<?> handleStorageFileNotFound(Throwable t) { return buildResponseEntity(new ApiReturnError(HttpStatus.NOT_FOUND, t.getMessage(), t));}

	@ExceptionHandler({WsAuthException.class})
	public ResponseEntity<?> handleWsAuthException(Throwable t) { return buildResponseEntity(new ApiReturnError(HttpStatus.BAD_GATEWAY, t.getMessage(), t));}


}
