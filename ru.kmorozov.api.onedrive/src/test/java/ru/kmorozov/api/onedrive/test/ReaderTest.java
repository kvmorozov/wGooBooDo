package ru.kmorozov.api.onedrive.test;

import ru.kmorozov.onedrive.client.OneDriveItem;
import ru.kmorozov.onedrive.client.OneDriveProvider;
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider;
import ru.kmorozov.onedrive.client.walker.OneDriveWalkers;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class ReaderTest {

    private OneDriveProvider api;

    @Before
    public void initApi() throws IOException {
        File file = new File(this.getClass().getClassLoader().getResource("onedrive.key").getFile());

        AuthorisationProvider authoriser = AuthorisationProvider.FACTORY.create(file.toPath(), null, null);
        this.api = OneDriveProvider.FACTORY.readOnlyApi(authoriser);
    }

    @Test
    public void rootTest() throws IOException {
        OneDriveItem root = this.api.getRoot();

        MatcherAssert.assertThat(root, CoreMatchers.is(CoreMatchers.notNullValue()));

        OneDriveItem[] children = this.api.getChildren(root);
        MatcherAssert.assertThat(children, CoreMatchers.is(CoreMatchers.notNullValue()));
    }

    @Test
    public void walkTest() throws IOException {
        OneDriveWalkers.walk(this.api, 3).forEach(oneDriveItem -> System.out.println(oneDriveItem.getName()));
    }
}
