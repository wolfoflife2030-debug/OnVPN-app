@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0upload-to-github.ps1"
pause
