package ru.simpleGBD.App.GUI.vaadin;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import ru.simpleGBD.App.Config.SystemConfigs;
import ru.simpleGBD.App.Logic.extractors.ImageExtractor;

import javax.swing.*;

import static ru.simpleGBD.App.GUI.vaadin.gbdUI.i18n;

/**
 * Created by km on 01.05.2016.
 */
public class LoadingPane extends HorizontalLayout {

    private SwingWorker workerExtractor;

    LoadingPane() {
        addComponent(new Label(i18n.getString("bookId")));

        TextField tfBookId = new TextField();
        tfBookId.setValue(SystemConfigs.getLastBookId());
        addComponent(tfBookId);

        Button bLoad = new Button(i18n.getString("doLoad"));
        bLoad.addClickListener(event -> {
            SystemConfigs.setLastBookId(tfBookId.getValue());
            bLoad.setEnabled(false);

            workerExtractor = new ImageExtractor();
            workerExtractor.execute();
        });
        addComponent(bLoad);

        setWidth("100%");
        setHeight("100%");
    }
}
