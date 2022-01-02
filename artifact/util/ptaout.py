#!/usr/bin/env python3

'''
PTAOutput is an object that we used to store interesting infomation of the output of each pointer analysis.
'''


class PTAOutput(object):
    def __init__(self):
        self.app = ''
        self.analysisName = ''
        self.analysisTime = -1.0
        self.mayFailCasts = '-'
        self.callEdges = '-'
        self.polyCalls = '-'
        self.reachMethods = '-'
        self.avgPointsToSize = '-'
        self.csCallEdges = -1
        self.csGPts = -1
        self.csLPts = -1
        self.csFPts = -1
        # for zipper,eagle,turner
        self.speedup = '-'
        self.sparkTime = ""
        self.preAnalysisTime = ''
        self.graphNode = ''
        self.graphEdge = ''
        self.cinodes = '-'
        self.csnodes = '-'
        # context sensitivity of methods.
        self.sparkreachableMethod = ''
        self.fulllyCSMethod = ''
        self.partialCSMethod = ''
        self.CIMethod = ''
        # this is only for zipper.
        self.graphNode2 = ''
        self.graphEdge2 = ''
        # this is only for conch
        self.conchTime = ''
        self.conchCI = -1
        self.conchCS = -1
        self.avgctx = -1.0
        # only for turner
        self.ciocg = '-'
        self.cidfa = '-'
        self.csobj = '-'
        self.citop = ''
        self.cibot = ''
        self.citopbot = ''
        # context sensitivity of nodes, only for eagle and turner.
        self.csn = ['0', '0', '0', '0', '0', '0']

    def analysisCompleted(self):
        if self.analysisTime < 0:
            return False
        else:
            return True

    def csfacts(self):
        return self.csCallEdges + self.csGPts + self.csLPts + self.csFPts

    def parseAppName(self, file):
        self.app = file[file.rfind('/') + 1: file.rfind('_')]

    def parseAnalysisName(self, file):
        self.analysisName = file[file.rfind('_') + 1: -4]

    def parseAnalysisTime(self, line):
        at = line[line.find(':') + 1:].strip()
        self.analysisTime = float(at[:at.find('.') + 2])

    def parsePTAOutput(self, file):
        self.parseAppName(file)
        self.parseAnalysisName(file)
        f = open(file)
        for line in f:
            ln = line.strip()
            if 'Time (sec):' in ln:
                self.parseAnalysisTime(line)
            if 'May Fail Cast (Total):' in ln:
                self.mayFailCasts = ln[ln.find(':') + 1:].strip()
            if '#Virtual Call Site(Polymorphic):' in ln:
                self.polyCalls = ln[ln.find(':') + 1:].strip()
            if '#Call Edge(CI):' in ln:
                self.callEdges = ln[ln.find(':') + 1:].strip()
            if '#Reachable Method (CI):' in ln:
                self.reachMethods = ln[ln.find(':') + 1:].strip()
            if '#Local Avg Points-To Target(CI):' in ln:
                self.avgPointsToSize = str('%.3f' % float(ln[ln.find(':') + 1:].strip()))
            if '#Call Edge(CS):' in ln:
                self.csCallEdges = int(ln[ln.find(':') + 1:].strip())
            if '#Global CS Pointer-to Relation:' in ln:
                self.csGPts = int(ln[ln.find(':') + 1:].strip())
            if '#Local CS Pointer-to Relation:' in ln:
                self.csLPts = int(ln[ln.find(':') + 1:].strip())
            if '#Field CS Pointer-to Relation:' in ln:
                self.csFPts = int(ln[ln.find(':') + 1:].strip())
            # only for zipper, eagle, turner
            if 'Select time:' in ln:
                self.preAnalysisTime = ln[ln.find(':') + 1: -1]
            if 'Spark time:' in ln:
                self.sparkTime = ln[ln.find(':') + 1: -1]
            if '#Node:' in ln:
                self.graphNode = ln[ln.find(':') + 1:]
            if '#Edge:' in ln:
                self.graphEdge = ln[ln.find(':') + 1:]
            if '#CIN:' in ln:
                self.cinodes = ln[ln.find(':') + 2:].strip()
            if '#CSN:' in ln:
                self.csnodes = ln[ln.find(':') + 2:].strip()
            # only for zipper
            if '#Node2:' in ln:
                self.graphNode2 = ln[ln.find(':') + 1:]
            if '#Edge2:' in ln:
                self.graphEdge2 = ln[ln.find(':') + 1:]
            # only for conch
            if 'Context debloating time:' in ln:
                self.conchTime = ln[ln.find(':') + 1: -1]
            if '#CI:' in ln:
                self.conchCI = int(ln[ln.find(':') + 1: -1])
            if '#CS:' in ln:
                self.conchCS = int(ln[ln.find(':') + 1: -1])
            if '#Avg Context per Merthod:' in ln:
                self.avgctx = float(ln[ln.find(':') + 1:].strip())
            # only for turner
            if '#CIByOCG:' in ln:
                self.ciocg = int(ln[ln.find(':') + 1:])
            if '#CIByDFA:' in ln:
                self.cidfa = int(ln[ln.find(':') + 1:])
            if '#CSOBJ:' in ln:
                self.csobj = int(ln[ln.find(':') + 1:])
            if '#CITOP:' in ln:
                self.citop = int(ln[ln.find(':') + 1:])
            if '#CIBOT:' in ln:
                self.cibot = int(ln[ln.find(':') + 1:])
            if '#CITOPBOT:' in ln:
                self.citopbot = int(ln[ln.find(':') + 1:])

            # methods
            if '#ReachableMethod:' in ln:
                self.sparkreachableMethod = ln[ln.find(':') + 1:]
            if '#FCSM:' in ln:
                self.fulllyCSMethod = ln[ln.find(':') + 1:]
            if '#PCSM:' in ln:
                self.partialCSMethod = ln[ln.find(':') + 1:]
            if '#CIM:' in ln:
                self.CIMethod = ln[ln.find(':') + 1:]
            # Nodes
            if 'T' not in self.analysisName:
                if '#CIN:' in ln:
                    self.csn[0] = ln[ln.find(':') + 1:]
                if '#CSN:' in ln:
                    self.csn[1] = ln[ln.find(':') + 1:]
            else:
                if '#CSN_' in ln:
                    idx = int(ln[ln.find('_') + 1:ln.find(':')])
                    self.csn[idx] = ln[ln.find(':') + 1:]
        f.close()

    def dump(self):
        print(self.app + '_' + self.analysisName)
        print('\ttime:' + str(self.analysisTime))
        print('\tpre-time:' + self.preAnalysisTime)
        print('\tcast:' + self.mayFailCasts)
        print('\tcalledge: ' + self.callEdges)
        print('\tpolycall: ' + self.polyCalls)
        print('\treachmethod: ' + self.reachMethods)
        print('\tavgPointsToSize: ' + self.avgPointsToSize)
        print('\tspeedup: ' + self.speedup)
        print('\tcsCallEdges: ' + str(self.csCallEdges))
        print('\tcsGPts: ' + str(self.csGPts))
        print('\tcsFPts: ' + str(self.csFPts))
        print('\ttime:' + self.sparkTime)
        print('\t#CIN:' + self.cinodes)
        print('\t#CSN: ' + self.csnodes)
        print('\t#CIByOCG: ' + str(self.ciocg))
        print('\t#CIByDFA: ' + str(self.cidfa))
        print('\t#CSOBJ: ' + str(self.csobj))
        print('\t#sparkRM:' + self.sparkreachableMethod)
        print('\t#FCSM:' + self.fulllyCSMethod)
        print('\t#PCSM:' + self.partialCSMethod)
        print('\t#CIM:' + self.CIMethod)
        print('\tCSN:[' + ' '.join(self.csn) + ']')
