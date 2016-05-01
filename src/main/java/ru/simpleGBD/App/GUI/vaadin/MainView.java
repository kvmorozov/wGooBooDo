package ru.simpleGBD.App.GUI.vaadin;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;

import static ru.simpleGBD.App.GUI.vaadin.gbdUI.i18n;

/**
 * Created by km on 01.05.2016.
 */
public class MainView extends HorizontalLayout {

    MainView() {
        TabSheet tabs = new TabSheet();
        tabs.addTab(new LoadingTab(), i18n.getString("load"));
        tabs.addTab(new SettingsTab(), i18n.getString("properties"));

        tabs.setSelectedTab(0);

        addComponent(tabs);

        setWidth("100%");
        setHeight("100%");
    }
}
