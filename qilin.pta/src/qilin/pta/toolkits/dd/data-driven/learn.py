#!/usr/bin/python
import sys
import argparse
import os
from tabulate import tabulate
# from learn import *
import time
import re
import json
from pprint import pprint
import pickle
import subprocess

''' Functions for learning  '''


def beep():
    print
    '\a'


def show_delta_time(t1, t2):
    spendtime = t2 - t1
    m, s = divmod(spendtime, 60)
    h, m = divmod(m, 60)
    print("%d:%02d:%02d" % (h, m, s))


def check_path(path):
    abspath = os.path.abspath(path)
    if not (os.path.isdir(abspath) or os.path.isfile(abspath)):
        print('ERROR: {} is not valid path'.format(abspath))
        sys.exit()
    else:
        return abspath


def check_and_load_json(abspath):
    try:
        with open(abspath, 'r') as f:
            json_data = json.load(f)
        return json_data
    except ValueError, e:
        print('Invalid JSON: {}'.format(abspath))
        sys.exit()


def load_training_json(json_data):
    rslt = {}
    for item in json_data:
        rslt[item['title']] = {'jars': item['JARS'], 'ub': item['precision_upper'], 'lb': item['precision_lower']}
    return rslt


def write_featurelogic(dooppath, json_data):
    logicpath = check_path(os.path.join(dooppath, 'logic/features.logic'))
    with open(logicpath, 'w') as f:
        for k in json_data.keys():
            f.write('{}\n'.format(json_data[k]['Rule']))
    declpath = check_path(os.path.join(dooppath, 'logic/features-decl.logic'))
    with open(declpath, 'w') as f:
        for k in json_data.keys():
            f.write('{}\n'.format(json_data[k]['Def']))


def analyze_with_each_atom(pgm, featuresjson, dooppath, analysis, depth, learnedF):
    perfs = []
    for k in featuresjson.keys():
        featurename = 'Feature' + k
        perfs.append(runF(dooppath, pgm, [[featurename]], analysis, depth, learnedF))
    return perfs


def formula_to_logic(formula, prefix):
    rst = ''
    for conj in formula:
        rst += prefix + '<-MethodSignatureRef(?meth),'
        for atom in conj:
            rst += atom + '(?meth),'
        rst = rst[:-1]
        rst += '.'
    return rst


def runF(dooppath, pgm, formula, analysis, depth, learnedF):
    analyses = {'sobj': 's-012-object-sensitive+heap', 'obj': '012-object-sensitive+heap',
                'type': '012-type-sensitive+heap', 'call': '012-call-site-sensitive_heap'}
    cmd = ''
    output = ''
    time = 0.0
    num_of_proven = 0
    proven_queries = []

    if depth == '12':
        select = formula_to_logic(formula, 'Select2(?meth)') + 'Select1(?meth)<-MethodSignatureRef(?meth).'
        cmd = 'run -jre1.6 -select \"' + select + '\" ' + analyses[analysis] + ' ' + pgm
    else:
        select = formula_to_logic(formula, 'Select1(?meth)')
        cmd = 'run -jre1.6 -select \"' + select + learnedF + '\" ' + analyses[analysis] + ' ' + pgm

    empty_cache_cmd = 'rm -rf ' + dooppath + r'/cache/*'
    tmp = os.system(empty_cache_cmd)
    if not (tmp == 0):
        print('Failed to {}/cache'.format(dooppath))
        sys.exit()

    print('Run Doop: {}'.format(cmd))
    output = subprocess.check_output(dooppath + '/' + cmd + '; exit 0;', stderr=subprocess.STDOUT, shell=True)

    # print (output)
    time_pattern = re.compile(r'analysis time\s+([0-9,\.]+)s')
    m = time_pattern.search(output)
    if m:
        time = float(m.group(1))
    else:
        print('Time string doesn\'t exist: {}'.format(output))
        sys.exit()

    proven_query_cmd = r'bloxbatch -db ' + dooppath + '/' + r'last-analysis -query "Stats:Simple:ProvenCast"'
    output = subprocess.check_output(proven_query_cmd + '; exit 0;', stderr=subprocess.STDOUT, shell=True)
    proven_queries = output.splitlines()
    for idx in range(len(proven_queries)):
        proven_queries[idx] = proven_queries[idx].strip()

    print('TIME: {}'.format(time))
    print('PROVENS: {}'.format(len(proven_queries)))

    return {'formula': formula, 'time': time, 'num_of_proven': len(proven_queries), 'proven_queries': proven_queries}


