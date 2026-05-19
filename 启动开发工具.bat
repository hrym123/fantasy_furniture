@echo off
setlocal EnableExtensions
cd /d "%~dp0"

rem One-click launcher: fantasy_furniture tools (FastAPI + pywebview)
rem GUI uses pythonw (no console). Errors: %LOCALAPPDATA%\fantasy_furniture_tools.log

chcp 65001 >nul 2>&1

set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"
set "SCRIPT=%ROOT%\tools\tools_webview.py"
set "FF_TOOLS_HIDE_CONSOLE=1"

where py >nul 2>&1
if %errorlevel%==0 (
  set "PY_LAUNCHER=py -3"
) else (
  set "PY_LAUNCHER=python"
)

%PY_LAUNCHER% --version >nul 2>&1
if errorlevel 1 (
  echo ERROR: Python not found. Install Python 3 and add to PATH.
  pause
  exit /b 1
)

%PY_LAUNCHER% -c "import fastapi, uvicorn, webview" >nul 2>&1
if errorlevel 1 (
  echo [fantasy_furniture] Installing web dependencies...
  %PY_LAUNCHER% -m pip install -r "%ROOT%\tools\requirements-web.txt"
  if errorlevel 1 (
    echo ERROR: pip install failed.
    pause
    exit /b 1
  )
)

set "PYEXE="
for /f "delims=" %%I in ('%PY_LAUNCHER% -c "import sys; print(sys.executable)" 2^>nul') do set "PYEXE=%%I"
if not defined PYEXE (
  echo ERROR: Cannot resolve Python executable.
  pause
  exit /b 1
)

set "PYW=%PYEXE:python.exe=pythonw.exe%"
if /i not "%PYEXE:~-10%"=="python.exe" set "PYW=%PYEXE:python3.exe=pythonw.exe%"
if not exist "%PYW%" set "PYW=%PYEXE%"

rem /D = repo root; quoted paths for spaces
start "" /D "%ROOT%" "%PYW%" "%SCRIPT%"
if errorlevel 1 (
  echo ERROR: Failed to start tools UI.
  echo Try: "%PYEXE%" "%SCRIPT%" --show-console
  echo Log: %LOCALAPPDATA%\fantasy_furniture_tools.log
  pause
  exit /b 1
)

endlocal
exit /b 0
