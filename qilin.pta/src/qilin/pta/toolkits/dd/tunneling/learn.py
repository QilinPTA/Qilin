#!/usr/bin/python
import sys
import argparse
import os
from tabulate import tabulate
import time
from scripts.context_compress import features
from scripts.context_compress import learn_context_comp
from scripts.utils import *
import ConfigParser
import pickle

analysis_full_name = {'obj': '1-tunneled-object-sensitive+heap', 'typ': '1-tunneled-type-sensitive+heap',
                      'cs': '1-tunneled-call-site-sensitive+heap', 'sobj': 'selective-1-tunneled-object-sensitive+heap'}

''' Main driver '''
if __name__ == '__main__':
    desc = 'Learning Context tunneling Heuristics'
    parser = argparse.ArgumentParser(description=desc, formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('analysis', metavar='ANALYSIS', help='Target analysis. (obj, cs, sobj, typ)')
    parser.add_argument('--cachedFeatEval', action='store_true',
                        help='Use cached performance of atomic features if possible.')
    args = parser.parse_args()

    ''' Conf validation '''
    config = ConfigParser.ConfigParser()
    try:
        config.read('learn.conf')
    except:
        print('Invalid learn.conf.')
        sys.exit()

    ''' Input validation '''
    if not ((args.analysis == 'obj') or (args.analysis == 'typ') or (args.analysis == 'cs') or (
            args.analysis == 'sobj')):
        print('Invalid analysis: {}'.format(args.analysis))
        sys.exit()
    else:
        analysis = args.analysis

    training_caches = os.path.abspath(config.get('CACHE', 'folder'))
    if not (os.path.exists(training_caches)):
        try:
            os.makedirs(training_caches)
        except OSError:
            print('Failed to create cache directory.')
            sys.exit()

    dooppath = check_path(config.get('ANALYZER', 'bean'), 'ANALYZER')

    programs = check_path(config.get('PGMS', 'pgms'), 'PGMS')

    featurejson = {}
    if os.path.exists('features.json'):
        try:
            with open('features.json', 'r') as f:
                featurejson = json.load(f)
        except ValueError:
            print('Invalid features.json')
            sys.exit()
    else:
        print('features.json not available.')
        sys.exit()

    features.write_featurelogic(dooppath, featurejson)
    with open('features.json', 'w') as f:
        json.dump(featurejson, f, indent=4, sort_keys=True)

    init_formula = [['Empty']]
    init_worklist = ['Feature' + k for k in featurejson.keys()]

    ''' Training programs validation '''
    training_pgms = []  # {'title': ..., 'jar': ...}
    try:
        with open(programs, 'r') as f:
            line = f.read()
            lines = line.splitlines()
            for line in lines:
                title_path = line.split(',')
                training_pgms.append({'title': title_path[0].strip(), 'jar': title_path[1].strip()})
    except IOError:
        print('{} doesn\'t exist.'.format(config.get('PGMS', 'pgms')))
        sys.exit()

    ''' Initialize '''
    print('=====SETTING=====')
    print('Analysis: {}'.format(analysis_full_name[analysis]))
    print('Doop: {}'.format(dooppath))
    print('Training caches: {}'.format(training_caches))
    print('Training programs:')
    table = []
    for pgm in training_pgms:
        row = [pgm['title']]
        row.append(pgm['jar'])
        table.append(row)
    print
    tabulate(table, headers=['Benchmark', 'JAR'])
    features.print_features(featurejson)
    print('')

    ''' Learn '''
    print('2. Learning heuristic for Tunneling')
    tunneling = 't'
    training_materials_path = os.path.abspath(os.path.join('./',
                                                           training_caches,
                                                           analysis_full_name[analysis] + '_cc'))
    if not (os.path.exists(training_materials_path)):
        try:
            os.makedirs(training_materials_path)
        except OSError:
            print('Failed to create folder {}.'.format(training_materials_path))
            sys.exit()
    print('Performance of atomic features will be cached in {}.'.format(training_materials_path))

    print('2.1. Obtaining precision bounds...')
    t1 = time.time()
    for pgm in training_pgms:
        perf = {}
        empty_filename = os.path.join(training_materials_path, pgm['title'] + '_' + tunneling + '_empty.pickle')
        if os.path.exists(empty_filename):
            with open(empty_filename, 'rb') as f:
                perf = pickle.load(f)
        else:
            # Empty NotImportant set == All methods are important == original analysis
            perf = learn_context_comp.runF(dooppath, pgm['jar'], [['Empty']], analysis, tunneling, [['Empty']])
            with open(empty_filename, 'wb') as f:
                pickle.dump(perf, f)
                print('{} is written.'.format(empty_filename))

        pgm['formula'] = perf['formula']
        pgm['cast_precision_empty'] = perf['cast_num_of_proven']
        pgm['cast_proven_queries'] = perf['cast_proven_queries']
        pgm['cast_unproven_queries'] = perf['cast_unproven_queries']
        pgm['cast_chosen_methods'] = perf['cast_chosen_methods']
        pgm['cast_cost_empty'] = perf['time']

        perf_org = {}
        org_analyses = {'obj': 'oobj', 'typ': 'otyp', 'cs': 'ocs', 'sobj': 'osobj'}
        org_filename = os.path.join(training_materials_path, pgm['title'] + '_org.pickle')
        if os.path.exists(org_filename):
            with open(org_filename, 'rb') as f:
                perf_org = pickle.load(f)
        else:
            perf_org = learn_context_comp.runF(dooppath, pgm['jar'], [['Empty']], org_analyses[analysis], tunneling,
                                               [['Empty']])
            with open(org_filename, 'wb') as f:
                pickle.dump(perf_org, f)
                print('{} is written.'.format(org_filename))

        pgm['cast_cost_org'] = perf_org['time']
    t2 = time.time()
    print('Elapsed time '),
    show_delta_time(t1, t2)
    print('')

    # Learn
    print('2.2. Learning a Boolean formula for Tunneling')
    # args
    # training_pgms = {'jar', 'title',
    #                 'cast_precision_empty', 'cast_proven_queries',
    #                 'cast_unproven_queries', 'cast_chosen_methods'}
    formula = learn_context_comp.learn(args.cachedFeatEval, analysis, training_materials_path, training_pgms,
                                       featurejson, dooppath, tunneling, init_formula, init_worklist, [['Empty']])
    # formula = [['Feature20','Feature28','Feature11','Feature17','Feature45','Feature0','Feature5'],['Feature36','Feature11','Feature0','Feature28','Feature26','Feature31','Feature17','Feature5','Feature19','Feature7','Feature14','Feature21'], ['Feature44','Feature28','Feature25','Feature11','Feature37','Feature26','Feature17','Feature39','Feature31','Feature5','Feature9','Feature35','Feature6','Feature14','Feature2','Feature32','Feature21']]
    # formula = [['Feature20', 'Feature17', 'Feature11', 'Feature0'], ['Feature18', 'Feature8', 'Feature0']]
    print('')
    print('Best formula for Tunneling: {}'.format(formula))
    print('')
    t3 = time.time()
    print('Elapsed time '),
    show_delta_time(t2, t3)

    print('3. Learning heuristic for TunnelingM')
    tunneling = 'tm'
    learned_formula = list(formula)

    print('3.1. Obtaining precision bounds...')
    t1 = time.time()
    for pgm in training_pgms:
        perf = {}
        empty_filename = os.path.join(training_materials_path, pgm['title'] + '_' + tunneling + '_empty.pickle')
        if os.path.exists(empty_filename):
            with open(empty_filename, 'rb') as f:
                perf = pickle.load(f)
        else:
            # Empty NotImportant set == All methods are important == original analysis
            perf = learn_context_comp.runF(dooppath, pgm['jar'], [['Empty']], analysis, tunneling, learned_formula)
            with open(empty_filename, 'wb') as f:
                pickle.dump(perf, f)
                print('{} is written.'.format(empty_filename))

        pgm['formula'] = perf['formula']
        pgm['cast_precision_empty'] = perf['cast_num_of_proven']
        pgm['cast_proven_queries'] = perf['cast_proven_queries']
        pgm['cast_unproven_queries'] = perf['cast_unproven_queries']
        pgm['cast_chosen_methods'] = perf['cast_chosen_methods']
        pgm['cast_cost_empty'] = perf['time']

        perf_org = {}
        org_analyses = {'obj': 'oobj', 'typ': 'otyp', 'cs': 'ocs', 'sobj': 'osobj'}
        org_filename = os.path.join(training_materials_path, pgm['title'] + '_org.pickle')
        if os.path.exists(org_filename):
            with open(org_filename, 'rb') as f:
                perf_org = pickle.load(f)
        else:
            perf_org = learn_context_comp.runF(dooppath, pgm['jar'], [['Empty']], org_analyses[analysis], tunneling,
                                               [['Empty']])
            with open(org_filename, 'wb') as f:
                pickle.dump(perf_org, f)
                print('{} is written.'.format(org_filename))

        pgm['cast_cost_org'] = perf_org['time']
    t2 = time.time()
    print('Elapsed time '),
    show_delta_time(t1, t2)
    print('')

    # Learn
    print('3.2. Learning a Boolean formula for TunnelingM')
    # args
    # training_pgms = {'jar', 'title',
    #                 'cast_precision_empty', 'cast_proven_queries',
    #                 'cast_unproven_queries', 'cast_chosen_methods'}
    formula = learn_context_comp.learn(args.cachedFeatEval, analysis, training_materials_path, training_pgms,
                                       featurejson, dooppath, tunneling, init_formula, init_worklist, learned_formula)
    # formula = [['Feature20', 'Feature17', 'Feature11', 'Feature0'], ['Feature18', 'Feature8', 'Feature0']]
    print('')
    print('Best formula for TunnelingM: {}'.format(formula))
    print('')
    t3 = time.time()
    print('Elapsed time '),
    show_delta_time(t2, t3)

    ''' Output select.logic '''
    print('4. Applying learned formulas')
    learn_context_comp.write_select_logic(formula, dooppath, tunneling, learned_formula)
    print('')

    print('FINAL RESULT')
    print('Formula for Tunneling: {}'.format(learn_context_comp.formula_to_string2(learned_formula)))
    print('Formula for TunnelingM: {}'.format(learn_context_comp.formula_to_string2(formula)))