def print_perf_tbl(perf_tbl):
    data = []
    for pgm in perf_tbl.keys():
        perfs = perf_tbl[pgm]
        for perf in perfs:
            row = [pgm]
            row.append(perf['time'])
            row.append(perf['num_of_proven'])
            tmp = perf['proven_queries']
            row.append(len(tmp))
            row.append(formula_to_string(perf['formula']))
            data.append(row)
    print
    tabulate(data, headers=['Program', 'Time', 'Number of proven', 'Actual number of proven', 'Formula'])


def print_full_prec_tbl(tbl):
    data = []
    for pgm in tbl.keys():
        row = [pgm, len(tbl[pgm]['proven_queries'])]
        data.append(row)
    print
    tabulate(data, headers=['Program', 'Actual number of proven'])


def print_training_set(trainingset):
    data = []
    for pgm in trainingset.keys():
        row = [pgm]
        benchmark = trainingset[pgm]
        row.append(benchmark['ub'])
        row.append(benchmark['lb'])
        row.append(benchmark['jars'])
        data.append(row)
    print
    tabulate(data, headers=['Benchmark', 'Precision upper bound', 'Precision lower bound', 'JARS'])


def formula_to_string(formula):
    s = []
    for conj in formula:
        conj_str = '/\\'.join(conj)
        s.append('(' + conj_str + ')')
    return ('\\/'.join(s))


def convert(featurestr):
    rst = ''
    idx = int(featurestr[7:])
    if not (idx % 2 == 0):
        rst = ('!Feature{}'.format(idx / 2 + 1))
    else:
        rst = ('Feature{}'.format(idx / 2 + 1))
    return rst


def formula_to_string2(formula):
    s = []
    for conj in formula:
        for idx in range(len(conj)):
            conj[idx] = convert(conj[idx])
        conj_str = '/\\'.join(conj)
        s.append('(' + conj_str + ')')
    return ('\\/'.join(s))


def search_perfs(perfs, formula):
    rst = {}
    for perf in perfs:
        if cmp(formula, perf['formula']) == 0:
            rst = perf
            break;
    if 'formula' in rst:
        return rst
    else:
        print('search_perfs failed: {}'.format(formula))
        return None


def check_cached_file(pgm, cachefilepath, featurejson):
    if not (os.path.isfile(cachefilepath)):
        return False
    with open(cachefilepath, 'rb') as f:
        try:
            perfs = pickle.load(f)
        except EOFError:
            print('check_cached_file experienced an error while loading a pickle file.')
            return False
    # check length
    # TODO: check contents
    if not (len(perfs) == len(featurejson)):
        return False
    else:
        return True


def remove_hopeless_atoms(featurejson, perf_tbl, trainingset):
    sum_of_prec_lowers = 0
    for pgm in trainingset.keys():
        sum_of_prec_lowers += trainingset[pgm]['lb']
    for k in featurejson.keys():
        atom = 'Feature' + k
        sum_of_prec = 0
        for pgm in trainingset.keys():
            perfs = perf_tbl[pgm]
            perf = search_perfs(perfs, [[atom]])
            sum_of_prec += perf['num_of_proven']
        if sum_of_prec <= sum_of_prec_lowers:
            del featurejson[k]
            for pgm in trainingset.keys():
                perfs = perf_tbl[pgm]
                perf = search_perfs(perfs, [[atom]])
                perfs.remove(perf)
    return (featurejson, perf_tbl)


def need_further_refinement(doop_result, trainingset, bestcost, gamma):
    sum_of_num_of_proven = 0
    sum_of_cost = 0
    sum_of_target = 0
    for pgm in trainingset.keys():
        sum_of_num_of_proven += doop_result[pgm]['num_of_proven']
        sum_of_target += trainingset[pgm]['ub']
        sum_of_cost += doop_result[pgm]['time']
    print('Current precision(# of proven queires): {}'.format(sum_of_num_of_proven))
    print('Target precision(# of proven queires): {}'.format(sum_of_target))
    error = len(doop_result) / 1
    is_true_cost = True
    for pgm in doop_result.keys():
        if doop_result[pgm]['time'] == -1:
            is_true_cost = is_true_cost and False
    if is_true_cost:
        if bestcost == -4:
            bestcost = sys.maxint
        print('Current sum of cost: {}'.format(sum_of_cost))
        print('Current best sum of cost: {}'.format(bestcost))
        print('Error: {}'.format(error))
        return ((sum_of_cost + error <= bestcost) or (sum_of_cost - error <= bestcost)) and (
                sum_of_num_of_proven > float(sum_of_target) / 100 * gamma)
    else:
        print('Current cost is not evaluated one')
        return (sum_of_num_of_proven > float(sum_of_target) / 100 * gamma)


