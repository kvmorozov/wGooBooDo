package ru.kmorozov.api.onedrive.test;

import com.wouterbreukink.onedrive.client.OneDriveItem;
import com.wouterbreukink.onedrive.client.OneDriveProvider;
import com.wouterbreukink.onedrive.client.authoriser.AuthorisationProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class ReaderTest {

    @Test
    public void rootTest() throws IOException {
        File file = new File(getClass().getClassLoader().getResource("onedrive.key").getFile());

        AuthorisationProvider authoriser = AuthorisationProvider.FACTORY.create(file.toPath());
        OneDriveProvider api = OneDriveProvider.FACTORY.readOnlyApi(authoriser);
        OneDriveItem root = api.getRoot();

        assertThat(root, is(notNullValue()));
    }
}
