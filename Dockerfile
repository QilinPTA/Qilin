# compile artifact
FROM amd64/gradle AS buildEnv
RUN apt-get install python3 -y
ADD . /build/
WORKDIR /build/
RUN ./gradlew proguard

# build image
FROM hdjay2013/jupx:latest
ENV workdir /turner
WORKDIR $workdir
ENV user root
USER $user
COPY artifact/benchmarks $workdir/benchmarks
COPY artifact/pta/ $workdir/pta
COPY artifact/util/ $workdir/util/
COPY artifact/sample/ $workdir/sample/
COPY artifact/main.py $workdir/
COPY artifact/run.py $workdir/
COPY artifact/__init__.py $workdir/
COPY artifact/doc/Getting-Started-Guide.pdf $workdir/
COPY artifact/doc/Step-by-Step-Instructions.pdf $workdir/
COPY --from=buildEnv /build/artifact/pta/turner-artifact-1.0-SNAPSHOT.jar $workdir/pta/
CMD /bin/bash
