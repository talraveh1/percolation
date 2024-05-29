@echo off
cd "%~dp0\.."
java -cp hsqldb/lib/hsqldb.jar org.hsqldb.server.Server -silent false -database.0 "file:db.%1/data"
