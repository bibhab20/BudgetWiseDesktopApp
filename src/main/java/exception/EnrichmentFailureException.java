package exception;

public class EnrichmentFailureException extends Exception{
    /**
     * Constructor that takes a message.
     */
    public EnrichmentFailureException(String message) {
        super(message);
    }

    /**
     * Constructor with no parameters
     */
    public EnrichmentFailureException(){
        super();
    }

    /**
     * Constructor that takes a message and a cause exception.
     */
    public EnrichmentFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor that takes a cause exception.
     */
    public EnrichmentFailureException(Throwable cause) {
        super(cause);
    }

}
