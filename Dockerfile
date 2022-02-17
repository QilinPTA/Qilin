# compile artifact
FROM amd64/gradle AS buildEnv
RUN apt-get install python3 -y
ADD . /build/
WORKDIR /build/
RUN ./gradlew clean fatJar

# build image
FROM hdjay2013/jupx:latest
ENV workdir /qilin
WORKDIR $workdir
ENV user root
USER $user
COPY artifact/benchmarks $workdir/benchmarks
COPY artifact/pta/ $workdir/pta
COPY artifact/util/ $workdir/util/
COPY artifact/run.py $workdir/
COPY artifact/__init__.py $workdir/
COPY --from=buildEnv /build/artifact/pta/Qilin-1.0-SNAPSHOT.jar $workdir/pta/
CMD /bin/bash
