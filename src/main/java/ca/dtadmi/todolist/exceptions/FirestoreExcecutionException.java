package ca.dtadmi.todolist.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FirestoreExcecutionException extends RuntimeException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final int errorCode;

    public FirestoreExcecutionException(String errorMessage){
        super(errorMessage);
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
    public FirestoreExcecutionException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
    public FirestoreExcecutionException(Throwable cause) {
        super(cause);
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
    public FirestoreExcecutionException(String errorMessage, int errorCode){
        super(errorMessage);
        this.errorCode = errorCode;
    }
    public FirestoreExcecutionException(String errorMessage, int errorCode, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
    }
    public FirestoreExcecutionException(int errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }
}
