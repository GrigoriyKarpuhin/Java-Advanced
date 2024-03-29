#!/bin/bash

cd ../../
javac -cp java-advanced-2024/modules/info.kgeorgiy.java.advanced.implementor/ -d java-advanced/java-solutions/info/kgeorgiy/ja/karpukhin/implementor/ java-advanced/java-solutions/info/kgeorgiy/ja/karpukhin/implementor/*.java
cd java-advanced/java-solutions/info/kgeorgiy/ja/karpukhin/implementor/
jar cfm implementor.jar manifest.mf info/kgeorgiy/ja/karpukhin/implementor/*.class info/kgeorgiy/java/advanced/implementor/*.class
rm -r info/
