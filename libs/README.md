# How to build the Soot?
## Step 1: Download [Soot](https://github.com/soot-oss/soot/archive/refs/tags/4.4.0.zip)
## Step 2: Build Soot
```commandline
mvn clean compile assembly:single
```
Move the resulting jar `target/sootclasses-trunk-jar-with-dependencies.jar` to this directory and rename it accordingly. 
