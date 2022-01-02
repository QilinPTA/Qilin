#!/usr/bin/python3

import os, sys, subprocess
from util.opt import *

# HOME
PA_HOME = os.path.dirname(os.path.realpath(__file__))
LIB = os.path.join(PA_HOME, 'lib')
# heapsize
XMX = '-Xmx512g'
timeout = -1

CLASSPATH = os.pathsep.join([
    os.path.join(PA_HOME, 'config-files'),
    os.path.join(PA_HOME, 'Qilin-1.0-SNAPSHOT.jar'),
])

runJava_cmd = 'java -Xms1g %s -cp ' + CLASSPATH + ' driver.Main %s'
OPTIONMESSAGE = 'The valid OPTIONs are:\n' \
                + option('-help|-h', 'print this message.') \
                + option('-jre=<[jre1.6.0_24|jre1.6.0_45|jre1.8.0_121_debug]>', 'specify the version of JDK.') \
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
    args.append('-jre=' + os.path.join(PA_HOME, 'lib', 'jre', JREVERSION))

    runCommmand = runJava_cmd % (XMX, ' '.join(args))
    print(runCommmand)
    if outputFile is None:
        subprocess.run(runCommmand.split(' '), timeout=timeout)
    else:
        with open(outputFile, "w") as outfile:
            subprocess.run(runCommmand.split(' '), stdout=outfile, stderr=subprocess.STDOUT, timeout=timeout)
