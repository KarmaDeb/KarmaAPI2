package es.karmadev.api.netty.encoder.exception;

/**
 * This exception is thrown when an
 * invalid message is received through
 * a {@link es.karmadev.api.netty.encoder.MessageHandler}
 */
public class RemoteEncoderException extends Exception {

    /**
     * Initialize the exception
     *
     * @param reason the reason
     */
    public RemoteEncoderException(final String reason) {
        super(reason);
    }
}
