package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.sources.Options;
import cz.muni.crocs.appletstore.sources.OptionsFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class BackgroundImgPanel extends JPanel {

    private BufferedImage bg;

    public BackgroundImgPanel() {
        String bgImagname = OptionsFactory.getOptions().getOption(Options.KEY_BACKGROUND);
        if (bgImagname == null) {
            loadDefault();
        } else {
            try {
                bg = ImageIO.read(new File(Config.APP_DATA_DIR + Config.SEP + bgImagname));
            } catch (IOException e) {
                e.printStackTrace();
                loadDefault();
            }
        }
    }

    public void setNewBackground(BufferedImage newBackground) {
        bg = newBackground;
    }

    private void loadDefault() {
//        //first run to blur the default image, otherwise just load
//        BackgroundImageLoader imgBuilder = new BackgroundImageLoader("bg.jpg", this);
//        bg = imgBuilder.get();
        try {
            bg = ImageIO.read(new File(Config.IMAGE_DIR + "bg.jpg"));
        } catch (IOException e) {
            //build white background
            //TODO show user unable to load default background
            bg = new BufferedImage(690, 540,BufferedImage.TYPE_INT_RGB);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //TODO maybe remember the last diemnstions and rescale only once
        Image scaledImage = bg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH);
        g.drawImage(scaledImage, 0, 0, this);
    }
}
