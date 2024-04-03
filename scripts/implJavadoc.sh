#!/bin/bash

SOURCE_PATH="../java-solutions info.kgeorgiy.ja.karpukhin.implementor"

OUTPUT_PATH="../javadoc"

CLASSPATH="../../java-advanced-2024/artifacts/info.kgeorgiy.java.advanced.implementor.jar"

javadoc -d $OUTPUT_PATH -sourcepath $SOURCE_PATH -cp $CLASSPATH
