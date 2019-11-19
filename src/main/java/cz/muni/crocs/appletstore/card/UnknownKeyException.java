package cz.muni.crocs.appletstore.card;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Exception to be thrown on unknown key
 * indicates that user should've been asked for keys to provide
 */
public class UnknownKeyException extends Exception {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private String cardId;

    public UnknownKeyException(String cardId) {
        this.cardId = cardId;
    }

    @Override
    public String getMessage() {
        return "Unknown key for the card: " + cardId;
    }

    @Override
    public String getLocalizedMessage() {
        return textSrc.getString("E_master_key_not_found");
    }
}
