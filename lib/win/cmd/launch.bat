for /f %%i in ('dir /b ^| findstr .jar') do set LAUNCHER_JAR=%%i
tasklist | findstr "fxpdfs_java.exe" || .\runtime\bin\fxpdfs_java.exe -javaagent:%LAUNCHER_JAR% -jar %LAUNCHER_JAR%