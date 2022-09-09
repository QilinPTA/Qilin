#!/usr/bin/env python3

BENCHMARKS = ['antlr', 'bloat', 'chart', 'eclipse', 'fop', 'luindex', 'lusearch', 'pmd', 'xalan', 'checkstyle', 'JPC',
              'findbugs']

MAINCLASSES = {
    'antlr': 'dacapo.antlr.Main',
    'bloat': 'dacapo.bloat.Main',
    'chart': 'dacapo.chart.Main',
    'eclipse': 'dacapo.eclipse.Main',
    'fop': 'dacapo.fop.Main',
    'luindex': 'dacapo.luindex.Main',
    'lusearch': 'dacapo.lusearch.Main',
    'pmd': 'dacapo.pmd.Main',
    'xalan': 'dacapo.xalan.Main',
    'checkstyle': 'com.puppycrawl.tools.checkstyle.Main',
    'findbugs': 'edu.umd.cs.findbugs.FindBugs',
    'JPC': 'org.jpc.j2se.JPCApplication',
}

APPPATH = {
    'antlr': 'benchmarks/dacapo2006/antlr.jar',
    'bloat': 'benchmarks/dacapo2006/bloat.jar',
    'chart': 'benchmarks/dacapo2006/chart.jar',
    'eclipse': 'benchmarks/dacapo2006/eclipse.jar',
    'fop': 'benchmarks/dacapo2006/fop.jar',
    'luindex': 'benchmarks/dacapo2006/luindex.jar',
    'lusearch': 'benchmarks/dacapo2006/lusearch.jar',
    'pmd': 'benchmarks/dacapo2006/pmd.jar',
    'xalan': 'benchmarks/dacapo2006/xalan.jar',
    'checkstyle': 'benchmarks/applications/checkstyle/checkstyle-5.7-all.jar',
    'findbugs': 'benchmarks/applications/findbugs/findbugs.jar',
    'JPC': 'benchmarks/applications/JPC/JPCApplication.jar',
}

LIBPATH = {
    'antlr': 'benchmarks/dacapo2006/antlr-deps.jar',
    'bloat': 'benchmarks/dacapo2006/bloat-deps.jar',
    'chart': 'benchmarks/dacapo2006/chart-deps.jar',
    'eclipse': 'benchmarks/dacapo2006/eclipse-deps.jar',
    'fop': 'benchmarks/dacapo2006/fop-deps.jar',
    'luindex': 'benchmarks/dacapo2006/luindex-deps.jar',
    'lusearch': 'benchmarks/dacapo2006/lusearch-deps.jar',
    'pmd': 'benchmarks/dacapo2006/pmd-deps.jar',
    'xalan': 'benchmarks/dacapo2006/xalan-deps.jar',
    'checkstyle': 'benchmarks/applications/checkstyle/',
    'findbugs': 'benchmarks/applications/findbugs/',
    'JPC': 'benchmarks/applications/JPC/',
}

TAMIFLEXLOG = {
    'antlr': 'benchmarks/dacapo2006/antlr-refl.log',
    'bloat': 'benchmarks/dacapo2006/bloat-refl.log',
    'chart': 'benchmarks/dacapo2006/chart-refl.log',
    'eclipse': 'benchmarks/dacapo2006/eclipse-refl.log',
    'fop': 'benchmarks/dacapo2006/fop-refl.log',
    'luindex': 'benchmarks/dacapo2006/luindex-refl.log',
    'lusearch': 'benchmarks/dacapo2006/lusearch-refl.log',
    'pmd': 'benchmarks/dacapo2006/pmd-refl.log',
    'xalan': 'benchmarks/dacapo2006/xalan-refl.log',
    'checkstyle': 'benchmarks/applications/checkstyle/checkstyle-refl.log',
    'findbugs': 'benchmarks/applications/findbugs/findbugs-refl.log',
    'JPC': 'benchmarks/applications/JPC/JPC-refl.log',
}

JREVERSION = {
    'antlr': 'jre1.6.0_45',
    'bloat': 'jre1.6.0_45',
    'chart': 'jre1.6.0_45',
    'eclipse': 'jre1.6.0_45',
    'fop': 'jre1.6.0_45',
    'luindex': 'jre1.6.0_45',
    'lusearch': 'jre1.6.0_45',
    'pmd': 'jre1.6.0_45',
    'xalan': 'jre1.6.0_45',
    'checkstyle': 'jre1.6.0_45',
    'findbugs': 'jre1.6.0_45',
    'JPC': 'jre1.6.0_45',
}
