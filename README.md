![Docker](https://github.com/QiLinPTA/QiLin/actions/workflows/docker-image.yml/badge.svg?event=push)
# QiLin: A fully imperative Java Pointer Analysis Framework.
The repository hosts QiLin, A new Java Pointer Analysis Framework for supporting fine-grained context-sensitivity.

QiLin is introduced in our ECOOP'22 paper. You might want to cite our paper by copying the following BibTeX text:
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
### Building QiLin with Gradle
We use Gradle as the build automation tool. To build Qilin, use
```
$ ./run.sh
```
This script contains command to generate `Qilin-VERSION-SNAPSHOT.jar` which will be automatically moved into `artifact/pta/`.

### Using Qilin
You can use Qilin either through its command-line interface (e.g., `driver.Main`) or as a library.
For researchers who are working on Java pointer analysis, we have provided a whole set of scripts, benchmarks (e.g., `DaCapo2006`) and jdk libraries in `artifact/`.

To test QiLin, you can directly use
```
$ cd artifact
$ python3 run.py antlr ci -print
```
The above command will analyze `antlr` with a context-insensitive pointer analysis and some metrics will be displayed on the screen. 
We will optimize the `run.py` script to make its help info more user-friendly.

## Documentation

| About QiLin       | Setup  Guide         | User Guide  | Developer Guide  |
| ------------- |:-------------:| -----:|-----:|
| Introducing QiLin -- [what it does](https://github.com/QiLinPTA/QiLin/wiki/About#what-is-qilin) and [how we design it](https://github.com/QiLinPTA/QiLin/wiki/QiLin-Design#qilin-design)      | A step by step [setup guide](https://github.com/QiLinPTA/QiLin/wiki/Setup-Guide#getting-started) to build QiLin | Command-line options to [run QiLin](https://github.com/svf-tools/SVF/wiki/User-Guide#quick-start), get [analysis outputs](https://github.com/QiLinPTA/QiLin/wiki/User-Guide#analysis-outputs), and test QiLin with [an example](https://github.com/QiLinPTA/QiLin/wiki/Analyze-a-Simple-Java-Program) | Detailed [technical documentation](https://github.com/QiLinPTA/QiLin/wiki/Technical-documentation) and how to [write your own analyses](https://github.com/QiLinPTA/QiLin/wiki/Write-your-own-analysis-in-QiLin) in QiLin or [use QiLin as a lib](https://github.com/QiLinPTA/QiLin/wiki/QiLin-as-a-lib) for your tool  |


## Contributing to QiLin
Contributions are always welcome. QiLin is an open source project that we published in the hope that it will be useful to the research community as a whole. 
If you have a new feature or a bug fix that you would like to see in the official code repository, please open a merge request here on Github and leave a short description of what you have done.

## License
QiLin is licenced under the GPL v2.1 license, see LICENSE file.

