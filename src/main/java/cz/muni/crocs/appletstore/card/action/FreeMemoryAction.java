package cz.muni.crocs.appletstore.card.action;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class FreeMemoryAction extends CardAction {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private static final Logger logger = LoggerFactory.getLogger(CardAction.class);
    private OnEventCallBack<Void, byte[]> customCall;

    public FreeMemoryAction(OnEventCallBack<Void, byte[]> call) {
        super(null);
        customCall = call;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        customCall.onStart();

        SwingWorker<byte[], Void> worker = new SwingWorker<byte[], Void>() {
            private LocalizedCardException e = null;

            @Override
            protected byte[] doInBackground() {
                try {
                    return CardCommands.getSystemInfo();
                } catch (LocalizedCardException ex) {
                    ex.printStackTrace();
                    logger.warn("Failed to obtain the free memory space: " + ex.getMessage());
                    e = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                if (e != null) {
                    customCall.onFinish(null);
                } else {
                    byte[] availableSpace = null;
                    try {
                        availableSpace = get();
                    } catch (InterruptedException | ExecutionException ex) {
                        //ignore
                        ex.printStackTrace();
                    }
                    customCall.onFinish(availableSpace);
                }
            }
        };

        worker.execute();
    }

    public static int getAvailableMemory() {
        try {
            byte[] bytes = CardCommands.getSystemInfo();
            if (bytes == null)
                return -1;
            return getAvailableMemory(bytes);
        } catch (LocalizedCardException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int toUnsigned(byte b) {
        if ((b & 0x80) == 0x80) {
            return 128 + (b & 0x7F);
        }
        return b;
    }

    public static int getAvailableMemory(byte[] response) {
        //big endian
        return (toUnsigned(response[0]) << 8) + toUnsigned(response[1]);
    }
}
