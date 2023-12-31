#!/usr/bin/python3

import os, sys, shutil

assert sys.version_info >= (3, 5), "Python version does not meet the minimum requirements, i.e., >= 3.5"

import qilin as pta
from util.opt import *
from util import Util
import util.TerminalColor as tc
from util.benchmark import BENCHMARKS
from util.benchmark import APPPATH
from util.benchmark import LIBPATH
from util.benchmark import TAMIFLEXLOG
from util.benchmark import MAINCLASSES
from util.benchmark import JREVERSION

# ANALYSES = ['1o', 'Z-2o', 'E-2o', 'T-2o', '2o', 'Z-3o', 'T-3o', 'E-3o', '3o', '1c', '2c', 'M-2o', '2h', '2t', 'Z-2c', 'M-2c']
ANALYSES = ['insens', '1o', 'Z-2o', 'E-2o', 'T-2o', '2o', '2t', '1c', 'M-2o', '2h', 'B-2o', '1c', 's-1c', 's-2c', '2c', '3o', 'T-3o', 'E-3o', 'Z-3o']

# for ZIPPEROPTIONS
OoM = {
    'E-3o': ['eclipse', ],
    'Z-3o': ['eclipse', ],
    '3o': ['chart', 'eclipse', 'checkstyle', 'findbugs', 'xalan'],
    '3o+D':['eclipse',],
    '3o+DX':['eclipse'],
    '3o+DC':['eclipse']
}
TIMEOUT = {
    '3o': ['bloat'],
    '3o+D': ['checkstyle'],
    '3o+DC': ['checkstyle', 'checkstyle'],
    'E-3o': ['checkstyle', 'findbugs'],
    'Z-3o': ['checkstyle'],
}

BASICOPTIONS = ['-Xmx512g', '-timeout=43200', ]
# This setting is same as that used in Zipper's artifact.
ZIPPEROPTIONS = ['-pae', '-pe', '-clinit=ONFLY', '-lcs', '-mh']
MAHJONGOPTIONS = ['-pae', '-pe', '-clinit=ONFLY']
# This setting is same as Zipper's except that we wont set ci and merge objects of type StringBuilder/StringBuffer/Throwable.
# We use this options as the EAGLE option.
EAGLEOPTIONS = ['-pae', '-clinit=ONFLY']
# This setting is the real option that are used in Eagle's artifact.
# EAGLEOPTIONS = ['-singleentry', '-clinit=FULL']
OUTPUTPATH = 'output'


def getPTACommand(analysis, bm, OPTIONSTYLE):
    if OPTIONSTYLE == 'zipper':
        options = BASICOPTIONS + ZIPPEROPTIONS
    elif OPTIONSTYLE == 'mahjong':
        options = BASICOPTIONS + MAHJONGOPTIONS
    else:
        options = BASICOPTIONS + EAGLEOPTIONS
    options.append('-pta=' + analysis)
    options += ['-apppath', APPPATH[bm]]
    options += ['-reflectionlog', TAMIFLEXLOG[bm]]
    if bm in LIBPATH:
        options += ['-libpath', LIBPATH[bm]]
    options += ['-mainclass', MAINCLASSES[bm]]
    if MODULAR:
        options.append('-tmd')
    options.append('-cga=' + CALLGRAPHALG)
    cmd = ' '.join(options)
    return cmd


def runPTA(analysis, bm, OPTIONSTYLE):
    print('now running ' + tc.CYAN + analysis + tc.RESET + ' for ' + tc.YELLOW + bm + tc.RESET + ' ...')
    cmd = getPTACommand(analysis, bm, OPTIONSTYLE)
    analysisName = analysis
    if 'T-' in analysis:
        if MODULAR:
            analysisName = analysis + "+M"
    if DEBLOAT:
        if DEBLOATAPPROACH == 'CONCH':
            analysisName = analysis + '+D'
        elif DEBLOATAPPROACH == 'DEBLOATERX':
            analysisName = analysis + '+DX'
        else:
            analysisName = analysis + '+DC'
    outputFile = os.path.join(OUTPUTPATH, bm + '_' + analysisName + '.txt')
    if (analysisName in OoM and bm in OoM[analysisName]) or (analysisName in TIMEOUT and bm in TIMEOUT[analysisName]):
        print('predicted unscalable. skip this.')
        if not os.path.exists(outputFile):
            with open(outputFile, 'a') as f:
                f.write('predicted unscalable.')
        return
    if MULTSOLVER:
        cmd += ' -mult '
    if PREONLY:
        cmd += ' -pre '
    if DUMP:
        cmd += ' -dumpstats '
    if DEBLOAT:
        cmd += ' -cd '
        cmd += ' -cda=' + DEBLOATAPPROACH
    if not PRINT:
        if os.path.exists(outputFile):
            print('old result found. skip this.')
            return
        cmd += ' > ' + outputFile
    cmd += ' -jre=' + JREVERSION[bm]
    print(cmd)
    pta.runPointsToAnalysis(cmd.split())