def choose_atom(perf_tbl, conj, full_prec_map, featurejson):
    best_atom = None
    best_cost = sys.maxint
    best_proven = -1
    # Proven queries of the conj for each program
    # provens(conj) : program -> PowerSet(Q)
    provens_conj = {}
    for pgm in perf_tbl.keys():
        perfs = perf_tbl[pgm]
        perf = search_perfs(perfs, [conj])
        provens_conj[pgm] = perf['proven_queries']

    for k in featurejson.keys():
        atom = 'Feature' + k
        # Proven queries of atomic feature for each program
        # proven(atom) : program -> PowerSet(Q)
        intersect = 0
        cost = 0
        for pgm in perf_tbl.keys():
            perfs = perf_tbl[pgm]
            perf = search_perfs(perfs, [[atom]])
            cost += perf['time']

            provens_full_prec_set = set(full_prec_map[pgm]['proven_queries'])
            provens_conj_set = set(provens_conj[pgm])
            conj_and_full = provens_conj_set.intersection(provens_full_prec_set)
            provens_atom = set(perf['proven_queries'])
            atom_and_full = provens_atom.intersection(provens_full_prec_set)
            intersect += len(conj_and_full.intersection(atom_and_full))

        if ((intersect > best_proven) and not (atom in conj)):
            best_atom = atom
            best_cost = cost
            best_proven = intersect
        elif ((intersect == best_proven) and not (atom in conj) and (best_cost > cost)):
            best_atom = atom
            best_cost = cost
        else:
            # do nothing
            continue
    return best_atom


def refine_conj(perf_tbl, conj, formula, full_prec_tbl, featurejson):
    # Auxiliary functions
    def check_subset(conj1, conj2):
        set1 = set(conj1)
        set2 = set(conj2)
        size1 = len(set1.intersection(set2))
        size2 = len(set2)
        if size1 == size2:
            return True  # conj2 is weaker than conj1
        else:
            return False  # conj1 is wwaker than conj2

    def check_atomic_power(conj, atom, perf_tbl, full_prec_tbl):
        is_enough = True
        for pgm in perf_tbl.keys():
            if is_enough == False:
                continue

            perfs = perf_tbl[pgm]
            perf_conj = search_perfs(perfs, [conj])
            prec_conj = set(perf_conj['proven_queries'])

            perf_atom = search_perfs(perfs, [[atom]])
            prec_atom = set(perf_atom['proven_queries'])

            prec_full = set(full_prec_tbl[pgm]['proven_queries'])

            full_intersect_conj = prec_full.intersection(prec_conj)
            full_intersect_atom = prec_full.intersection(prec_atom)

            is_enough = full_intersect_conj.issubset(full_intersect_atom)
        return is_enough

    atom = choose_atom(perf_tbl, conj, full_prec_tbl, featurejson)
    if atom == None:
        return (conj, 'SAME')
    else:
        new_conj = list(conj)
        new_conj.append(atom)
        # Subset checking 1
        # If new_conj is definitely stronger than any conjunctions except conj in formula - conj,
        # return (conj, 'SUBSET')
        tmp_formula = list(formula)
        isSubset = False
        tmp_formula.remove(conj)
        for c in tmp_formula:
            if isSubset == True:
                continue
            else:
                isSubset = check_subset(new_conj, c)
        if isSubset == True:
            print('{} is subset of current formula'.format(formula_to_string([new_conj])))
            return (conj, 'SUBSET')
        else:
            # Subset checking 2
            # If the chosen atomic feature covers full_prec better than conj,
            # we don't need to compare new_conj's precision with the precision criteria
            if check_atomic_power(conj, atom, perf_tbl, full_prec_tbl):
                return (new_conj, 'NEW_SURE')
            else:
                return (new_conj, 'NEW_NOTSURE')
        return (conj, 'SAME')


def choose_conj(formula, worklist, perf_tbl):
    worst_cost = 0
    best_conj = []
    for conj in formula:
        if not (conj in worklist):
            continue
        sum_of_cost = 0
        for pgm in perf_tbl.keys():
            perf = search_perfs(perf_tbl[pgm], [conj])
            sum_of_cost += perf['time']
        if sum_of_cost > worst_cost:
            best_conj = conj
            worst_cost = sum_of_cost
    return best_conj


