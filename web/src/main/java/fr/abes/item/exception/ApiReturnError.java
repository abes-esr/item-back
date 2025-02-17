package fr.abes.item.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.abes.item.core.constant.Constant;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Représente une erreur au format JSON de l'API
 */
@Data
public class ApiReturnError {
    private HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;


    private ApiReturnError() {
        timestamp = LocalDateTime.now();
    }


    ApiReturnError(HttpStatus status, String message, Throwable ex) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getMessage();
    }

    ApiReturnError(String fileCheckingError, HttpStatus status, String message, Throwable ex) {
        this();
        if (ex.getMessage().contains(Constant.ERR_FILE_TOOMUCH_START)) {
            this.status = status;
            this.message = ex.getMessage();
            this.debugMessage = ex.getMessage();
        } else {
            this.status = status;
            this.message = message;
            this.debugMessage = ex.getMessage();
        }
    }
}
