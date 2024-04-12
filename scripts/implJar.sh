#!/bin/bash

javac -cp ../../java-advanced-2024/artifacts/info.kgeorgiy.java.advanced.implementor.jar -d ./tmp ../java-solutions/info/kgeorgiy/ja/karpukhin/implementor/*.java
jar cfm Implementor.jar manifest.mf -C ./tmp .
rm -r ./tmp
