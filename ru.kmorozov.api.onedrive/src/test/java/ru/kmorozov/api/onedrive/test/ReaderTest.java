package ru.kmorozov.api.onedrive.test;

import com.kmorozov.onedrive.client.OneDriveItem;
import com.kmorozov.onedrive.client.OneDriveProvider;
import com.kmorozov.onedrive.client.OneDriveProvider.FACTORY;
import com.kmorozov.onedrive.client.authoriser.AuthorisationProvider;
import com.kmorozov.onedrive.client.walker.OneDriveWalkers;
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
        final File file = new File(getClass().getClassLoader().getResource("onedrive.key").getFile());

        final AuthorisationProvider authoriser = AuthorisationProvider.FACTORY.create(file.toPath());
        api = FACTORY.readOnlyApi(authoriser);
    }

    @Test
    public void rootTest() throws IOException {
        final OneDriveItem root = api.getRoot();

        MatcherAssert.assertThat(root, CoreMatchers.is(CoreMatchers.notNullValue()));

        final OneDriveItem[] children = api.getChildren(root);
        MatcherAssert.assertThat(children, CoreMatchers.is(CoreMatchers.notNullValue()));
    }

    @Test
    public void walkTest() throws IOException {
        OneDriveWalkers.walk(api, 3).forEach(oneDriveItem -> System.out.println(oneDriveItem.getName()));
    }
}
