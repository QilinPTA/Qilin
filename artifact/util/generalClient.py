#!/usr/bin/env python3

import util.Tex as Tex
import util.Util as Util
from util.common import TOOLNAMEMAP


# latex code for table head
def genTableHeadPart(analysisList):
    headPart = [
        r"\begin{table}[hbtp]",
        r"\centering",
        r"\caption{Main analysis results. }",
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
    times = ['', 'Time (s)']
    casts = ['', '\#may-fail-casts']
    edges = ['', '\#call-edges']
    poly = ['', '\#poly-calls']
    avgpts = ['', '\#avg-pts']
    reachs = ['', '\#reach-methods']
    allAnaList = []
    allAnaList.extend(analysisList)
    for elem in allAnaList:
        if elem in anaName2Obj:
            ptaOutput = anaName2Obj[elem]
            timeStr = '%.1f' % ptaOutput.analysisTime
            times.append(timeStr)
            casts.append(ptaOutput.mayFailCasts)
            edges.append(ptaOutput.callEdges)
            poly.append(ptaOutput.polyCalls)
            pts = ptaOutput.avgPointsToSize
            avgpts.append(pts[:pts.find('.') + 9])
            reachs.append(ptaOutput.reachMethods)
        else:
            times.append('')
            casts.append('')
            edges.append('')
            poly.append('')
            avgpts.append('')
            reachs.append('')

    ret = "\t &".join(times) + "\\\\ \n"
    ret += "\t &".join(casts) + "\\\\ \n"
    ret += "\t &".join(edges) + "\\\\ \n"
    ret += "\t &".join(poly) + "\\\\ \n"
    ret += "\t &".join(reachs) + "\\\\ \n"
    ret += '\multirow{-6}{*}{' + app + '}' + "\t &".join(avgpts) + "\\\\ \\hline\n"
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


def genGeneralClientTable(allPtaOutput, outputfile, benchmarks, analysisList):
    texContent = Tex.genDocHeadPart()
    texContent += genTable(allPtaOutput, benchmarks, analysisList)
    texContent += Tex.genDocTailPart()
    f = open(outputfile, "w")
    f.write(texContent)
    f.close()
