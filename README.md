[![Gradle](https://github.com/QiLinPTA/QiLin/actions/workflows/gradle.yml/badge.svg?event=push)](https://github.com/QiLinPTA/QiLin/actions/workflows/gradle.yml)
[![Docker](https://github.com/QiLinPTA/QiLin/actions/workflows/docker-image.yml/badge.svg?event=push)](https://github.com/QiLinPTA/QiLin/actions/workflows/docker-image.yml)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/QiLinPTA/QiLin) 

> :warning: [Documentation for QiLin](https://qilinpta.github.io/) is under development.

# Qilin: A fully imperative Java Pointer Analysis Framework.
The repository hosts Qilin, A new Java Pointer Analysis Framework for supporting fine-grained context-sensitivity.

Qilin is introduced in our ECOOP'22 paper. You might want to cite our paper by copying the following BibTeX text:
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
### Download
This repository contains a `submodule` that contains a set of real-world ready-to-use benchmarks for Qilin. 
If you want to run Qilin on these benchmarks, so please use the following command to fetch Qilin sources:
```
$ git clone --recurse-submodules https://github.com/QiLinPTA/Qilin.git
```

If you have cloned Qilin in a normal way, you can use this command to download these benchmarks:
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
For researchers who are working on Java pointer analysis, we have provided a whole set of scripts, benchmarks (e.g., `DaCapo2006`) and jdk libraries in `artifact/`.

To test Qilin, you can directly use
```
$ cd artifact
$ python3 run.py antlr ci -print
```
The above command will analyze `antlr` with a context-insensitive pointer analysis and some metrics will be displayed on the screen. 
We will optimize the `run.py` script to make its help info more user-friendly.

## Documentation

| About Qilin                                                                                                                                                                                                        |                                                         Setup  Guide                                                         |                                                                                                                                                                                                                                                                                                                                 User Guide |                                                                                                                                                                                                                                                                                                                                               Developer Guide |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------:|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Introducing Qilin -- [what it does](https://github.com/QiLinPTA/qilinpta.github.io/wiki/About#what-is-qilin) and [how we design it](https://github.com/QiLinPTA/qilinpta.github.io/wiki/Qilin-Design#qilin-design) | A step by step [setup guide](https://github.com/QiLinPTA/qilinpta.github.io/wiki/Setup-Guide#getting-started) to build Qilin | Command-line options to [run Qilin](https://github.com/svf-tools/SVF/wiki/User-Guide#quick-start), get [analysis outputs](https://github.com/QiLinPTA/qilinpta.github.io/wiki/User-Guide#analysis-outputs), and test Qilin with [an example](https://github.com/QiLinPTA/qilinpta.github.io/wiki/Analyze-a-Simple-Java-Program#an-example) | Detailed [technical documentation](https://github.com/QiLinPTA/qilinpta.github.io/wiki/Technical-documentation) and how to [write your own analyses](https://github.com/QiLinPTA/qilinpta.github.io/wiki/Write-your-own-analysis-in-Qilin) in Qilin or [use Qilin as a lib](https://github.com/QiLinPTA/qilinpta.github.io/wiki/Qilin-as-a-lib) for your tool |


## Contributing to Qilin
Contributions are always welcome. Qilin is an open source project that we published in the hope that it will be useful to the research community as a whole. 
If you have a new feature or a bug fix that you would like to see in the official code repository, please open a merge request here on Github and leave a short description of what you have done.

## License
Qilin is licenced under the GPL v2.1 license, see LICENSE file.

