#!/usr/bin/python
import os
from scripts import utils
from tabulate import tabulate
import re
import sys


def write_featurelogic(dooppath, json_data):
    logicpath = utils.check_path(os.path.join(dooppath, 'logic'), 'logic file')
    with open(os.path.join(logicpath, 'features.logic'), 'w') as f:
        for k in json_data.keys():
            f.write('{}\n'.format(json_data[k]['Rule']))
    declpath = utils.check_path(os.path.join(dooppath, 'logic'), 'logic file')
    with open(os.path.join(declpath, 'features-decl.logic'), 'w') as f:
        for k in json_data.keys():
            f.write('{}\n'.format(json_data[k]['Def']))
    return


def print_features(featuredata):
    feat_pattern = re.compile(r'Feature\d+\(\?meth\)<-(.*),MethodSignatureRef\(\?meth\)\.')
    table = []
    keys = featuredata.keys()
    keys.sort()
    for feat in keys:
        row = ['Feature' + feat]
        m = feat_pattern.search(featuredata[feat]['Rule'])
        if m:
            row.append(m.group(1))
        else:
            print('Feature data has invalid contents')
            sys.exit()
        table.append(row)
    print('[[Atomic Features]]')
    print
    tabulate(table, headers=['Feature ID', 'Rule'])
    return
