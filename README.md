[![Gradle](https://github.com/QilinPTA/Qilin/actions/workflows/gradle.yml/badge.svg?event=push)](https://github.com/QilinPTA/Qilin/actions/workflows/gradle.yml)
[![Docker](https://github.com/QilinPTA/Qilin/actions/workflows/docker-image.yml/badge.svg?event=push)](https://github.com/QilinPTA/Qilin/actions/workflows/docker-image.yml)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/QilinPTA/Qilin) 

> :warning: [Documentation for Qilin](https://qilinpta.github.io/) is under development.

# Qilin: A fully imperative Java Pointer Analysis Framework.
The repository hosts Qilin, a new Java Pointer Analysis Framework for supporting fine-grained context-sensitivity.

Qilin is introduced in our ECOOP'22 paper. You can cite our paper as follows:
```
@InProceedings{he2022qilin,
  author = {He, Dongjie and Lu, Jingbo and Xue, Jingling},
  title =	{Qilin: A New Framework for Supporting Fine-Grained Context-Sensitivity in Java Pointer Analysis},
  booktitle =	{36th European Conference on Object-Oriented Programming (ECOOP 2022)},
  year =	{2022},
  publisher =	{Schloss Dagstuhl -- Leibniz-Zentrum f{\"u}r Informatik},
  address =	{Dagstuhl, Germany},
}
```
## A Quick Start
### Prerequisites
* Java 16+ (Qilin uses the pattern matching for `instanceof` provided since Java 16).
* Python 3.5+ (the api `subprocess.run` used in `artifact/qilin.py` is added in Python 3.5).

### Download
This repository contains a `submodule` that contains a set of real-world ready-to-use benchmarks for Qilin. 
If you want to run Qilin on these benchmarks, please use the following command to fetch Qilin sources:
```
$ git clone --recurse-submodules https://github.com/QilinPTA/Qilin.git
```

If you have cloned Qilin in a normal way, you still can use the command below to download these benchmarks:
```
$ git submodule update --init
```
### Building Qilin with Gradle
We use Gradle as the build automation tool. To build Qilin, use
```
$ ./run.sh
```
This script contains command to generate `Qilin-VERSION-SNAPSHOT.jar` which will be automatically moved into `artifact/pta/`.

### Using Qilin
You can use Qilin either through its command-line interface (e.g., `driver.Main`) or as a library.
For researchers who are working on Java pointer analysis, we have provided a whole set of scripts, benchmarks (e.g., `DaCapo2006`) and jdk libraries under `artifact/`.

To test Qilin, you can directly do:
```
$ cd artifact
$ python3 run.py antlr ci -print
```
The above command will analyse `antlr` with a context-insensitive pointer analysis with some metrics being displayed on the screen. 
We will optimise the `run.py` script to make its help info more user-friendly.

## Documentation

| About Qilin       | Setup  Guide         | User Guide  | Developer Guide  |
| ------------- |:-------------:| -----:|-----:|
| Introducing Qilin -- [what it does](https://qilinpta.github.io/#what-is-qilin) and [how we design it](https://github.com/QilinPTA/qilinpta.github.io/wiki/Qilin-Design#qilin-design)      | A step by step [setup guide](https://github.com/QilinPTA/qilinpta.github.io/wiki/Setup-Guide#getting-started) to build Qilin | [Command-line options](https://github.com/QilinPTA/qilinpta.github.io/wiki/Full-list-of-Qilin-options) of Qilin, and running Qilin with [an example](https://github.com/QilinPTA/qilinpta.github.io/wiki/Analyse-a-Simple-Java-Program#an-example) | Detailed [technical documentation](https://qilinpta.github.io/QilinCodeStructure.html) and how to [use Qilin as a lib](https://github.com/QilinPTA/qilinpta.github.io/wiki/Qilin-as-a-lib) for your tool or [write your own analyses](https://github.com/QilinPTA/qilinpta.github.io/wiki/Write-your-own-analysis-in-Qilin) in Qilin |

## Contributing to Qilin
Contributions are always welcome. Qilin is an open source project that we published in the hope that it will be useful to the research community as a whole. 
If you have a new feature or a bug fix that you would like to see in the official code repository, please open a merge request here on Github and leave a short description of what you have done.

## License
Qilin is licenced under the GPL v2.1 license, see LICENSE file.

