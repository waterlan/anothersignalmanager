setlocal
set JAVA_HOME=%~dp0\jdk-11.0.16+8-jre
set JAVAFX=%~dp0\javafx-sdk-11.0.2
set PATH=%JAVA_HOME%\bin;%PATH%
java --module-path %JAVAFX%\lib --add-modules="javafx.controls" -jar %~dp0\lib\asm.jar
endlocal
