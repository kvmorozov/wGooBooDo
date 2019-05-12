set PROJECT_ROOT=J:\Work\wGooBooDo
set REPO=%PROJECT_ROOT%\repo
set VERSION=2.2-b2

rem ru.kmorozov.api.onedrive ru.kmorozov.gbd.core ru.kmorozov.gbd.core.config ru.kmorozov.gbd.core.logger ru.kmorozov.gbd.db ru.kmorozov.gbd.db.mongo ru.kmorozov.library.data.model ru.kmorozov.library.data.server

for %%i in () do (
    cd %PROJECT_ROOT%\%%i
    mvn compile
    mvn package -Dmaven.test.skip=true
    mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=%PROJECT_ROOT%\%%i\target\%%i-%VERSION%.jar -DlocalRepositoryPath=%REPO%
)

for %%i in (ru.kmorozov.gbd.parent ru.kmorozov.library.parent) do (
    cd %PROJECT_ROOT%\%%i
rem    mvn compile
rem    mvn package -Dmaven.test.skip=true
    mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=%PROJECT_ROOT%\%%i\pom.xml -DlocalRepositoryPath=%REPO%
)