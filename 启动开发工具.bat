@echo off
setlocal EnableExtensions
cd /d "%~dp0"

rem One-click launcher: fantasy_furniture tools (FastAPI + pywebview)
rem Double-click this file, or pin to taskbar / desktop.

chcp 65001 >nul 2>&1

where py >nul 2>&1
if %errorlevel%==0 (
  set "PY=py -3"
) else (
  set "PY=python"
)

echo [fantasy_furniture] Checking Python...
%PY% --version >nul 2>&1
if errorlevel 1 (
  echo ERROR: Python not found. Install Python 3 and add to PATH.
  pause
  exit /b 1
)

%PY% -c "import fastapi, uvicorn, webview" >nul 2>&1
if errorlevel 1 (
  echo [fantasy_furniture] Installing web dependencies...
  %PY% -m pip install -r tools\requirements-web.txt
  if errorlevel 1 (
    echo ERROR: pip install failed.
    pause
    exit /b 1
  )
)

echo [fantasy_furniture] Starting tools UI...
%PY% tools\tools_webview.py
if errorlevel 1 (
  echo Startup failed.
  pause
  exit /b 1
)
endlocal
exit /b 0
