#!/usr/bin/python
import time
import os, sys
from collections import OrderedDict
import argparse

ROOT = sys.path[0]

DOOP_DIR = os.path.join(ROOT, 'Data-Driven-Doop')
DOOP_cache_DIR = os.path.join(DOOP_DIR, 'cache')
DOOP_LAST_DIR = os.path.join(DOOP_DIR, 'last-analysis')
DOOP_RESULTS_DIR = os.path.join(DOOP_DIR, 'results')
DOOP_OUTPUT_DIR = os.path.join(ROOT, 'output-client')
JARS_DIR = os.path.join(DOOP_DIR, 'jars')

APP = [
    'eclipse', 'chart', 'bloat', 'xalan', 'jython', 'hsqldb', 'luindex', 'lusearch', 'pmd', 'antlr'
]

CLIENT = {
    'Stats:Simple:PotentiallyFailingCast': 'mayfailcasts',
    'Stats:Simple:PolymorphicCallSite': 'polycalls',
    'CallGraphEdge': 'calledges',
    'Reachable': 'reachableMthds',
}

DPTA = OrderedDict()
DPTA['s2objH+Data'] = 'selective-2-object-sensitive+heap+Data'
DPTA['2objH+Data'] = '2-object-sensitive+heap+Data'
DPTA['2typeH+Data'] = '2-type-sensitive+heap+Data'

NPTA = OrderedDict()
NPTA['s2objH'] = 'selective-2-object-sensitive+heap'
NPTA['2objH'] = '2-object-sensitive+heap'
NPTA['2typeH'] = '2-type-sensitive+heap'

IPTA = OrderedDict()
IPTA['introA-s2objH'] = 'refA-selective-2-object-sensitive+heap'
IPTA['introA-2objH'] = 'refA-2-object-sensitive+heap'
IPTA['introA-2typeH'] = 'refA-2-type-sensitive+heap'
IPTA['introB-s2objH'] = 'refB-selective-2-object-sensitive+heap'
IPTA['introB-2objH'] = 'refB-2-object-sensitive+heap'
IPTA['introB-2typeH'] = 'refB-2-type-sensitive+heap'

PTA = OrderedDict()
PTA['introA-s2objH'] = 'refA-selective-2-object-sensitive+heap'
PTA['introA-2objH'] = 'refA-2-object-sensitive+heap'
PTA['introA-2typeH'] = 'refA-2-type-sensitive+heap'
PTA['introB-s2objH'] = 'refB-selective-2-object-sensitive+heap'
PTA['introB-2objH'] = 'refB-2-object-sensitive+heap'
PTA['introB-2typeH'] = 'refB-2-type-sensitive+heap'
PTA['s2objH'] = 'selective-2-object-sensitive+heap'
PTA['2objH'] = '2-object-sensitive+heap'
PTA['2typeH'] = '2-type-sensitive+heap'
PTA['s2objH+Data'] = 'selective-2-object-sensitive+heap+Data'
PTA['2objH+Data'] = '2-object-sensitive+heap+Data'
PTA['2typeH+Data'] = '2-type-sensitive+heap+Data'
PTA['insens'] = 'context-insensitive'


# data normal intro
def runDoop(pta, app, kind):
    if kind == 'data':
        analysis = DPTA[pta]
    elif kind == 'intro':
        analysis = IPTA[pta]
    elif kind == 'normal':
        analysis = NPTA[pta]
    else:
        analysis = PTA[pta]
    execDoop(analysis, app)


def execDoop(analysis, app):
    appDir = os.path.join(JARS_DIR, 'dacapo')
    print(app)
    cmd = './run -jre1.6 '
    cmd += ' ' + analysis + ' '
    cmd += os.path.join(appDir, app + '.jar')
    print(cmd)

    print
    '==========================================================='
    anaStr = analysis
    print
    'Running ' + anaStr + ' points-to analysis '
    os.system(cmd)
    # print 'Writing all detailed client results to %s ...' % DOOP_OUTPUT_DIR
    # if not os.path.exists(DOOP_OUTPUT_DIR):
    #  os.makedirs(DOOP_OUTPUT_DIR)
    # writeClientResult(anaStr, app, DOOP_LAST_DIR, DOOP_OUTPUT_DIR)
    # os.system('rm -r ' + DOOP_cache_DIR + '/*')
    print
    '==========================================================='
    print


