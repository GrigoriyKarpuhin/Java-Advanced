#!/bin/bash

cd ../
javac -cp ../java-advanced-2024/modules/info.kgeorgiy.java.advanced.implementor/ -d scripts java-solutions/info/kgeorgiy/ja/karpukhin/implementor/*.java
cd scripts
jar cfm Implementor.jar manifest.mf info/kgeorgiy/ja/karpukhin/implementor/*.class info/kgeorgiy/java/advanced/implementor/*.class
rm -r info/
