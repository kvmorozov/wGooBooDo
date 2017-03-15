package ru.kmorozov.api.onedrive.test;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.authoriser.AuthorisationProvider;
import com.wouterbreukink.onedrive.client.walker.OneDriveWalkers;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class ReaderTest {

    private OneDriveProvider api;

    @Before
    public void initApi() throws IOException {
        File file = new File(getClass().getClassLoader().getResource("onedrive.key").getFile());

        AuthorisationProvider authoriser = AuthorisationProvider.FACTORY.create(file.toPath());
        api = OneDriveProvider.FACTORY.readOnlyApi(authoriser);
    }

    @Test
    public void rootTest() throws IOException {
        OneDriveItem root = api.getRoot();

        assertThat(root, is(notNullValue()));

        OneDriveItem[] children = api.getChildren(root);
        assertThat(children, is(notNullValue()));
    }

    @Test
    public void walkTest() throws IOException {
        OneDriveWalkers.walk(api, 3).forEach(oneDriveItem -> {
            System.out.println(oneDriveItem.getName());
        });
    }
}
