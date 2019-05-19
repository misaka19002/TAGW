#!/bin/sh
dir=$(dirname "$0")
nohup java -cp "$dir/h2-1.4.199.jar:$H2DRIVERS:$CLASSPATH" \
org.h2.tools.Server \
-webAllowOthers \
-tcpAllowOthers  \
-pgAllowOthers \
-webAdminPassword wangyudi \
-ifNotExists \
"$@" &
