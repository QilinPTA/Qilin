# QiLin: A fully imperative Java Pointer Analysis Framework.
The repository hosts QiLin, A new Java Pointer Analysis Framework for supporting fine-grained context-sensitivity.

## Building QiLin with Gradle
We use Gradle as the build automation tool. To build Qilin, use
```
$ ./run.sh
```
This script contains command to generate `Qilin-VERSION-SNAPSHOT.jar` which will be automatically moved into `artifact/pta/`.

## Using Qilin
You can use Qilin either through its command-line interface (e.g., `driver.Main`) or as a library.
For researchers who are working on Java pointer analysis, we have provided a whole set of scripts, benchmarks (e.g., `DaCapo2006`) and jdk libraries in `artifact/`.

To test QiLin, you can directly use
```
$ cd artifact
$ python3 run.py antlr ci -print
```
The above command will analyze `antlr` with a context-insensitive pointer analysis and some metrics will be displayed on the screen. 
We will optimize the `run.py` script to make its help info more user-friendly.

## Contributing to QiLin
Contributions are always welcome. QiLin is an open source project that we published in the hope that it will be useful to the research community as a whole. 
If you have a new feature or a bug fix that you would like to see in the official code repository, please open a merge request here on Github and contact 
us (see below) with a short description of what you have done.

## License
QiLin is licenced under the GPL v2.1 license, see LICENSE file.

