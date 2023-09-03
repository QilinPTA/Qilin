#!/usr/bin/env python3

# ignore eclipse and jython.
BENCHMARKS = ['avrora', 'batik', 'h2', 'luindex', 'lusearch', 'pmd', 'sunflow', 'tradebeans', 'xalan']


def getBachAppJar(app):
    return "%s.jar" % (app)


def getBachAppDep(app):
    return "%s-deps.jar" % (app)


def getTamiflexLog(app):
    if app == 'tradebeans':
        return "%s-tamiflex.log" % (app)
    else:
        return "%s-tamiflex-default.log" % (app)
