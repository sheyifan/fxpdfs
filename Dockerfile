# escape=`

# Use the latest Windows Server Core 2022 image.
FROM mcr.microsoft.com/windows/servercore:ltsc2022

# Restore the default Windows shell for correct batch processing.
SHELL ["cmd", "/S", "/C"]

RUN `
    # Download the Build Tools bootstrapper.
    curl -SL --output vs_buildtools.exe https://aka.ms/vs/17/release/vs_buildtools.exe `
    `
    # Install Build Tools with the Microsoft.VisualStudio.Workload.AzureBuildTools workload, excluding workloads and components with known issues.
    && (start /w vs_buildtools.exe --quiet --wait --norestart --nocache `
        --installPath "%ProgramFiles%\Microsoft Visual Studio\2022\BuildTools" `
        --add Microsoft.VisualStudio.ComponentGroup.VC.Tools.142.x86.x64 `
        --add Microsoft.VisualStudio.Component.Windows10SDK.19041 `
        --add Microsoft.VisualStudio.Component.VC.CMake.Project `
        --add Microsoft.VisualStudio.Component.TestTools.BuildTools `
        --add Microsoft.VisualStudio.Component.VC.ASAN `
        --add Microsoft.VisualStudio.Component.NuGet `
        --add Microsoft.VisualStudio.Component.Roslyn.Compiler `
        --add Microsoft.VisualStudio.Component.Roslyn.LanguageServices `
        --add Microsoft.Net.Component.4.6.TargetingPack `
        --remove Microsoft.VisualStudio.Component.Windows10SDK.10240 `
        --remove Microsoft.VisualStudio.Component.Windows10SDK.10586 `
        --remove Microsoft.VisualStudio.Component.Windows10SDK.14393 `
        --remove Microsoft.VisualStudio.Component.Windows81SDK `
        || IF "%ERRORLEVEL%"=="3010" EXIT 0) `
    `
    # Cleanup
    && del /q vs_buildtools.exe

RUN `
    mkdir C:\Users\Administrator\Documents\workdir && `
    curl -SL --output C:\Users\Administrator\Documents\workdir\librica_nik.zip https://download.bell-sw.com/vm/23.0.1/bellsoft-liberica-vm-full-openjdk17.0.8+7-23.0.1+1-windows-amd64.zip && `
    tar -xf C:\Users\Administrator\Documents\workdir\librica_nik.zip -C C:\Users\Administrator\Documents\workdir\ && `
    curl -SL --output C:\Users\Administrator\Documents\workdir\apacha-maven-3.9.4.zip https://dlcdn.apache.org/maven/maven-3/3.9.4/binaries/apache-maven-3.9.4-bin.zip && `
    tar -xf C:\Users\Administrator\Documents\workdir\apacha-maven-3.9.4.zip -C C:\Users\Administrator\Documents\workdir\ && `
    SETX /M JAVA_HOME C:\Users\Administrator\Documents\workdir\bellsoft-liberica-vm-full-openjdk17-23.0.1 && `
    SETX /M GRAALVM_HOME C:\Users\Administrator\Documents\workdir\bellsoft-liberica-vm-full-openjdk17-23.0.1 && `
    SETX /M MAVEN_HOME C:\Users\Administrator\Documents\workdir\apache-maven-3.9.4 && `
    SETX /M PATH %MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH% `
    `
    && del /q C:\Users\Administrator\Documents\workdir\librica_nik.zip `
    && del /q C:\Users\Administrator\Documents\workdir\apacha-maven-3.9.4.zip

# Define the entry point for the docker container.
# This entry point starts the developer command prompt and launches the PowerShell shell.
ENTRYPOINT ["C:\\Program Files\\Microsoft Visual Studio\\2022\\BuildTools\\VC\\Auxiliary\\Build\\vcvars64.bat", "&&", "powershell.exe", "-NoLogo", "-ExecutionPolicy", "Bypass"]