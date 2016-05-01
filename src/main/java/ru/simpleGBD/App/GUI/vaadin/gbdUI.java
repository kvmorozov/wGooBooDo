package ru.simpleGBD.App.GUI.vaadin;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Config.LocalSystemOptions;

import javax.servlet.annotation.WebServlet;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by km on 01.05.2016.
 */
public class gbdUI extends UI {

    static ResourceBundle i18n;

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet")
    @VaadinServletConfiguration(ui = gbdUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        GBDOptions.init(new LocalSystemOptions());

        updateLocale();

        updateContent();
    }

    private void updateContent() {
        setContent(new MainView());
    }

    private void updateLocale() {
        setLocale(Locale.forLanguageTag("RU"));

        i18n = ResourceBundle.getBundle("bundles.gui", getLocale());
    }
}