#!/usr/bin/python3

import fnmatch
import os
import subprocess
import sys

from util.opt import *

# HOME
PA_HOME = os.path.dirname(os.path.realpath(__file__))
# heapsize
XMX = '-Xmx512g'
timeout = -1


def qilinJarFile():
    qilinJars = []
    for f in os.listdir(PA_HOME):
        if fnmatch.fnmatch(f, 'Qilin-*-SNAPSHOT.jar'):
            qilinJars.append(f)
    if len(qilinJars) == 1:
        return qilinJars[0]
    elif len(qilinJars) > 1:
        print("There are {0:d} choices for Qilin executables:".format(len(qilinJars)))
        for count, value in enumerate(qilinJars):
            print("{0:d} \t {1}".format(count, value))
        val = input("please type the selection number:")
        idx = int(val)
        return qilinJars[idx]

    else:
        sys.exit('could not find Qilin executable file.')


CLASSPATH = os.pathsep.join([os.path.join(PA_HOME, qilinJarFile()), ])

runJava_cmd = 'java -Xms1g %s -cp ' + CLASSPATH + ' driver.Main %s'
OPTIONMESSAGE = 'The valid OPTIONs are:\n' \
                + option('-help|-h', 'print this message.') \
                + option('-jre=<JRE>', 'specify the version of JDK.') \
                + bioption('-Xmx', '\b<MAX>', '  Specify the maximum size, in bytes, of the memory allocation pool.') \
                + bioption('-timeout', 'seconds', 'Timeout for PTA (default value: -1 (unlimited)).') \
                + option('-ptahelp', 'print help info for pointer analysis.')


def runPointsToAnalysis(args):
    global XMX, timeout
    outputFile = None
    if '-help' in args or '-h' in args:
        sys.exit(OPTIONMESSAGE)
    if '-ptahelp' in args or '-ptah' in args:
        os.system(runJava_cmd % ('', '-help'))
        sys.exit()
    if (len(args) < 2):
        sys.exit('Not enough options! ' + OPTIONMESSAGE)

    JREVERSION = 'jre1.6.0_45'
    i = 0
    while (i < len(args)):
        if args[i].startswith('-timeout'):
            if args[i].startswith('-timeout='):
                timeout = args[i][9:]
                args.remove(args[i])
                i -= 1
            else:
                timeout = args[i + 1]
                args.remove('-timeout')
                args.remove(timeout)
                i -= 2
            timeout = int(timeout)
        elif args[i].startswith('-Xmx'):
            XMX = args[i]
            args.remove(XMX)
            i -= 1
        elif args[i] == '>':
            outputFile = args[i + 1]
            args.remove('>')
            args.remove(outputFile)
            i -= 2
        elif args[i].startswith('-jre='):
            JREVERSION = args[i][5:]
            args.remove(args[i])
            i -= 1
        i += 1

    # prepare javacmd args
    args.append('-jre=' + os.path.join(PA_HOME, 'benchmarks', 'JREs', JREVERSION))

    runCommmand = runJava_cmd % (XMX, ' '.join(args))
    print(runCommmand)
    if outputFile is None:
        subprocess.run(runCommmand.split(' '), timeout=timeout)
    else:
        with open(outputFile, "w") as outfile:
            subprocess.run(runCommmand.split(' '), stdout=outfile, stderr=subprocess.STDOUT, timeout=timeout)
