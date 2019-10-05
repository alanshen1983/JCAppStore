package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.InstallDialogWindow;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.crypto.KeyBase;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import static pro.javacard.gp.GPRegistryEntry.Kind;


/**
 * Class to add to button as listener target to perform applet installation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallAction extends CardAction {
    private static final Logger logger = LoggerFactory.getLogger(InstallAction.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private boolean installed;
    private File capfile;
    private AppletInfo info;
    private String titleBar;
    private boolean fromCustomFile = false;

    public InstallAction(String titleBar, AppletInfo info, File capfile, boolean installed,
                         OnEventCallBack<Void, Void, Void> call) {
        super(call);
        this.installed = installed;
        this.capfile = capfile;
        this.titleBar = titleBar;
        this.info = info;
    }

    public InstallAction(OnEventCallBack<Void, Void, Void> call) {
        this("", null, null, false, call);
        this.fromCustomFile = true;
    }

    public InstallAction(String titleBar, AppletInfo info, File capfile, OnEventCallBack<Void, Void, Void> call) {
        this(titleBar, info, capfile, false, call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (capfile == null) capfile = CapFileChooser.chooseCapFile(Config.APP_LOCAL_DIR);

        if (fromCustomFile) {
            showInstallDialog("custom_file", "verify_no_keybase.png");
            return;
        }

        JOptionPane pane = new JOptionPane(textSrc.getString("H_keybase_loading"),
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION,
                new ImageIcon(Config.IMAGE_DIR + "verify_loading.png"),
                new Object[]{}, null);

        JDialog dialog = pane.createDialog(null, textSrc.getString("wait_sec"));
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        new SwingWorker<Void, Void>() {
            private Tuple<String, String> result;

            @Override
            public Void doInBackground() {
                try {
                    result = new KeyBase().verifySignature(capfile.getAbsolutePath());
                } catch (LocalizedSignatureException e) {
                    e.printStackTrace();
                    result = new Tuple<>("not_verified.png", textSrc.getString("H_verify_failed")
                            + (OptionsFactory.getOptions().getOption(Options.KEY_ERROR_MODE).equals("verbose") ?
                            e.getLocalizedMessage() : e.getLocalizedMessageWithoutCause()));
                }
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                showInstallDialog(result.second, result.first);
            }
        }.execute();
        dialog.setVisible(true);
    }

    private void showInstallDialog(String verifyResult, String imgIcon) {
        CAPFile file = CapFileChooser.getCapFile(capfile);
        if (file == null)
            return;

        InstallDialogWindow dialog = new InstallDialogWindow(file, info, installed, verifyResult);
        int result = JOptionPane.showOptionDialog(null, dialog,
                textSrc.getString("CAP_install_applet") + titleBar,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + imgIcon),
                new String[]{textSrc.getString("install"), textSrc.getString("cancel")}, "error");

        switch (result) {
            case JOptionPane.YES_OPTION:
                if (!dialog.validAID() || !dialog.validInstallParams()) {
                    InformerFactory.getInformer().showInfo(textSrc.getString("E_install_invalid_data"));
                    showInstallDialog(verifyResult, imgIcon);
                    return;
                }
                break;
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return;
        }

        final InstallOpts opts = dialog.getInstallOpts();
        logger.info("Install fired, list of AIDS: " + file.getApplets().toString());
        logger.info("Install AID: " + opts.getAID());

        final CardManager manager = CardManagerFactory.getManager();
        //if easy mode && package already present
        if (!OptionsFactory.getOptions().isVerbose()) {
            //if applet present dont change anything
            if (manager.getInstalledApplets().stream().noneMatch(a -> a.getKind() != Kind.ExecutableLoadFile && a.getAid().equals(opts.getAID()))) {
                if (manager.getInstalledApplets().stream().anyMatch(a -> a.getKind() == Kind.ExecutableLoadFile && a.getAid().equals(file.getPackageAID()))) {
                    opts.setForce(true);
                }
            }
        }

        execute(() -> {
            manager.install(file, opts);
            manager.setLastAppletInstalled(opts.getAID());
        }, "Failed to install applet.", textSrc.getString("install_failed"));
    }
}
