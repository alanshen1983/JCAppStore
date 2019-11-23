package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.TextField;

import java.util.Locale;
import java.util.ResourceBundle;

public class HelpBuilder extends Help {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private float title;
    private float subtitle;
    private String css;

    public HelpBuilder(float titleSize, float subtitleSize, int paragraphWith) {
        this.title = titleSize;
        this.subtitle = subtitleSize;
        this.css = "width: " + paragraphWith + "px;";
    }

    public HelpBuilder addTitle(String textResourceKey) {
        add(getLabel(textSrc.getString(textResourceKey), title), "wrap");
        return this;
    }

    public HelpBuilder addSubTitle(String textResourceKey) {
        add(getLabel(textSrc.getString(textResourceKey), subtitle), "wrap");
        return this;
    }

    public HelpBuilder addText(String textResourceKey) {
        add(TextField.getTextField(textSrc.getString("cmd_introduction"), css, null), "gapleft 10, wrap");
        return this;
    }
}