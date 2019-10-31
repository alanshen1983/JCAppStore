package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.InstallDialogWindow;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.crypto.Signature;
import cz.muni.crocs.appletstore.crypto.SignatureImpl;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.CAPFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static javax.swing.JOptionPane.*;
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
    private CAPFile code;
    private AppletInfo info;
    private String titleBar;
    private String signer;
    private String identifier;
    private boolean fromCustomFile = false;

    /**
     * Create an install action
     * @param titleBar title of the dialog
     * @param info info about the applet to install
     * @param capfile file with the compiled sourcecode
     * @param installed whether installed on the card already
     * @param signer signer's name
     * @param identifier signer's identifier, can be either his email or key ID
     * @param call callback that is called before action and after failure or after success
     */
    public InstallAction(String titleBar, AppletInfo info, File capfile, boolean installed, String signer,
                         String identifier, OnEventCallBack<Void, Void> call) {
        super(call);
        this.installed = installed;
        this.capfile = capfile;
        this.titleBar = titleBar;
        this.signer = signer;
        this.identifier = identifier;
        this.info = info;
    }

    public InstallAction(OnEventCallBack<Void, Void> call) {
        this("", null, null, false, null, "", call);
        this.fromCustomFile = true;
    }

    public InstallAction(String titleBar, AppletInfo info, File capfile, String signer,
                         String identifier, OnEventCallBack<Void, Void> call) {
        this(titleBar, info, capfile, false, signer, identifier, call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!CardManagerFactory.getManager().isCard()) {
            InformerFactory.getInformer().showWarning(textSrc.getString("missing_card"),
                    Warning.Importance.SEVERE, Warning.CallBackIcon.CLOSE, null, 7000);
            return;
        }

        if (fromCustomFile) capfile = CapFileChooser.chooseCapFile(Config.APP_LOCAL_DIR);
        code = CapFileChooser.getCapFile(capfile);
        if (code == null) {
            return;
        }
        checkIfEnoughSpaceAndInstall(call);
//        if (installed) {
//            doInstall();
//        } else {
//            checkIfEnoughSpaceAndInstall(call);
//        }
    }

    private void checkIfEnoughSpaceAndInstall(OnEventCallBack<Void, Void> call) {
        FreeMemoryAction memoryAction = new FreeMemoryAction(new OnEventCallBack<Void, byte[]>() {
            @Override
            public void onStart() {
                call.onStart();
            }

            @Override
            public void onFail() {
                call.onFail();
            }

            @Override
            public Void onFinish() {
                call.onFinish();
                return null;
            }

            @Override
            public Void onFinish(byte[] value) {
                //todo if value == null show info failed to get card memory
                if (value == null) {
                    call.onFinish();
                    doInstall();
                    return null;
                }
                int cardMemory = FreeMemoryAction.getAvailableMemory(value);
                long size;
                try {
                    size = capfile.length();
                } catch (SecurityException sec) {
                    sec.printStackTrace();
                    return onFinish();
                }
                call.onFinish();
                //todo compute real free memory based on installed stuff
                if (size > cardMemory) {
                    int res = JOptionPane.showConfirmDialog(null, "TODO add icons and custom buttons, the size of application is more then remaining card storage " + value + ", " +
                            "the installation might fail. continue anyway?");
                    if (res == YES_OPTION) {
                        doInstall();
                    } else {
                        return null;
                    }
                } else {
                    doInstall();
                }
                return null;
            }
        });
        memoryAction.mouseClicked(null);
    }

    private void doInstall() {
        if (fromCustomFile) {
            verifyCustomInstallationAndShowInstallDialog();
        } else {
            verifyStoreInstallationAndShowInstallDialog();
        }
    }

    private void verifyCustomInstallationAndShowInstallDialog() {
        final InstallDialogWindow dialogWindow = showInstallDialog(textSrc.getString("custom_file"), "verify_no_pgp.png", true);
        if (dialogWindow == null) return;
        final File customSign = dialogWindow.getCustomSignatureFile();
        if (customSign != null) {
            verifySignatureRoutine(new Executable() {
                @Override
                void work() {
                    final Signature signature = new SignatureImpl();
                    try {
                        result = signature.verifyPGPAndReturnMessage(null, capfile, customSign);
                    } catch (LocalizedSignatureException e) {
                        result = new Tuple<>("not_verified.png", e.getLocalizedMessage());
                    }
                }

                @Override
                void after() {
                    int choice = JOptionPane.showConfirmDialog(null,
                            "<html><div width=\"350\">" + result.second + "<br>" +
                                    textSrc.getString("install_ask") + "</div></html>",
                            textSrc.getString("signature_title_dialog") ,
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            new ImageIcon(Config.IMAGE_DIR + result.first));
                    if (choice == YES_OPTION) {
                        fireInstall(dialogWindow.getInstallOpts());
                    }
                }
            });
        } else {
            fireInstall(dialogWindow.getInstallOpts());
        }
    }

    private void verifyStoreInstallationAndShowInstallDialog() {
        verifySignatureRoutine(new Executable() {
            @Override
            void work() {
                final Signature signature = new SignatureImpl();
                try {
                    result = signature.verifyPGPAndReturnMessage("JCAppStore", capfile);
                    if (signer != null && !signer.isEmpty()) {
                        Tuple<String, String> another = signature.verifyPGPAndReturnMessage(signer, capfile);
                        result = new Tuple<>(another.first, "JCAppStore: " + result.second + "<br>" + signer + ": " + another.second);
                    }
                } catch (LocalizedSignatureException e) {
                    result = new Tuple<>("not_verified.png", e.getLocalizedMessage());
                }
            }

            @Override
            void after() {
                InstallDialogWindow dialogWindow = showInstallDialog(result.second, result.first, false);
                if (dialogWindow == null) return;
                fireInstall(dialogWindow.getInstallOpts());
            }
        });
    }

    private InstallDialogWindow showInstallDialog(String verifyResult, String imgIcon, boolean buildCustomInstall) {
        InstallDialogWindow dialog = new InstallDialogWindow(code, info, installed, verifyResult);
        String[] buttons = new String[]{textSrc.getString("install"), textSrc.getString("cancel")};

        JOptionPane pane = new JOptionPane(dialog, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
                new ImageIcon(Config.IMAGE_DIR + imgIcon), buttons, "error");
        JDialog window = pane.createDialog(textSrc.getString("CAP_install_applet") + titleBar);
        if (buildCustomInstall) dialog.buildAdvancedAndCustomSigned(window);
        else dialog.buildAdvanced(window);
        window.pack();
        window.setVisible(true);

        window.dispose();
        int selectedValue = getSelectedValue(buttons, pane.getValue() );//waiting line

        switch (selectedValue) {
            case JOptionPane.YES_OPTION:
                if (!dialog.validCustomAID() || !dialog.validInstallParams()) {
                    InformerFactory.getInformer().showInfo(textSrc.getString("E_install_invalid_data"));
                    return showInstallDialog(verifyResult, imgIcon, buildCustomInstall);
                }
                break;
            case JOptionPane.NO_OPTION:
            case CLOSED_OPTION:
                return null;
        }
        return dialog;
    }

    private void fireInstall(final InstallOpts opts) {
        logger.info("Install fired, list of AIDS: " + code.getApplets().toString());
        logger.info("Install AID: " + opts.getAID());

        final CardManager manager = CardManagerFactory.getManager();
        //if easy mode && package already present
        if (!OptionsFactory.getOptions().isVerbose()) {
            //if applet present dont change anything
            if (manager.getInstalledApplets().stream().noneMatch(a -> a.getKind() != Kind.ExecutableLoadFile && a.getAid().equals(opts.getAID()))) {
                if (manager.getInstalledApplets().stream().anyMatch(a -> a.getKind() == Kind.ExecutableLoadFile && a.getAid().equals(code.getPackageAID()))) {
                    opts.setForce(true);
                }
            }
        }

        execute(() -> {
            manager.install(code, opts);
            manager.setLastAppletInstalled(opts.getAID());
            SwingUtilities.invokeLater(() -> {
                InformerFactory.getInformer().showWarning(textSrc.getString("installed"), Warning.Importance.INFO, Warning.CallBackIcon.CLOSE, null, 4000);
            });
            capfile = null;
        }, "Failed to install applet.", textSrc.getString("install_failed"));
    }

    //copied from JOptionPane to parse the JOptionPane return value
    private int getSelectedValue(Object[] options, Object selectedValue) {
        Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (fo != null && fo.isShowing()) {
            fo.requestFocus();
        }
        if (selectedValue == null) {
            return CLOSED_OPTION;
        }
        if (options == null) {
            if (selectedValue instanceof Integer) {
                return (Integer) selectedValue;
            }
            return CLOSED_OPTION;
        }
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if (options[counter].equals(selectedValue)) {
                return counter;
            }
        }
        return CLOSED_OPTION;
    }

    private static void verifySignatureRoutine(Executable task) {
        JOptionPane pane = new JOptionPane(textSrc.getString("H_pgp_loading"),
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION,
                new ImageIcon(Config.IMAGE_DIR + "verify_loading.png"),
                new Object[]{}, null);

        JDialog dialog = pane.createDialog(null, textSrc.getString("wait_sec"));
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                task.work();
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                task.after();
            }
        }.execute();
        dialog.setVisible(true);
    }

    private abstract class Executable {
        Tuple<String, String> result;
        void setResult(Tuple<String, String> result) {
            this.result = result;
        }
        abstract void work();
        abstract void after();
    }
}