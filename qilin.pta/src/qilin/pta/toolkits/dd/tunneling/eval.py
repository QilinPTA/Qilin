#!/usr/bin/python
import time
import os, sys
from collections import OrderedDict
import argparse

ROOT = sys.path[0]

DOOP_DIR = os.path.join(ROOT, 'artifact')
DOOP_cache_DIR = os.path.join(DOOP_DIR, 'cache')
DOOP_LAST_DIR = os.path.join(DOOP_DIR, 'last-analysis')
DOOP_RESULTS_DIR = os.path.join(DOOP_DIR, 'results')
DOOP_OUTPUT_DIR = os.path.join(ROOT, 'output-client')
JARS_DIR = os.path.join(DOOP_DIR, 'jars')

APP = [
    'luindex', 'lusearch', 'antlr', 'pmd', 'eclipse', 'xalan', 'fop', 'chart', 'bloat', 'jython',
]
TRAIN = [
    'luindex', 'lusearch', 'antlr', 'pmd',
]
TEST = [
    'eclipse', 'xalan', 'fop', 'chart', 'bloat', 'jython',
]

CLIENT = {
    'Stats:Simple:PotentiallyFailingCast': 'mayfailcasts',
    'Stats:Simple:PolymorphicCallSite': 'polycalls',
    'CallGraphEdge': 'calledges',
    'Reachable': 'reachableMthds',
}

TPTA = OrderedDict()
TPTA['s1objH+T'] = 'selective-1-tunneled-object-sensitive+heap'
TPTA['1objH+T'] = '1-tunneled-object-sensitive+heap'
TPTA['1typeH+T'] = '1-tunneled-type-sensitive+heap'
TPTA['1callH+T'] = '1-tunneled-call-site-sensitive+heap'

NPTA = OrderedDict()
NPTA['s2objH'] = 'selective-2-object-sensitive+heap'
NPTA['2objH'] = '2-object-sensitive+heap'
NPTA['2typeH'] = '2-type-sensitive+heap'
NPTA['2callH'] = '2-call-site-sensitive+heap'
NPTA['s1objH'] = 'selective-1-object-sensitive+heap'
NPTA['1objH'] = '1-object-sensitive+heap'
NPTA['1typeH'] = '1-type-sensitive+heap'
NPTA['1callH'] = '1-call-site-sensitive+heap'

PTA = OrderedDict()
PTA['s1objH+T'] = 'selective-1-tunneled-object-sensitive+heap'
PTA['1objH+T'] = '1-tunneled-object-sensitive+heap'
PTA['1typeH+T'] = '1-tunneled-type-sensitive+heap'
PTA['1callH+T'] = '1-tunneled-call-site-sensitive+heap'
PTA['s2objH'] = 'selective-2-object-sensitive+heap'
PTA['2objH'] = '2-object-sensitive+heap'
PTA['2typeH'] = '2-type-sensitive+heap'
PTA['2callH'] = '2-call-site-sensitive+heap'
PTA['s1objH'] = 'selective-1-object-sensitive+heap'
PTA['1objH'] = '1-object-sensitive+heap'
PTA['1typeH'] = '1-type-sensitive+heap'
PTA['1callH'] = '1-call-site-sensitive+heap'


# data normal intro
def runDoop(pta, app, kind):
    if kind == 'tunneling':
        analysis = TPTA[pta]
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
    # print(cmd)

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
    print
    '==========================================================='
    print
    os.system('rm -r ' + DOOP_cache_DIR + '/*')


# def writeClientResult(anaName, app, dbDir, outputDir):
# for query, name in CLIENT.items():
#  outputFile = os.path.join(outputDir, '%s-%s.%s' % (app, anaName, name))
#  cmd = 'bloxbatch -db %s -query %s | sort > %s' % (dbDir, query, outputFile)
#  #print cmd
#  os.system(cmd)


def ScalabilityCheck(analysis, benchmark):
    if (
            analysis == 's2objH' or analysis == '2objH' or analysis == '1objH' or analysis == '1objH+T' or analysis == '2typeH') and (
            benchmark == 'jython'):
        print(analysis + ' is not scalable for ' + benchmark)
        return 0
    if (analysis == '2callH' and benchmark == 'bloat'):
        print(analysis + ' is not scalable for ' + benchmark)
        return 0
    return 1


def run(args):
    os.chdir(DOOP_DIR)
    if args[0] == 'all':
        run(['tunneling', 'all'])
        run(['normal', 'all'])
        return

    if args[0] == 'tunneling':
        if args[1] == 'all':
            for app in APP:
                for Tpta in TPTA.keys():
                    if (ScalabilityCheck(Tpta, app) == 1):
                        runDoop(Tpta, app, 'tunneling')
        elif args[1] == 'training':
            for app in TRAIN:
                for Tpta in TPTA.keys():
                    if (ScalabilityCheck(Tpta, app) == 1):
                        runDoop(Tpta, app, 'tunneling')
        elif args[1] == 'testing':
            for app in TEST:
                for Tpta in TPTA.keys():
                    if (ScalabilityCheck(Tpta, app) == 1):
                        runDoop(Tpta, app, 'tunneling')
        else:
            app = args[1]
            for Tpta in TPTA.keys():
                runDoop(Tpta, app, 'tunneling')

        return

    if args[0] == 'normal':

        if args[1] == 'all':
            for app in APP:
                for Npta in NPTA.keys():
                    if (ScalabilityCheck(Npta, app) == 1):
                        runDoop(Npta, app, 'normal')
        elif args[1] == 'training':
            for app in TRAIN:
                for Npta in NPTA.keys():
                    if (ScalabilityCheck(Npta, app) == 1):
                        runDoop(Npta, app, 'normal')
        elif args[1] == 'testing':
            for app in TEST:
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
        elif args[1] == 'training':
            for app in TRAIN:
                if (ScalabilityCheck(analysis, app) == 1):
                    runDoop(analysis, app, 'specific')
        elif args[1] == 'testing':
            for app in TEST:
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
    analysis_str = ', '.join(TPTA.keys() + NPTA.keys() + PTA.keys())
    parser.add_argument('analysis', metavar='ANALYSIS',
                        help='Target analysis. ANALYSIS can be one of followings: all, data, intro, normal, ' + analysis_str)
    parser.add_argument('pgm', metavar='PROGRAM',
                        help='Target benchmark. PROGRAM can be one of followings: all, ' + ', '.join(APP))
    args = parser.parse_args()

    ''' Input validation '''
    if not ((args.analysis in TPTA) or (args.analysis in NPTA) or (args.analysis in PTA) or (
            args.analysis == 'tunneling') or (args.analysis == 'normal') or (args.analysis == 'all')):
        print('Invalid ANALYSIS: {}'.format(args.analysis))
        sys.exit()

    if not ((args.pgm == 'all') or (args.pgm == 'training') or (args.pgm == 'testing') or (args.pgm in APP)):
        print('Invalid PROGRAM: {}'.format(args.pgm))
        sys.exit()

    t1 = time.time()
    run_args = []
    run_args.append(args.analysis)
    run_args.append(args.pgm)

    run(run_args)

    t2 = time.time()
    show_delta_time(t1, t2)