def writeClientResult(anaName, app, dbDir, outputDir):
    for query, name in CLIENT.items():
        outputFile = os.path.join(outputDir, '%s-%s.%s' % (app, anaName, name))
        cmd = 'bloxbatch -db %s -query %s | sort > %s' % (dbDir, query, outputFile)
        # print cmd
        os.system(cmd)


def ScalabilityCheck(analysis, benchmark):
    if (analysis == 'introB-s2objH' or analysis == 'introB-2objH') and benchmark == 'jython':
        print(analysis + ' is not scalable for ' + benchmark)
        return 0
    if (analysis == 's2objH' or analysis == '2objH') and (benchmark == 'jython' or benchmark == 'hsqldb'):
        print(analysis + ' is not scalable for ' + benchmark)
        return 0
    if (analysis == '2typeH' and benchmark == 'jython'):
        print(analysis + ' is not scalable for ' + benchmark)
        return 0
    return 1


def run(args):
    os.chdir(DOOP_DIR)
    if args[0] == 'all':
        run(['data', 'all'])
        run(['intro', 'all'])
        run(['normal', 'all'])
        run(['insens', 'all'])
        return

    if args[0] == 'data':
        if args[1] == 'all':
            for app in APP:
                for Dpta in DPTA.keys():
                    runDoop(Dpta, app, 'data')
        else:
            app = args[1]
            for Dpta in DPTA.keys():
                runDoop(Dpta, app, 'data')
        return

    if args[0] == 'intro':
        if args[1] == 'all':
            for app in APP:
                for Ipta in IPTA.keys():
                    if (ScalabilityCheck(Ipta, app) == 1):
                        runDoop(Ipta, app, 'intro')
        else:
            app = args[1]
            for Ipta in IPTA.keys():
                runDoop(Dpta, app, 'intro')
        return

    if args[0] == 'normal':
        if args[1] == 'all':
            for app in APP:
                for Npta in NPTA.keys():
                    if (ScalabilityCheck(Npta, app) == 1):
                        runDoop(Npta, app, 'normal')
        else:
            app = args[1]
            for Npta in NPTA.keys():
                runDoop(Npta, app, 'normal')
        return

    else:
        analysis = args[0]
        if args[1] == 'all':
            for app in APP:
                if (ScalabilityCheck(analysis, app) == 1):
                    runDoop(analysis, app, 'specific')
        else:
            app = args[1]
            runDoop(analysis, app, 'specific')


def show_delta_time(t1, t2):
    spendtime = t2 - t1
    m, s = divmod(spendtime, 60)
    h, m = divmod(m, 60)
    print("%d:%02d:%02d" % (h, m, s))


if __name__ == '__main__':
    desc = 'Points-to analysis artifact'
    parser = argparse.ArgumentParser(description=desc)
    analysis_str = ', '.join(DPTA.keys() + NPTA.keys() + IPTA.keys() + PTA.keys())
    parser.add_argument('analysis', metavar='ANALYSIS',
                        help='Target analysis. ANALYSIS can be one of followings: all, data, intro, normal, ' + analysis_str)
    parser.add_argument('pgm', metavar='PROGRAM',
                        help='Target benchmark. PROGRAM can be one of followings: all, ' + ', '.join(APP))
    args = parser.parse_args()

    ''' Input validation '''
    if not ((args.analysis in DPTA) or (args.analysis in NPTA) or (args.analysis in IPTA) or (args.analysis in PTA) or (
            args.analysis == 'data') or (args.analysis == 'intro') or (args.analysis == 'normal') or (
                    args.analysis == 'all')):
        print('Invalid ANALYSIS: {}'.format(args.analysis))
        sys.exit()

    if not ((args.pgm == 'all') or (args.pgm in APP)):
        print('Invalid PROGRAM: {}'.format(args.pgm))
        sys.exit()

    t1 = time.time()
    run_args = []
    run_args.append(args.analysis)
    run_args.append(args.pgm)

    run(run_args)

    t2 = time.time()
    show_delta_time(t1, t2)
