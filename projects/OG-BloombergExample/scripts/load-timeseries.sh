#!/bin/sh

cd `dirname $0`/..

scripts/run-tool.sh com.opengamma.integration.tool.marketdata.TimeSeriesLoaderTool $@ -c config/toolcontext/toolcontext-bloombergexample-bin.properties -l com/opengamma/util/test/warn-logback.xml
