package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.BackgroundImgSplitPanel;
import cz.muni.crocs.appletstore.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Main panel of the application, holds the left menu and working panes
 * takes care of displaying the information boxes
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class MainPanel extends BackgroundImgSplitPanel implements Informable {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private JPanel content;
    private LocalWindowPane localPanel;
    private StoreWindowManager storePanel;
    private Component current = null;
    private LoggerConsole console;

    /**
     * Create a main panel containing left menu, store, my card panels
     * @param context
     */
    public MainPanel(BackgroundChangeable context) {
        //there was a problem with focus when using search feature, request focus
        requestFocusInWindow();
        setOneTouchExpandable(true);
        setDividerLocation(150);

        OnEventCallBack<Void, Void> callback = new WorkCallback(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.switchEnabled(false);
            }
        }, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.switchEnabled(true);
            }
        }, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.switchEnabled(true);
                localPanel.refresh();
            }
        });

        localPanel = new LocalWindowPane(callback);
        storePanel = new StoreWindowManager(callback);

        buildStoreContents();
        buildLogger();
        InformerFactory.setInformer(this);
    }

    public void toggleLogger() {
        setDividerSize(getBottomComponent() == null ? 15 : 0);
        setBottomComponent(getBottomComponent() == null ? (LoggerConsoleImpl)console : null);
    }

    /**
     * Build store, upper part of the split pane
     */
    private void buildStoreContents() {
        content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        JPanel pages = new JPanel();
        pages.setLayout(new OverlayLayout(pages));
        pages.setOpaque(false);
        pages.add(localPanel);
        pages.add(storePanel);
        LeftMenu leftMenu = new LeftMenu(this);
        localPanel.registerSearchBar(leftMenu);
        storePanel.registerSearchBar(leftMenu);

        setOpaque(false);
        content.add(leftMenu, BorderLayout.WEST);
        content.add(pages, BorderLayout.CENTER);
        content.setMinimumSize(new Dimension(content.getMinimumSize().width, 250));

        setLocalPanelVisible();
        setLeftComponent(content);
    }

    void buildLogger() {
        console = new LoggerConsoleImpl();
        setDividerSize(0);
        setBottomComponent(null);
    }

    /**
     * Switch to local panel
     */
    public void setLocalPanelVisible() {
        localPanel.setVisible(true);
        storePanel.setVisible(false);
    }

    /**
     * Switch to store panel and call store update
     */
    public void setStorePaneVisible() {
        localPanel.setVisible(false);
        storePanel.setVisible(true);
        storePanel.updateGUI(); //always
    }

    /**
     * Get searchable panel: wither store or local panel - depends
     * on where to perform the searching
     * @return currently visible panel
     */
    public Searchable getSearchablePane() {
        return (storePanel.isVisible()) ? storePanel : localPanel;
    }

    /**
     * Get 'my card' panel
     * @return panel for 'my card'
     */
    public LocalWindowPane getRefreshablePane() {
        return localPanel;
    }

    @Override
    public void showMessage(String info) {
        if (info == null || info.isEmpty())
            return;
        JOptionPane.showMessageDialog(this,
                "<html><div width=\"350\">" + info + "</div></html>",
                textSrc.getString("info"),
                JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + "info.png"));
    }

    @Override
    public void showFullScreenInfo(JPanel pane) {
        localPanel.showError(pane);
    }

    @Override
    public void showInfo(JComponent component) {
        current = component;
        content.add(current, BorderLayout.NORTH);
        content.revalidate();
    }

    @Override
    public void hideInfo() {
        if (current == null) return;
        content.remove(current);
        content.revalidate();
        content.repaint();

        current = null;
    }
}
