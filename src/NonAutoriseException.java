import java.rmi.server.ExportException;

public class NonAutoriseException extends Exception {
    public NonAutoriseException(String message) {
        super(message);
    }
}