def refine(dooppath, analysis, depth, learnedF, featurejson, perf_tbl, trainingset, full_prec_tbl, gamma):
    formula = []
    worklist = []
    bestcost = sys.maxint
    # formula = Feature0 \/ Feature1 \/ Feature2 ...
    # worklist = formula
    for k in featurejson.keys():
        formula.append(['Feature' + k])
        worklist.append(['Feature' + k])

    # Here we go...
    cnt = 0
    while len(worklist) is not 0:
        cnt += 1
        print('==Iteration {}=='.format(cnt))
        print('Current formula: {}'.format(formula_to_string(formula)))
        print('Current worklist: {}'.format(formula_to_string(worklist)))

        # Remove useless conjunctions from current formula, worklist and performance table
        print('Lengh of formula BEFORE pruning: {}'.format(len(formula)))
        print('Lengh of worklist BEFORE pruning: {}'.format(len(worklist)))
        sum_of_prec_lowers = 0
        conjs_to_remove = []
        for pgm in trainingset.keys():
            sum_of_prec_lowers += trainingset[pgm]['lb']
        for conj in formula:
            sum_of_prec = 0
            for pgm in trainingset.keys():
                perfs = perf_tbl[pgm]
                perf = search_perfs(perfs, [conj])
                sum_of_prec += perf['num_of_proven']
            if sum_of_prec <= sum_of_prec_lowers:
                conjs_to_remove.append(conj)
        print('Useless {} conjunctions in formula: {}'.format(len(conjs_to_remove), conjs_to_remove))
        for useless in conjs_to_remove:
            formula.remove(useless)
            worklist.remove(useless)

        # Cleanup performajce table
        useless_perf_cnt = 0
        for pgm in perf_tbl.keys():
            perfs = perf_tbl[pgm]
            for useless in conjs_to_remove:
                useless_perf_cnt += 1
                useless_perf = search_perfs(perfs, [useless])
                perfs.remove(useless_perf)

        print('Number of removed performance entry: {}'.format(useless_perf_cnt))
        print('Lengh of formula AFTER pruning: {}'.format(len(formula)))
        print('Lengh of worklist AFTER pruning: {}'.format(len(worklist)))
        print('')

        print('[[Performance table]]')
        print_perf_tbl(perf_tbl)
        print('')

        # Update performance table
        print('Updating performance table...'),
        for pgm in trainingset.keys():
            tbl = perf_tbl[pgm]
            for idx in range(len(tbl)):
                if tbl[idx]['time'] == -1:
                    print('{} need to be evaluated...'.format(formula_to_string(tbl[idx]['formula'])))
                    tbl[idx] = runF(dooppath, trainingset[pgm]['jars'][0], tbl[idx]['formula'], analysis, depth,
                                    learnedF)
        print('Done')
        print('')
        # Select a conjunction to refine
        conj = choose_conj(formula, worklist, perf_tbl)
        print('{} will be refined...'.format(formula_to_string([conj])))

        # Refine the conjunction confidence = 'SAME' | 'NEW_SURE' | 'NEW_NOTSURE' | 'SUBSET'
        (conj_new, confidence) = refine_conj(perf_tbl, conj, formula, full_prec_tbl, featurejson)

        # Check for precision and update formula and worklist accordingly.
        if confidence == 'SUBSET':
            print('Refined conj is a subset of the other conj in formula.')
            formula.remove(conj)
            worklist.remove(conj)
        elif confidence == 'SAME':
            print('Refined conj is same with the original conj.')
            formula.remove(conj)
            worklist.remove(conj)
        elif confidence == 'NEW_SURE':
            print('Newly added atom is powerful enough to make the refined conj pass the evaluation phase');
            print('{} becomes {}'.format(formula_to_string([conj]), formula_to_string([conj_new])))
            formula.remove(conj)
            formula.append(conj_new)

            # Pretending Doop is run with the new formula
            doop_result = {}
            for pgm in full_prec_tbl.keys():
                full_prec = full_prec_tbl[pgm]
                doop_result[pgm] = {'formula': formula, 'time': -1, 'num_of_proven': len(full_prec['proven_queries']),
                                    'proven_queries': full_prec['proven_queries']}

            # precision > gamma ?
            if need_further_refinement(doop_result, trainingset, bestcost, gamma):
                print('Enough precision')
                # Update perf_tbl
                for pgm in perf_tbl.keys():
                    perf_tbl[pgm].append(
                        {'formula': [conj_new], 'time': -1, 'num_of_proven': 5400, 'proven_queries': []})
                # Update worklist
                worklist.remove(conj)
                worklist.append(conj_new)
            else:
                print('Not possible. NEW_SURE')
                sys.exit()
        else:  # NEW_NOTSURE
            print("Newly added atom seems not powerful enough. So conj will be checked again...");
            print('{} will become {}'.format(formula_to_string([conj]), formula_to_string([conj_new])))
            # Update formula
            formula.remove(conj)
            formula.append(conj_new)
            doop_result = {}
            for pgm in perf_tbl.keys():
                doop_result[pgm] = runF(dooppath, trainingset[pgm]['jars'][0], formula, analysis, depth, learnedF)
            if need_further_refinement(doop_result, trainingset, bestcost, gamma):
                print('Enough precision')
                # Update perf_tbl
                for pgm in perf_tbl.keys():
                    # 5400 is an arbitary big number.
                    perf_tbl[pgm].append(
                        {'formula': [conj_new], 'time': -1, 'num_of_proven': 5400, 'proven_queries': []})
                # Update worklist
                worklist.remove(conj)
                worklist.append(conj_new)
                # Update bestcost
                new_bestcost = 0
                for pgm in doop_result.keys():
                    new_bestcost += doop_result[pgm]['time']
                bestcost = new_bestcost
            else:
                print('Not enough precision')
                print('Old: {}'.format(formula_to_string(worklist)))
                # Update worklist
                worklist.remove(conj)
                print('New: {}'.format(formula_to_string(worklist)))
                # Restore formula
                formula.remove(conj_new)
                formula.append(conj)

        print('')
    return formula


