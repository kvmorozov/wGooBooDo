package ru.kmorozov.onedrive.client.authoriser;

import java.io.IOException;
import java.nio.file.Path;

public interface AuthorisationProvider {

    String getAccessToken() throws IOException;

    void refresh() throws IOException;

    class FACTORY {
        public static AuthorisationProvider create(Path keyFile, final String clientId, final String clientSecret) throws IOException {
            return new OneDriveAuthorisationProvider(keyFile, clientId, clientSecret);
        }

        public static void printAuthInstructions(final String clientId) {
            OneDriveAuthorisationProvider.printAuthInstructions(clientId);
        }
    }
}
