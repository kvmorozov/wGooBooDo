set PROJECT_ROOT=J:\Work\wGooBooDo
set REPO=%PROJECT_ROOT%\repo
set VERSION=2.2-b2

rem ru.kmorozov.api.onedrive ru.kmorozov.gbd.core ru.kmorozov.gbd.core.config ru.kmorozov.gbd.core.logger
rem ru.kmorozov.gbd.db ru.kmorozov.gbd.db.mongo ru.kmorozov.library.data.model

for %%i in (ru.kmorozov.api.onedrive ru.kmorozov.gbd.core ru.kmorozov.gbd.core.config ru.kmorozov.gbd.core.logger ru.kmorozov.gbd.db ru.kmorozov.gbd.db.mongo ru.kmorozov.library.data.model ru.kmorozov.library.data.server) do (
    cd %PROJECT_ROOT%\%%i
    mvn compile
    mvn package -Dmaven.test.skip=true
    mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=%PROJECT_ROOT%\%%i\target\%%i-%VERSION%.jar -DlocalRepositoryPath=%REPO%
)