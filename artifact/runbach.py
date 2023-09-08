#!/usr/bin/python3
import os, sys, shutil
import qilin as pta
from util.opt import *
import util.TerminalColor as tc
import util.dacapobach as db

ANALYSES = ['insens', 'Z-2o', 'E-2o', '2o', 'Z-3o', 'E-3o', '3o']

# eclipse and jython are always unscalable.
UNSCALABLE = {
    '3o': ['avrora', 'batik', 'h2', 'luindex', 'lusearch', 'pmd', 'sunflow', 'tradebeans', 'xalan'], # OoM
    'E-3o': ['avrora', 'batik', 'h2', 'luindex', 'lusearch', 'pmd', 'sunflow', 'tradebeans', 'xalan'], # OoM
    'Z-3o': ['avrora', 'batik', 'h2', 'luindex', 'lusearch', 'pmd', 'sunflow', 'tradebeans', 'xalan'], # OoM
    '3o+D': ['batik', 'h2'],
    '3o+DX': ['h2'],
}

BASICOPTIONS = ['-Xmx512g', '-timeout=43200', ]
# This setting is same as that used in Zipper's artifact.
ZIPPEROPTIONS = ['-pae', '-pe', '-clinit=ONFLY', '-lcs', '-mh']
# This setting is same as Zipper's except that we wont set ci and merge objects of type StringBuilder/StringBuffer/Throwable.
MAHJONGOPTIONS = ['-pae', '-pe', '-clinit=ONFLY']

OUTPUTPATH = 'output-bach'

BATCHPATH = 'benchmarks/dacapo-bach/'

def getPath(dir, file):
    return os.path.join(dir, file)

def getPTACommand(analysis, bm, OPTIONSTYLE):
    options = []
    options += BASICOPTIONS
    if OPTIONSTYLE == 'zipper':
        print('zipper style')
        options += ZIPPEROPTIONS
    elif OPTIONSTYLE == 'mahjong':
        print('mahjong style')
        options += MAHJONGOPTIONS
    options.append('-pta=' + analysis)
    options += ['-apppath', getPath(BATCHPATH, db.getBachAppJar(bm))]
    options += ['-libpath', getPath(BATCHPATH, db.getBachAppDep(bm))]
    options += ['-reflectionlog', getPath(BATCHPATH, db.getTamiflexLog(bm))]
    options += ['-mainclass', 'Harness']
    options += ['-jre=jre1.8.0_121_debug']
    options.append('-cga=' + CALLGRAPHALG)
    cmd = ' '.join(options)
    return cmd


def runPTA(analysis, bm, OPTIONSTYLE):
    print('now running ' + tc.CYAN + analysis + tc.RESET + ' for ' + tc.YELLOW + bm + tc.RESET + ' ...')
    cmd = getPTACommand(analysis, bm, OPTIONSTYLE)
    if DEBLOAT:
        if DEBLOATAPPROACH == 'CONCH':
            analysis = analysis + '+D'
        elif DEBLOATAPPROACH == 'DEBLOATERX':
            analysis = analysis + '+DX'
        else:
            analysis = analysis + '+DC'
    outputFile = os.path.join(OUTPUTPATH, bm + '_' + analysis + '.txt')
    if analysis in UNSCALABLE and bm in UNSCALABLE[analysis]:
        print('predicted unscalable. skip this.')
        if not os.path.exists(outputFile):
            with open(outputFile, 'a') as f:
                f.write('predicted unscalable.')
        return
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
    print(cmd)
    pta.runPointsToAnalysis(cmd.split())


OPTIONMESSAGE = 'The valid OPTIONs are:\n' \
                + option('-help|-h', 'print this message.') \
                + option('-print', 'print the analyses results on screen.') \
                + option('-clean', 'remove previous outputs.') \
                + option('-dump', 'dump statistics into files.') \
                + option('<PTA>', 'specify pointer analysis.') \
                + option('-cga=<[CHA|VTA|RTA|GEOM|SPARK|QILIN]>', 'specify the callgraph construction algorithm (default value is QILIN)') \
                + option('<Benchmark>', 'specify benchmark.') \
                + option('-out=<out>', 'specify output path.') \
                + option('-pre', 'run pre-analysis only.') \
                + option('-cd', 'enable context debloating.') \
                + option('-cda=<[CONCH|DEBLOATERX|COLLECTION]>', 'specify the debloating approach (default value is CONCH)') \
                + option('-OS=<zipper|mahjong>', 'specify the style of configurations.') \
                + option('-all', 'run all analyses for specified benchmark(s) if ONLY benchmark(s) is specified;\n\
				run specified analyses for all benchmark(s) if ONLY analyses is specified;\n\
				run all analyses for all benchmarks if nothing specified.')

DUMP = False
PRINT = False
PREONLY = False
DEBLOAT = False
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
    if "-cd" in sys.argv:
        DEBLOAT = True
    if "-dump" in sys.argv:
        DUMP = True

    analyses = []
    benchmarks = []
    for arg in sys.argv:
        if arg in ANALYSES:
            analyses.append(arg)
        elif arg in db.BENCHMARKS:
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
            benchmarks = db.BENCHMARKS
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

    for bm in benchmarks:
        for analysis in analyses:
            runPTA(analysis, bm, OPTIONSTYLE)
