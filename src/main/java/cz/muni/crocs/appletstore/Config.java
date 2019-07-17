package cz.muni.crocs.appletstore;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Config {

    //options map and key values
    public static final String OPT_KEY_LANGUAGE = "lang";
    public static final String OPT_KEY_GITHUB_LATEST_VERSION = "github.latest.version";
    public static final String OPT_KEY_BACKGROUND = "background";
    public static final String OPT_KEY_HINT = "hint";
    //system path separator
    public static final String SEP = File.separator;

    //app external folders
    public static final String APP_ROOT_DIR = getDefaultAppRootFolder();
    public static final File APP_DATA_DIR = checkFolders(APP_ROOT_DIR + SEP + "data");
    public static final File APP_STORE_DIR = checkFolders(APP_ROOT_DIR + SEP + "store");
    public static final File APP_STORE_CAPS_DIR = checkFolders(APP_ROOT_DIR + SEP + "store" + SEP + "JCApplets");
    public static final File APP_LOCAL_DIR = checkFolders(APP_ROOT_DIR + SEP + "my_applets");

    //app internal dirs
    public static final String IMAGE_DIR = "src"+SEP+"main"+SEP+"resources"+SEP+"img"+SEP;
    public static final String LANG_DIR = "src"+SEP+"main"+SEP+"resources"+SEP+"lang"+SEP;

    //database related constants
    public static final String JC_DB_FILE = "jcappstore.db";
    public static final String INI_CARD_LIST = Config.APP_DATA_DIR + Config.SEP + "cards.ini";
    public static final String INI_CARD_TYPES = "src"+SEP+"main"+SEP+"resources"+SEP+"data"+SEP+"types.ini";

    //ini database constants
    public static final String INI_NAME = "name";
    public static final String INI_KEY = "key";
    public static final String INI_KEY_TYPE = "type";
    public static final String INI_DIVERSIFIER = "diversifier";
    public static final String INI_AUTHENTICATED = "auth";
    public static final String INI_ATR = "atr";
    public static final String INI_CIN = "cin";
    public static final String INI_IIN = "iin";
    public static final String INI_CPLC = "cplc";
    public static final String INI_DATA = "card_data";
    public static final String INI_CAPABILITIES = "card_capabilities";
    public static final String INI_KEY_INFO = "key_info";
    public static final String INI_INSTALLED = "applets";

    //store constants
    public static final String REMOTE_STORE_URL = "https://github.com/petrs/JCAppStoreContent.git";
    public static final String REMOTE_STORE_LATEST_URL = "https://api.github.com/repos/petrs/JCAppStoreContent/releases/latest";
    public static final String FILE_INFO_PREFIX = "info_";
    public static final String FILE_INFO_SUFFIX = ".json";
    public static final String JSON_TAG_NAME = "name";
    public static final String JSON_TAG_TITLE = "title";
    public static final String JSON_TAG_ICON = "icon";
    public static final String JSON_TAG_LATEST = "latest";
    public static final String JSON_TAG_VERSION = "versions";
    public static final String JSON_TAG_BUILD = "builds";
    public static final String JSON_TAG_AUTHOR = "author";
    public static final String JSON_TAG_DESC = "description";
    public static final String JSON_TAG_URL = "url";
    public static final String JSON_TAG_USAGE = "usage";
    public static final String RESOURCES = Config.APP_STORE_DIR + Config.SEP + "Resources" + Config.SEP;

    /**
     * Gets the default app folder root location
     * @return
     */
    public static String getDefaultAppRootFolder() {
        //from  https://stackoverflow.com/questions/8782797/creating-directory-in-application-support-or-appdata
        String appFolder = FileSystemView.getFileSystemView().getDefaultDirectory() + SEP + "JCAppStore";
        File directory = new File(appFolder);
        if (!directory.exists() && (!directory.mkdirs())) {
            throw new RuntimeException("The Application doesn't have the rights to store data into default folder.");
        }
        return appFolder;
    }

    private static File checkFolders(String folder) {
        File f = new File(folder);
        if (!f.exists()) {
            if (!f.mkdir()) {
                return null;
            }
        }
        if (!f.isDirectory()) return null;
        return f;
    }
}
