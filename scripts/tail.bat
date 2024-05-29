@echo off
cd "%~dp0\.."
java -Xmx20G -jar out\artifacts\Percolation_jar\Percolation.jar %1