def learn(use_cached_perf_tbl, use_cached_full_prec, gamma, analysis, depth, learnedF, trainingpath, trainingjson,
          featurejson, dooppath):
    trainingset = {}
    perf_tbl = {}
    full_prec_tbl = {}

    ''' Loading training targets' info '''
    print('Loading training targets\' info'),
    trainingset = load_training_json(trainingjson)
    print('Done')
    print('')

    ''' Loading features into Doop '''
    print('Applying features to Doop...'),
    write_featurelogic(dooppath, featurejson)
    print('Done')
    print('')

    ''' Building and archiving perf_tbl '''
    print('Building and archiving performance table of each atomic feature...')
    if use_cached_perf_tbl:
        for pgm in trainingset.keys():
            print('Loading {}...'.format(pgm))
            cachefilepath = os.path.abspath(os.path.join(trainingpath, pgm + '_perf_tbl.pickle'))
            if check_cached_file(pgm, cachefilepath, featurejson):
                with open(cachefilepath, 'rb') as f:
                    perf_tbl[pgm] = pickle.load(f)
            else:
                print('Cached file {} is invalid. Reloading...'.format(cachefilepath))
                perfs = analyze_with_each_atom(trainingset[pgm]['jars'][0], featurejson, dooppath, analysis, depth,
                                               learnedF)
                perf_tbl[pgm] = perfs
                with open(cachefilepath, 'wb') as f:
                    pickle.dump(perfs, f)
                print('{}_perf_tbl.pickle is written.'.format(pgm))
        print_perf_tbl(perf_tbl)
    else:
        print('Loading performance table takes some times...')
        for pgm in trainingset.keys():
            print('Loading {}...'.format(pgm))
            perfs = analyze_with_each_atom(trainingset[pgm]['jars'][0], featurejson, dooppath, analysis, depth,
                                           learnedF)
            cachefilepath = os.path.abspath(os.path.join(trainingpath, pgm + '_perf_tbl.pickle'))
            with open(cachefilepath, 'wb') as f:
                pickle.dump(perfs, f)
            print('{}_perf_tbl.pickle is written.'.format(pgm))
            perf_tbl[pgm] = perfs
    print('Done')
    print('')

    ''' Remove hopeless atomic features from feature set '''
    print('Number of atomic features before cleanup: {}'.format(len(featurejson)))
    (featurejson, perf_tbl) = remove_hopeless_atoms(featurejson, perf_tbl, trainingset)
    print('Number of atomic features after cleanup: {}'.format(len(featurejson)))

    '''
    Loading maximum provable queries of training targets
    '''
    print('Building and archiving maximum provable queries of training targets...')
    if use_cached_full_prec:
        for pgm in trainingset.keys():
            cachefilepath = os.path.abspath(os.path.join(trainingpath, pgm + '_full_prec.pickle'))
            if os.path.isfile(cachefilepath):
                with open(cachefilepath, 'rb') as f:
                    full_prec_tbl[pgm] = pickle.load(f)
            else:
                tbl = runF(dooppath, trainingset[pgm]['jars'][0], [['All']], analysis, depth, learnedF)
                del tbl['time']
                del tbl['num_of_proven']
                cachefilepath = os.path.abspath(os.path.join(trainingpath, pgm + '_full_prec.pickle'))
                with open(cachefilepath, 'wb') as f:
                    pickle.dump(tbl, f)
                print('{}_full_prec.pickle is written.'.format(pgm))
                full_prec_tbl[pgm] = tbl
        print_full_prec_tbl(full_prec_tbl)
    else:
        print('Loading full precision table takes some times...'),
        for pgm in trainingset.keys():
            tbl = runF(dooppath, trainingset[pgm]['jars'][0], [['All']], analysis, depth, learnedF)
            del tbl['time']
            del tbl['num_of_proven']
            cachefilepath = os.path.abspath(os.path.join(trainingpath, pgm + '_full_prec.pickle'))
            with open(cachefilepath, 'wb') as f:
                pickle.dump(tbl, f)
            print('{}_full_prec.pickle is written.'.format(pgm))
            full_prec_tbl[pgm] = tbl
    print('Done')
    print('')

    '''
    Main refinement process
    '''
    print('Main learning process begins...')
    formula = refine(dooppath, analysis, depth, learnedF, featurejson, perf_tbl, trainingset, full_prec_tbl, gamma)

    return formula


