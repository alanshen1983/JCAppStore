package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.iface.CallBack;
import sun.misc.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class ErrorPane extends JPanel {

    public ErrorPane (int translationId, String imgName) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel error = new JLabel(new ImageIcon(Config.IMAGE_DIR + imgName));
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        error.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(error);
        JLabel errorMsg = new JLabel(Config.translation.get(translationId));
        errorMsg.setFont(CustomFont.plain.deriveFont(20f));
        errorMsg.setForeground(Color.WHITE);
        errorMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorMsg);
    }

    public ErrorPane (int translationId, String imgName, CallBack callable) {
        this(translationId, imgName);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel(new ImageIcon(Config.IMAGE_DIR + "sync.png"));
        panel.add(icon);

        JLabel retry = new JLabel(Config.translation.get(112));
        retry.setFont(CustomFont.plain.deriveFont(16f));
        retry.setForeground(Color.WHITE);
        panel.add(retry);

        panel.setOpaque(false);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                callable.callBack();
            }
        });
        add(panel);
    }

    public ErrorPane (int translationId, int hintId, String imgName) {
        this(translationId, Config.translation.get(hintId), imgName);
    }

    public ErrorPane (int translationId, String message, String imgName) {
        this(translationId, imgName);

        JLabel hint = new JLabel("<html><p width=\"400\" align=\"center\">" + message + "</p></html>");
        hint.setBorder(new EmptyBorder(20, 20, 20, 20));
        hint.setFont(CustomFont.plain.deriveFont(16f));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(Color.WHITE);
        add(hint);
    }
}
