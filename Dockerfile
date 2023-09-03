# compile artifact
FROM amd64/gradle AS buildEnv
RUN apt-get install python3 -y
ADD . /build/
WORKDIR /build/
RUN ./gradlew clean fatJar

# build image
FROM hdjay2013/jupx:v16
ENV workdir /qilin
WORKDIR $workdir
ENV user root
USER $user
COPY artifact/benchmarks $workdir/benchmarks
COPY artifact/util/ $workdir/util/
COPY artifact/run.py $workdir/
COPY artifact/qilin.py $workdir/
COPY artifact/__init__.py $workdir/
COPY --from=buildEnv /build/artifact/Qilin-0.9.4-SNAPSHOT.jar $workdir/
CMD /bin/bash