def write_select_logic(f2, f1, dooppath):
    select_logic_path = os.path.abspath(os.path.join(dooppath, 'logic', 'select.logic'))
    with open(select_logic_path, 'w') as f:
        f.write(formula_to_logic(f2, 'Select2(?meth)') + '\n')
        f.write(formula_to_logic(f1, 'Select1(?meth)') + '\n')


training_pgms = [{'title': 'luindex', 'JARS': ['$DOOP_HOME/jars/dacapo/luindex.jar']},
                 {'title': 'lusearch', 'JARS': ['$DOOP_HOME/jars/dacapo/lusearch.jar']},
                 {'title': 'antlr', 'JARS': ['$DOOP_HOME/jars/dacapo/antlr.jar']},
                 {'title': 'pmd', 'JARS': ['$DOOP_HOME/jars/dacapo/pmd.jar']}]
gammas = {'sobj': 93, 'obj': 92, 'type': 88}
analysis_full_name = {'sobj': 'selective-2-object-sensitive+heap+Data', 'obj': '2-object-sensitive+heap+Data',
                      'type': '2-type-sensitive+heap+Data'}

''' Functions for feature extraction '''


def extract_features():
    ROOT = sys.path[0]

    DOOP_DIR = os.path.join(ROOT, 'Data-Driven-Doop')
    DOOP_METHODS_DIR = os.path.join(DOOP_DIR, 'methods')
    JARS_DIR = os.path.join(DOOP_DIR, 'jars/dacapo')
    FEATURETXT = os.path.join(ROOT, 'features/SignatureRanking.txt')

    def remove_values_from_list(the_list, val):
        return [value for value in the_list if value != val]

    def getKey(item):
        return item[0]

    words = list();
    s2 = list();
    count_words = list();
    lines = set();
    cmd = DOOP_DIR + '/run -jre1.6 context-insensitive '
    cmd += os.path.join(JARS_DIR, 'luindex.jar')
    print(cmd)
    os.system(cmd)
    methodDeclFacts = os.path.join(DOOP_METHODS_DIR, 'MethodDeclaration.facts')
    pre_lines = [line.rstrip('\n') for line in open(methodDeclFacts)]
    luindexSize = len(pre_lines)

    for i, val in enumerate(pre_lines):
        declaration = val.split('\t')
        lines.add(declaration[1])

    for i, val in enumerate(lines):
        content = val
        content = content.replace("<", "")
        content = content.replace(">", "")
        content = content.replace("$", ".")
        content = content.replace(",", ".")
        content = content.replace("()", "({}")
        content = content.replace("(", ".")
        content = content.replace(")", ".")
        content = content.replace(": ", ".")
        content = content.replace(" ", ".")
        words.extend(content.split("."))
    words = remove_values_from_list(words, "")

    while len(words) > 0:
        keyword = words.pop(0)
        counter = words.count(keyword)
        count_words.append([counter, keyword])
        words = remove_values_from_list(words, keyword)

    feature = {}

    featureLogic = open("features/features.logic", 'w')
    featureDecl = open("features/features-decl.logic", 'w')
    jsonPath = open("features/features.json", 'w')

    sortedList = sorted(count_words, key=getKey)
    sortedList.reverse()

    txt = open(FEATURETXT, 'w')
    for i, val in enumerate(sortedList):
        #  print(val[1])
        if i >= 10:
            break;
        event = {}
        if val[1] == "{}":
            val[1] = "()"

        print(str(i) + ": " + val[1] + " " + str(val[0]))
        txt.write(str(i) + ": " + val[1] + " " + str(val[0]) + "\n")

        featureDef = "Feature" + str(2 * i) + "(?meth)" + "->" + "MethodSignatureRef(?meth)."
        featureRule = "Feature" + str(2 * i) + "(?meth)" + "<-"
        featureRule += "string:like(?meth,\"%" + val[1] + "%\"),"
        featureRule += "MethodSignatureRef(?meth)."
        event['Def'] = featureDef
        event['Rule'] = featureRule
        feature[2 * i] = event
        event = {}
        featureDef = "Feature" + str(2 * i + 1) + "(?meth)" + "->" + "MethodSignatureRef(?meth)."
        featureRule = "Feature" + str(2 * i + 1) + "(?meth)" + "<-"
        featureRule += "string:notlike(?meth,\"%" + val[1] + "%\"),"
        featureRule += "MethodSignatureRef(?meth)."
        event['Def'] = featureDef
        event['Rule'] = featureRule
        feature[2 * i + 1] = event

        data = "Feature" + str(2 * i) + "(?meth)" + "->" + "MethodSignatureRef(?meth).\n"
        featureDecl.write(data)
        data = "Feature" + str(2 * i) + "(?meth)" + "<-"
        data += "string:like(?sig,\"%" + val[1] + "%\"),"
        data += "MethodSignatureRef(?meth)." + '\n'
        featureLogic.write(data)

        data = "Feature" + str(2 * i + 1) + "(?meth)" + "->" + "MethodSignatureRef(?meth).\n"
        featureDecl.write(data)
        data = "Feature" + str(2 * i + 1) + "(?meth)" + "<-"
        data += "string:notlike(?sig,\"%" + val[1] + "%\"),"
        data += "MethodSignatureRef(?meth)." + '\n'
        featureLogic.write(data)

    k = 0
    binding1 = {}
    binding2 = {}

    pre_lines = [line.rstrip('\n') for line in open('features/bodyfeatures.logic')]
    for i1, val in enumerate(pre_lines):
        binding1[k] = val
        featureLogic.write(val + "\n")
        k = k + 1
    k = 0
    pre_lines = [line.rstrip('\n') for line in open('features/bodyfeatures-decl.logic')]
    for i1, val in enumerate(pre_lines):
        binding2[k] = val
        featureDecl.write(val + "\n")
        k = k + 1

    for k in range(0, 30):
        event = {}
        event['Def'] = binding2[k]
        event['Rule'] = binding1[k]
        feature[2 * i + k] = event

    # fd.write("Select(?meth)->MethodInvocation(?meth).")
    featureLogic.close
    featureDecl.close

    a = json.dumps(feature, sort_keys=True, indent=4)
    jsonPath.write(a)

    #
    # for body feature
    #

    print("Feature extraction Done: SignatureRanking.txt")
    print("Feature extraction Done: features.logic")
    print("Feature extraction Done: features-decl.logic")
    print("Feature extraction Done: features.json")
    os.system('cat features/SignatureRanking.txt')


