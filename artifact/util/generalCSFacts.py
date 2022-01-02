#!/usr/bin/env python3

import util.Tex as Tex
import util.Util as Util
from util.common import TOOLNAMEMAP


# latex code for table head
def genTableHeadPart(analysisList):
    headPart = [
        r"\begin{table}[htbp]",
        r"\centering",
        r"\caption{Context-sensitive facts (in millions). For all the metrics, smaller is better.}",
        r"\label{table:main}",
        r"\scalebox{0.75}{",
        r"\begin{tabular}{@{}|c|l|",
    ]

    ret = "\n".join(headPart)
    for i in range(len(analysisList)):
        ret += " r |"
    ret += "@{}}	\\hline \n"
    ret += "\t Benchmark \t & Metrics \t "
    for elem in analysisList:
        ret += "& " + TOOLNAMEMAP[elem] + " \t "
    ret += "\\\\ \\hline\n"
    return ret


# generate latex code for table body.
def genTableTexContentForOneApp(app, ptaOutputs, analysisList):
    # ordered by analysis name
    anaName2Obj = Util.buildAnalysisNameToObjMap(ptaOutputs)
    edges = ['', '\#cs-calls']
    csgpts = ['', '\#cs-gpts']
    cslpts = ['', '\#cs-pts']
    csfpts = ['', '\#cs-fpts']
    sum = ['', 'total']
    allAnaList = []
    allAnaList.extend(analysisList)
    for elem in allAnaList:
        if elem in anaName2Obj:
            ptaOutput = anaName2Obj[elem]
            edges.append(str(ptaOutput.csCallEdges))
            csgpts.append(str(ptaOutput.csGPts))
            cslpts.append(str(ptaOutput.csLPts))
            csfpts.append(str(ptaOutput.csFPts))
            sum.append(str(ptaOutput.csGPts + ptaOutput.csCallEdges + ptaOutput.csLPts + ptaOutput.csFPts))
        else:
            edges.append('-')
            csgpts.append('-')
            cslpts.append('-')
            csfpts.append('-')
            sum.append('-')

    ret = "\t &".join(csgpts) + "\\\\ \n"
    ret += "\t &".join(cslpts) + "\\\\ \n"
    ret += "\t &".join(csfpts) + "\\\\ \n"
    ret += "\t &".join(edges) + "\\\\ \n"
    ret += '\multirow{-4}{*}{' + app + '}' + "\t &".join(sum) + "\\\\ \\hline\n"
    return ret


# input should be a list of PTAOutput instances.
def genTable(allPtaOutput, benchmarks, analysisList):
    # classify by App name.
    texContent = genTableHeadPart(analysisList)
    ret = Util.classifyByAppName(allPtaOutput)
    for app in benchmarks:
        if app not in ret:
            continue
        ptaOutputs = ret[app]
        texContent += genTableTexContentForOneApp(app, ptaOutputs, analysisList)
    texContent += Tex.genTableTailPart()
    return texContent


def genGeneralCSTable(allPtaOutput, benchmarks, analysisList, outputfile):
    texContent = Tex.genDocHeadPart()
    texContent += genTable(allPtaOutput, benchmarks, analysisList)
    texContent += Tex.genDocTailPart()
    f = open(outputfile, "w")
    f.write(texContent)
    f.close()
