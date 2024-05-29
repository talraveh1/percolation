@echo off
cd "%~dp0\.."
java -cp out/artifacts/Percolation_jar/Percolation.jar percolation.util.LogRenamer
\cygwin64\bin\touch log/run.log
\cygwin64\bin\tail -f log/run.log
