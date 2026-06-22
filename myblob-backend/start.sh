#!/bin/bash
# Myblob 后端启动脚本 (2核4G 服务器优化版)

# JVM 参数
JAVA_OPTS="-Xms1g -Xmx2g"
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"
JAVA_OPTS="$JAVA_OPTS -XX:G1HeapRegionSize=4m"
JAVA_OPTS="$JAVA_OPTS -XX:ParallelGCThreads=2"
JAVA_OPTS="$JAVA_OPTS -XX:ConcGCThreads=1"
JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication"
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=./logs/heapdump.hprof"

# Spring 配置
SPRING_OPTS="--spring.profiles.active=prod"

# 启动应用
echo "Starting Myblob Backend with JVM opts: $JAVA_OPTS"
java $JAVA_OPTS -jar myblob-backend.jar $SPRING_OPTS
