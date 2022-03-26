The official soot (version 4.2.1) contains bugs. Thus, Qilin is built on top of a modified version of Soot.
The following steps introduce how to build the version of Soot that we use.

# How to build the Soot?
## Step 1: Download [Soot](https://github.com/soot-oss/soot/archive/refs/tags/4.2.1.tar.gz)
## Step 2: Modify the souce of Soot according to `diff.txt`
## Step 3: Build Soot
```commandline
mvn clean compile assembly:single
```
Move the resulting jar `target/sootclasses-trunk-jar-with-dependencies.jar` to this directory and rename it accordingly. 
