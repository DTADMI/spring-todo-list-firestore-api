package ca.dtadmi.todolist.exceptions;

public class FirestoreExcecutionException extends RuntimeException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public FirestoreExcecutionException(String errorMessage){
        super(errorMessage);
    }
    public FirestoreExcecutionException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
    public FirestoreExcecutionException(Throwable cause) {
        super(cause);
    }
}
