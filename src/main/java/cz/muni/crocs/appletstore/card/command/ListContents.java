package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Parses all installed applets to display them in app
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ListContents extends GPCommand<Set<AppletInfo>> {
    private static Logger logger = LoggerFactory.getLogger(ListContents.class);
    private String cardId;

    public ListContents(String cardId) {
        this.cardId = cardId;
    }

    @Override
    public boolean execute() throws CardException, GPException, IOException {
        result = new AppletSet();
        GPRegistry registry = context.getRegistry();
        if (registry == null || cardId == null) return false;

        logger.info("List contents of card: " + cardId);
        AppletSerializer<Set<AppletInfo>> savedData = new AppletSerializerImpl();
        File file = new File(Config.APP_DATA_DIR + Config.S + cardId);

        Set<AppletInfo> saved;
        if (file.exists()) {
            try {
                saved = savedData.deserialize(file);
            } catch (LocalizedCardException e) {
                e.printStackTrace();
                logger.warn("Failed to obtain card cache file", e);
                saved = Collections.emptySet();
            }
        } else {
            saved = Collections.emptySet();
        }

        for (GPRegistryEntry entry : registry) {
            result.add(new AppletInfo(entry, saved));
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "List contents method for: " + cardId;
    }
}
