@echo off
REM Myblob 后端启动脚本 (2核4G 服务器优化版)

set JAVA_OPTS=-Xms1g -Xmx2g
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxGCPauseMillis=200
set JAVA_OPTS=%JAVA_OPTS% -XX:G1HeapRegionSize=4m
set JAVA_OPTS=%JAVA_OPTS% -XX:ParallelGCThreads=2
set JAVA_OPTS=%JAVA_OPTS% -XX:ConcGCThreads=1
set JAVA_OPTS=%JAVA_OPTS% -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JAVA_OPTS=%JAVA_OPTS% -XX:HeapDumpPath=./logs/heapdump.hprof

set SPRING_OPTS=--spring.profiles.active=prod

echo Starting Myblob Backend with JVM opts: %JAVA_OPTS%
java %JAVA_OPTS% -jar myblob-backend.jar %SPRING_OPTS%
