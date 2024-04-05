#!/bin/bash

SOURCE_PATH="../java-solutions info.kgeorgiy.ja.karpukhin.implementor"

OUTPUT_PATH="../javadoc"

CLASSPATH="../../java-advanced-2024/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/"

javadoc -d $OUTPUT_PATH -sourcepath $SOURCE_PATH "${CLASSPATH}Impler.java" "${CLASSPATH}JarImpler.java" "${CLASSPATH}ImplerException.java"