OPTIONMESSAGE = 'The valid OPTIONs are:\n' \
                + option('-help|-h', 'print this message.') \
                + option('-print', 'print the analyses results on screen.') \
                + option('-clean', 'remove previous outputs.') \
                + option('-cd', 'enable context debloating.') \
                + option('-cda=<[CONCH|DEBLOATERX|COLLECTION]>', 'specify the debloating approach (default value is CONCH)') \
                + option('-dump', 'dump statistics into files.') \
                + option('<PTA>', 'specify pointer analysis.') \
                + option('-cga=<[CHA|VTA|RTA|GEOM|SPARK|QILIN]>', 'specify the callgraph construction algorithm (default value is QILIN)') \
                + option('<Benchmark>', 'specify benchmark.') \
                + option('-jre=<[jre1.6.0_45|jre1.8.0_312]>', 'specify the JRE version.') \
                + option('-out=<out>', 'specify output path.') \
                + option('-M', 'run Turner modularly.') \
                + option('-pre', 'run pre-analysis only.') \
                + option('-mult', 'use multiple-threaded solver.') \
                + option('-OS=<eagle|zipper|mahjong>', 'specify the style of configurations.') \
                + option('-all', 'run all analyses for specified benchmark(s) if ONLY benchmark(s) is specified;\n\
				run specified analyses for all benchmark(s) if ONLY analyses is specified;\n\
				run all analyses for all benchmarks if nothing specified.')

DUMP = False
PRINT = False
PREONLY = False
MODULAR = False
DEBLOAT = False
MULTSOLVER = False
DEBLOATAPPROACH = 'CONCH'
CALLGRAPHALG = 'QILIN'
OPTIONSTYLE = 'zipper'

if __name__ == '__main__':
    if '-help' in sys.argv or '-h' in sys.argv:
        sys.exit(OPTIONMESSAGE)
    if '-clean' in sys.argv:
        if os.path.exists(OUTPUTPATH):
            shutil.rmtree(OUTPUTPATH)
        sys.exit()
    if "-print" in sys.argv:
        PRINT = True
    if "-pre" in sys.argv:
        PREONLY = True
    if "-mult" in sys.argv:
        MULTSOLVER = True
    if "-M" in sys.argv:
        MODULAR = True
    if "-cd" in sys.argv:
        DEBLOAT = True
    if "-dump" in sys.argv:
        DUMP = True

    ALLBENCHMARKS = BENCHMARKS
    analyses = []
    benchmarks = []
    for arg in sys.argv:
        if arg in ANALYSES:
            analyses.append(arg)
        elif arg in ALLBENCHMARKS:
            benchmarks.append(arg)
        elif arg.startswith('-out='):
            OUTPUTPATH = arg[len('-out='):]
        elif arg.startswith('-OS='):
            OPTIONSTYLE = arg[len('-OS='):]
            print(OPTIONSTYLE)
        elif arg.startswith('-cda='):
            DEBLOATAPPROACH = arg[len('-cda='):]
            print(DEBLOATAPPROACH)
        elif arg.startswith('-cga='):
            CALLGRAPHALG = arg[len('-cga='):]
            print(CALLGRAPHALG)

    if "-all" in sys.argv:
        if len(benchmarks) == 0:
            benchmarks = ALLBENCHMARKS
        if len(analyses) == 0:
            analyses = ANALYSES

    if len(benchmarks) == 0:
        sys.exit("benchmark(s) not specified." + OPTIONMESSAGE)
    if len(analyses) == 0:
        analyses.append("insens")

    OUTPUTPATH = os.path.realpath(OUTPUTPATH)
    try:
        if not os.path.isdir(OUTPUTPATH):
            if os.path.exists(OUTPUTPATH):
                raise IOError('NC OUTPUTPATH')
            else:
                os.makedirs(OUTPUTPATH)
    except 'NC OUTPUTPATH':
        print(
            tc.RED + 'ERROR: ' + tc.RESET + 'CANNOT CREATE OUTPUTDIR: ' + tc.YELLOW + OUTPUTPATH + tc.RESET + ' ALREADY EXISTS AS A FILE!')

    Util.checkJavaVersion()
    for bm in benchmarks:
        for analysis in analyses:
            runPTA(analysis, bm, OPTIONSTYLE)
