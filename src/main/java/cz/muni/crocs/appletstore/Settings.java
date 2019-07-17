package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.BackgroundImageLoader;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.CustomComboBoxItem;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.util.Sources;
import cz.muni.crocs.appletstore.util.Tuple;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Settings extends JPanel {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private String bgImg = Sources.options.get(Config.OPT_KEY_BACKGROUND);
    private JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 8, 1);
    private final String defaultBgPath = Config.IMAGE_DIR + "bg.jpg";

    private final Tuple[] langs = new Tuple[]{
            new Tuple<>("en", "English"),
            new Tuple<>("cz", "Česky")
    };
    private JComboBox<Tuple<String, String>> languageBox;

    private JCheckBox hintEnabled = new JCheckBox();

    private AppletStore context;

    private CompoundBorder frame = BorderFactory.createCompoundBorder(
            new MatteBorder(new Insets(1, 1, 1, 1), Color.BLACK),
            new EmptyBorder(new Insets(4, 4, 4, 4)));

    public Settings(AppletStore context) {
        this.context = context;
        setPreferredSize(new Dimension(350, context.getHeight() / 2));
        setLayout(new MigLayout("fillx, gap 5px 5px"));
        addBackground();
        addLanguage();
        addHint();
    }

    private void addBackground() {
        addTitleLabel(textSrc.getString("background"), "span 3, wrap");

        String path = Sources.options.get(Config.OPT_KEY_BACKGROUND);
        if (path.equals(defaultBgPath)) {
            path = textSrc.getString("default");
            slider.setEnabled(false);
        }
        cutString(path);

        JLabel bgValue = new JLabel("<html>" + path + "</html>");
        bgValue.setFont(CustomFont.plain.deriveFont(12f));
        bgValue.setBorder(frame);
        bgValue.setBackground(Color.WHITE);
        bgValue.setOpaque(true);
        add(bgValue, "span 3, growx, wrap");

        add(new JLabel()); //empty space

        JButton defaultBg = new JButton(new AbstractAction(textSrc.getString("reset_default")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                bgImg = textSrc.getString("default");
                bgValue.setText(bgImg);
                slider.setValue(1);
                slider.setEnabled(false);
            }
        });
        add(defaultBg, "align right");

        JButton getNewBg = new JButton(new AbstractAction(textSrc.getString("change")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", ImageIO.getReaderFileSuffixes()));
                fileChooser.setAcceptAllFileFilterUsed(false);
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    bgImg = fileChooser.getSelectedFile().getAbsolutePath();
                    bgValue.setText(cutString(bgImg));
                    slider.setEnabled(true);
                }
            }
        });
        add(getNewBg, "align right, wrap");

        //blur option
        addTitleLabel(textSrc.getString("blur"), "");
        slider.setEnabled(false);
        add(slider, "w 180, align right, span 2, wrap");
    }

    private void addLanguage() {
        addTitleLabel(textSrc.getString("language"), "");

        languageBox = new JComboBox<>(langs);
        CustomComboBoxItem listItems = new CustomComboBoxItem();
        languageBox.setMaximumRowCount(4);
        languageBox.setRenderer(listItems);
        add(languageBox, "align right, span 2, w 180, wrap");
    }

    private void addHint() {
        addTitleLabel(textSrc.getString("enable_hints"), "");
        hintEnabled.setSelected(Sources.options.get(Config.OPT_KEY_HINT).equals("true"));
        add(hintEnabled, "align left, span 2, w 180, wrap");
    }

    private void addTitleLabel(String titleText, String constraints) {
        JLabel title = new JLabel(titleText);
        title.setFont(CustomFont.plain);
        add(title, constraints);
    }

    private String cutString(String value) {
        if (value.length() > 45) {
            int len = value.length();
            value = "..." + value.substring(len - 42, len);
        }
        return value;
    }


    private void saveBackgroundImage() {
        if (bgImg.equals(Sources.options.get(Config.OPT_KEY_BACKGROUND))) {
            return;
        }
        if (bgImg.equals(textSrc.getString("default"))) {
            try {
                Sources.options.put(Config.OPT_KEY_BACKGROUND, Config.IMAGE_DIR + "bg.jpg");
                ((BackgroundImgPanel) context.getContentPane()).setNewBackground(
                        ImageIO.read(new File(defaultBgPath)));
            } catch (IOException e) {
                //todo show error
                e.printStackTrace();
            }
        } else {
            BackgroundImageLoader loader = new BackgroundImageLoader(bgImg, this, slider.getValue());
            ((BackgroundImgPanel) context.getContentPane()).setNewBackground(loader.get());
        }
    }

    private void saveLanguage() {
        if (langs[languageBox.getSelectedIndex()].first.equals(Sources.options.get(Config.OPT_KEY_LANGUAGE))) return;
        Sources.options.put(Config.OPT_KEY_LANGUAGE, (String)langs[languageBox.getSelectedIndex()].first);
        showAlertChange();
        //Config.translation = new Translation(Config.options.get(Config.OPT_KEY_LANGUAGE));
    }

    private void saveHint() {
        Sources.options.put(Config.OPT_KEY_HINT, hintEnabled.isSelected() ? "true" : "false");
    }

    public void apply() {
        saveBackgroundImage();
        saveLanguage();
        saveHint();
    }

    /**
     * Change alert notification
     * display: changes will apply
     */
    private void showAlertChange() {
        JOptionPane.showMessageDialog(null,
                textSrc.getString("reset_to_apply"),
                textSrc.getString("reset_to_apply_title"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
