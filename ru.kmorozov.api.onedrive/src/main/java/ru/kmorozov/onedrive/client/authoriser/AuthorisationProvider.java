package ru.kmorozov.onedrive.client.authoriser;

import java.io.IOException;
import java.nio.file.Path;

public interface AuthorisationProvider {

    String getAccessToken() throws IOException;

    void refresh() throws IOException;

    class FACTORY {
        public static AuthorisationProvider create(final Path keyFile, String clientId, String clientSecret) throws IOException {
            return new OneDriveAuthorisationProvider(keyFile, clientId, clientSecret);
        }

        public static void printAuthInstructions(String clientId) {
            OneDriveAuthorisationProvider.printAuthInstructions(clientId);
        }
    }
}
