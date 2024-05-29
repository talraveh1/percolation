@echo off
cd "%~dp0\.."
java -cp out/artifacts/Percolation_jar/Percolation.jar percolation.db.Shutdown %1
