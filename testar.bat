@echo off
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo Java JDK 17 ou superior nao encontrado. Verifique a instalacao e o PATH.
  pause
  exit /b 1
)
if not exist out mkdir out
javac -encoding UTF-8 -d out src\loja\*.java
if errorlevel 1 (
  echo Falha na compilacao dos testes.
  pause
  exit /b 1
)
java -cp out loja.LojaTest
pause
