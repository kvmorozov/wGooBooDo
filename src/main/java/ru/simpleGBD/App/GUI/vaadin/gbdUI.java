package ru.simpleGBD.App.GUI.vaadin;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import javax.servlet.annotation.WebServlet;

/**
 * Created by km on 01.05.2016.
 */
public class gbdUI extends UI {

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet")
    @VaadinServletConfiguration(ui = gbdUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setContent(new Label("Hello World!"));
    }
}
