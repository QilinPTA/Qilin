#!/bin/bash
./gradlew clean fatjar
if [ "${1}" = "throw" ]
then
	mv artifact/pta/Qilin-1.0-SNAPSHOT.jar ../doopvsqilin/artifact/pta/
	echo "hello, done!"
fi