''' Main driver '''
if __name__ == '__main__':
    desc = 'Boolean formula learning artifact'
    parser = argparse.ArgumentParser(description=desc, formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('analysis', metavar='ANALYSIS', help='Target analysis. (sobj, obj, or type)')
    args = parser.parse_args()

    ''' Input validation '''
    if not ((args.analysis == 'sobj') or (args.analysis == 'obj') or \
            (args.analysis == 'type')):
        print('Invalid analysis: {}'.format(args.analysis))
        sys.exit()
    else:
        analysis = args.analysis
    gamma = gammas[analysis]
    dooppath = check_path('Data-Driven-Doop')

    ''' Initialize '''
    print('=====SETTING=====')
    print('Analysis: {}'.format(analysis_full_name[analysis]))
    print('Doop: {}'.format(dooppath))
    table = []
    for pgm in training_pgms:
        row = [pgm['title']]
        row.append(pgm['JARS'][0])
        table.append(row)
    print
    tabulate(table, headers=['Benchmark', 'JAR'])
    print('')

    ''' Feature extraction '''
    t0 = time.time()
    print('1. Extracting features...')
    extract_features()
    featurepath = os.path.abspath(os.path.join('./', 'features', 'features.json'))
    featurejson = check_and_load_json(featurepath)

    t1 = time.time()
    print('Elapsed time '),
    show_delta_time(t0, t1)
    print('')

    ''' Learn f2 '''
    # Initialize learning
    # Obtain upper and lower bounds
    print('2. Obtaining precision bounds...')
    # debugging_ub = {'antlr':981, 'luindex':838, 'lusearch':933, 'pmd':951}
    # debugging_lb = {'antlr':660, 'luindex':613, 'lusearch':654, 'pmd':623}
    for pgm in training_pgms:
        perf = runF(dooppath, pgm['JARS'][0], [['Empty']], analysis, '12', '')
        pgm['precision_lower'] = perf['num_of_proven']
        # pgm['precision_lower'] = debugging_lb[pgm['title']]
        perf = runF(dooppath, pgm['JARS'][0], [['All']], analysis, '12', '')
        pgm['precision_upper'] = perf['num_of_proven']
        # pgm['precision_upper'] = debugging_ub[pgm['title']]
    t2 = time.time()
    print('Elapsed time '),
    show_delta_time(t1, t2)
    print('')

    # Learn
    print('2. Learning a Boolean formula for f2')
    training_materials_path = os.path.abspath(os.path.join('./',
                                                           'training_caches',
                                                           analysis_full_name[analysis] + '_12'))
    if not (os.path.isdir(training_materials_path)):
        tmp = os.system('mkdir -p ' + training_materials_path)
        if not (tmp == 0):
            print('Failed to create folder {}.'.format(training_materials_path))
            sys.exit()
    print('Performance of atomic features will be cached in {}.'.format(training_materials_path))

    formula12 = learn(True, True, gamma, analysis, '12', '', training_materials_path, training_pgms, featurejson,
                      dooppath)
    # formula12 = [['Feature0']]
    print('Learned formula: {}'.format(formula_to_string(formula12)))
    t3 = time.time()
    print('Elapsed time '),
    show_delta_time(t2, t3)
    print('')

    ''' Learn f1 '''
    # Initialize learning
    # Obtain upper and lower bounds
    print('3. Obtaining precision bounds...')
    for pgm in training_pgms:
        # print (''),
        perf = runF(dooppath, pgm['JARS'][0], [['Empty']], analysis, '01',
                    formula_to_logic(formula12, 'Select2(?meth)'))
        pgm['precision_lower'] = perf['num_of_proven']
    t4 = time.time()
    print('Elapsed time '),
    show_delta_time(t3, t4)
    print('')

    # Learn
    print('4. Learning a Boolean formula for f1')
    training_materials_path = os.path.abspath(os.path.join('./',
                                                           'training_caches',
                                                           analysis_full_name[analysis] + '_01'))
    if not (os.path.isdir(training_materials_path)):
        tmp = os.system('mkdir -p ' + training_materials_path)
        if not (tmp == 0):
            print('Failed to create folder {}.'.format(training_materials_path))
            sys.exit()
    print('Performance of atomic features will be cached in {}.'.format(training_materials_path))

    formula01 = learn(True, True, gamma, analysis, '01', formula_to_logic(formula12, 'Select2(?meth)'),
                      training_materials_path, training_pgms, featurejson, dooppath)
    # formula01 = [['Feature49']]
    print('Learned formula: {}'.format(formula_to_string(formula01)))
    t5 = time.time()
    print('Elapsed time '),
    show_delta_time(t4, t5)
    print('')

    ''' Output select.logic '''
    print('5. Applying learned formulas')
    write_select_logic(formula12, formula01, dooppath)
    print('')

    print('FINAL RESULT')
    print('Formula for context-depth 1 or 2: {}'.format(formula_to_string2(formula12)))
    print('Formula for context-depth 0 or 1: {}'.format(formula_to_string2(formula01)))
