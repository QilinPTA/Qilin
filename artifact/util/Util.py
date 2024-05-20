#!/usr/bin/env python3

import math

import os
import subprocess
from util.ptaout import PTAOutput


def checkJavaVersion():
    javaversion = subprocess.check_output(['java', '-version'], stderr=subprocess.STDOUT)
    jv = javaversion.decode("utf-8")
    version_info = jv.split()[2].replace('"', '')
    major_version = int(version_info.split('.')[0])
    if major_version < 16:
        print(jv)
        print('Error: JRE version does not meet the minimum requirement, i.e., >= 16')
        exit()


def checkConsistency(pta1, pta2):
    if pta1.app != pta2.app or pta1.analysisName != pta2.analysisName or \
            pta1.mayFailCasts != pta2.mayFailCasts or \
            pta1.callEdges != pta2.callEdges or pta1.polyCalls != pta2.polyCalls or \
            pta1.avgPointsToSize != pta2.avgPointsToSize or \
            pta1.speedup != pta2.speedup or \
            pta1.csCallEdges != pta2.csCallEdges or \
            pta1.csGPts != pta2.csGPts or \
            pta1.csLPts != pta2.csLPts or \
            pta1.csFPts != pta2.csFPts or \
            pta1.sparkreachableMethod != pta2.sparkreachableMethod or pta1.ciocg != pta2.ciocg or \
            pta1.cidfa != pta2.cidfa or pta1.csobj != pta2.csobj:
        return False
    return True


def merge(pta1, pta2, verbose):
    if pta1.analysisCompleted():
        pta1.analysisTime = float(pta1.analysisTime) + float(pta2.analysisTime)
    pta1.sparkTime += pta2.sparkTime
    pta1.preAnalysisTime += pta2.preAnalysisTime
    if verbose and not checkConsistency(pta1, pta2):
        print('Inconsistent pta outputs:')
        pta1.dump()
        print()
        pta2.dump()
    return pta1


# given a run, build such kinds of a map: Map<APPName, Map<analysisName, PtaOutput>>
def buildApp2Tool2PtaOutputMap(run):
    ret = {}
    app2ptas = Util.classifyByAppName(run)
    for app in app2ptas:
        ret[app] = Util.buildAnalysisNameToObjMap(app2ptas[app])
    return ret


def mergeHelper(app2tool2pta1, app2tool2pta2):
    ret = {}
    for app in app2tool2pta1:
        tool2pta1 = app2tool2pta1[app]
        tool2pta2 = app2tool2pta2[app]
        ret[app] = {}
        for tool in tool2pta1:
            pta1 = tool2pta1[tool]
            pta2 = tool2pta2[tool]
            ret[app][tool] = merge(pta1, pta2, True)
    return ret


def average(lst):
    return sum(lst) / len(lst)


# give a list, output a list of the log2 value of each element
def mylog2(mList):
    for i in range(len(mList)):
        mList[i] = math.log2(mList[i])
    return mList


# input should be a list of PTAOutput instances.
def buildAnalysisNameToObjMap(ptaOutputs):
    ret = {}
    for elem in ptaOutputs:
        ret[elem.analysisName] = elem
    return ret


# input should be a list of PTAOutput instances.
def buildAppNameToObjMap(ptaOutputs):
    ret = {}
    for elem in ptaOutputs:
        ret[elem.app] = elem
    return ret


# input should be a list of PTAOutput instances.
def classifyByToolName(allPtaOutput):
    ret = {}
    for elem in allPtaOutput:
        if elem.analysisName not in ret:
            ret[elem.analysisName] = []
        ret[elem.analysisName].append(elem)
    return ret


# input should be a list of PTAOutput instances.
def classifyByAppName(ptaOutputs):
    ret = {}
    for elem in ptaOutputs:
        if elem.app not in ret:
            ret[elem.app] = []
        ret[elem.app].append(elem)
    return ret


# input should be a list of PTAOutput instances.
def classifyByAppName2(ptaOutputs, analysisName):
    ret = {}
    for elem in ptaOutputs:
        if elem.analysisName == analysisName:
            ret[elem.app] = elem
    return ret


def loadPtaOutputs(analysisList, benchmarks, ptaOutputPath):
    allOutput = []
    for r, d, f in os.walk(ptaOutputPath):
        for file in f:
            path = os.path.join(r, file)
            appName = path[path.rfind('/') + 1: path.rfind('_')]
            analysisName = file[file.rfind('_') + 1: -4]
            if appName in benchmarks and analysisName in analysisList:
                ptaOutput = PTAOutput()
                ptaOutput.parsePTAOutput(path)
                allOutput.append(ptaOutput)
    return allOutput
