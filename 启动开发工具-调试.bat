@echo off
setlocal EnableExtensions
cd /d "%~dp0"
chcp 65001 >nul 2>&1

set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"
set "SCRIPT=%ROOT%\tools\tools_webview.py"

where py >nul 2>&1
if %errorlevel%==0 (set "PY=py -3") else (set "PY=python")

echo [debug] cwd=%CD%
echo [debug] script=%SCRIPT%
echo [debug] log=%LOCALAPPDATA%\fantasy_furniture_tools.log
echo.

%PY% "%SCRIPT%" --show-console
echo.
echo exit code=%ERRORLEVEL%
pause
