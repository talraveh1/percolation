@echo off
cd "%~dp0\.."

:: Check if two arguments are provided
IF "%~1"=="" GOTO args
IF NOT "%~2"=="" GOTO args

:: Continue with your script if exactly two arguments are provided
wt -w _quake^
        sp --title Log -d "%cd%" cmd /C scripts\log.bat;^
        sp --title Server -H -d "%cd%" cmd /C scripts\server.bat %1;^
        mf --direction first
goto end

:args
echo Please provide the database number
goto end

:end


