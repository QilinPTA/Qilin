import os
import sys
import re
import subprocess
import pickle
from tabulate import tabulate
from itertools import *

''' Functions for learning  '''


# Convert a list to a dictionary
# list {'title', 'jar', ..., 'cast_proven_queries'} -> dict['title'] = {'jar', ...}
def load_training_json(json_data):
    rslt = {}
    for item in json_data:
        rslt[item['title']] = {'jar': item['jar'],
                               'formula': item['formula'],
                               'cast_empty': item['cast_precision_empty'],
                               'cast_proven_queries': item['cast_proven_queries'],
                               'cast_unproven_queries': item['cast_unproven_queries'],
                               'cast_empty_cost': item['cast_cost_empty'],
                               'cast_chosen_methods': item['cast_chosen_methods'],
                               'cast_org_cost': item['cast_cost_org']}
    return rslt


def analyze_with_each_atom(pgm, featuresjson, dooppath, analysis, trainingpath, tunneling, tmformula):
    perfs = []
    # run insensitive analysis for 1) precision and 2) profiling each atom's method selections.
    # obtained precision will be used to fill timeout-ed atomic feature's precision
    insens = runF(dooppath, pgm, [[]], 'insens', '', [[]])
    insens_chosen_methods = {}
    for k in featuresjson.keys():
        cast_chosen_methods_cmd = r'bloxbatch -db ' + dooppath + '/' + r'last-analysis -query "_(?meth)<-Feature' + k + r'(?meth)."'
        output = subprocess.check_output(cast_chosen_methods_cmd + '; exit 0;', stderr=subprocess.STDOUT, shell=True)
        insens_chosen_methods['Feature' + k] = cast_chosen_methods = [method.strip() for method in output.splitlines()]

    for k in featuresjson.keys():
        perf = {}
        featurecache_filename = os.path.join(trainingpath,
                                             os.path.basename(pgm) + '_' + tunneling + '_' + k + '.pickle')
        if os.path.isfile(featurecache_filename):
            print('Feature{} is reloaded.'.format(k))
            with open(featurecache_filename, 'rb') as f:
                try:
                    perf = pickle.load(f)
                except EOFError:
                    print('{} is invalid'.format(featurecache_filename))
                    featurename = 'Feature' + k
                    perf = runF(dooppath, pgm, [[featurename]], analysis, tunneling, tmformula)
                    if len(perf['cast_proven_queries']) == 0:
                        print('Timeout happened during analyzing {}.'.format(featurename))
                        # timeout
                        print('Proven queries BEFORE: {}'.format(len(perf['cast_proven_queries'])))
                        perf['cast_proven_queries'].extend(insens['cast_proven_queries'])
                        print('Proven queries AFTER: {}'.format(len(perf['cast_proven_queries'])))
                        perf['cast_unproven_queries'].extend(insens['cast_unproven_queries'])
                        perf['cast_chosen_methods'].extend(insens_chosen_methods[featurename])
                        print('Chosen methods: {}'.format(len(perf['cast_chosen_methods'])))
            with open(featurecache_filename, 'wb') as f:
                pickle.dump(perf, f)
                print('{} is written.'.format(featurecache_filename))
        else:
            featurename = 'Feature' + k
            perf = runF(dooppath, pgm, [[featurename]], analysis, tunneling, tmformula)
            if len(perf['cast_proven_queries']) == 0:
                print('Timeout happened during analyzing {}.'.format(featurename))
                # timeout
                print('Proven queries BEFORE: {}'.format(len(perf['cast_proven_queries'])))
                perf['cast_proven_queries'].extend(insens['cast_proven_queries'])
                print('Proven queries AFTER: {}'.format(len(perf['cast_proven_queries'])))
                perf['cast_unproven_queries'].extend(insens['cast_unproven_queries'])
                perf['cast_chosen_methods'].extend(insens_chosen_methods[featurename])
                print('Chosen methods: {}'.format(len(perf['cast_chosen_methods'])))
            with open(featurecache_filename, 'wb') as f:
                pickle.dump(perf, f)
                print('{} is written.'.format(featurecache_filename))

        perfs.append(perf)
    print(len(perfs))
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


def runF(dooppath, pgm, formula, analysis, tunneling, tmformula):
    analyses = {'obj': '1-tunneled-object-sensitive+heap+Learn', 'insens': 'context-insensitive',
                'typ': '1-tunneled-type-sensitive+heap+Learn', 'cs': '1-tunneled-call-site-sensitive+heap+Learn',
                'sobj': 'selective-1-tunneled-object-sensitive+heap+Learn',
                'osobj': 'selective-1-object-sensitive+heap', 'oobj': '1-object-sensitive+heap',
                'otyp': '1-type-sensitive+heap', 'ocs': '1-call-site-sensitive+heap'}
    tunneling_str = {'t': 'Tunneling(?meth)', 'tm': 'TunnelingM(?meth)'}
    cmd = ''
    output = ''
    time = 0.0
    num_of_proven = 0
    cast_proven_queries = []
    cast_unproven_queries = []
    cast_chosen_methods = []
    cwd = os.getcwd()

    if analysis == 'insens' or analysis == 'oobj' or analysis == 'otyp' or analysis == 'ocs' or analysis == 'osobj':
        cmd = 'run -jre1.6 ' + analyses[analysis] + ' ' + pgm
    else:
        if tunneling == 't':
            select = formula_to_logic(formula, tunneling_str[tunneling])
        else:
            select = formula_to_logic(formula, tunneling_str[tunneling]) + formula_to_logic(tmformula,
                                                                                            'Tunneling(?meth)')

        cmd = 'run -jre1.6 -timeout 200 -select \"' + select + '\" ' + analyses[analysis] + ' ' + pgm

    # empty_cache_cmd = 'rm -rf ' + dooppath + r'/cache/*'
    empty_cache_cmd = 'rm -rf ' + dooppath + r'/cache/analysis'
    tmp = os.system(empty_cache_cmd)
    if not (tmp == 0):
        print('Failed to {}/cache'.format(dooppath))
        sys.exit()

    print('Run Doop: {}'.format(cmd))
    os.chdir(dooppath)
    output = subprocess.check_output(dooppath + '/' + cmd + '; exit 0;', stderr=subprocess.STDOUT, shell=True)
    os.chdir(cwd)

    timeout_pattern = re.compile(r'Timeout after\s+([0-9,\.]+)s')
    m = timeout_pattern.search(output)
    if m:
        print('Timeout after {} seconds.'.format(m.group(1)))
        return {'formula': formula, 'time': float(m.group(1)),
                'cast_proven_queries': [], 'cast_unproven_queries': [], 'cast_chosen_methods': [],
                'cast_num_of_proven': 0}

    time_pattern = re.compile(r'elapsed time\s+([0-9,\.]+)')
    m = time_pattern.search(output)
    if m:
        time = float(m.group(1))
    else:
        print('Time string doesn\'t exist: {}'.format(output))
        sys.exit()

    cast_proven_query_cmd = r'bloxbatch -db ' + dooppath + '/' + r'last-analysis -query "Stats:Simple:ProvenCast"'
    output = subprocess.check_output(cast_proven_query_cmd + '; exit 0;', stderr=subprocess.STDOUT, shell=True)
    cast_proven_queries = [cast_proven_query.strip() for cast_proven_query in output.splitlines()]

    cast_unproven_query_cmd = r'bloxbatch -db ' + dooppath + '/' + r'last-analysis -query "Stats:Simple:PotentiallyFailingCast"'
    output = subprocess.check_output(cast_unproven_query_cmd + '; exit 0;', stderr=subprocess.STDOUT, shell=True)
    cast_unproven_queries = [cast_unproven_query.strip() for cast_unproven_query in output.splitlines()]

    if analysis != 'insens' and analysis != 'oobj' and analysis != 'otyp' and analysis != 'ocs' and analysis != 'osobj':
        cast_chosen_methods_cmd = r'bloxbatch -db ' + dooppath + '/' + r'last-analysis -query "_(?meth)<-' + \
                                  tunneling_str[tunneling] + r'."'
        output = subprocess.check_output(cast_chosen_methods_cmd + '; exit 0;', stderr=subprocess.STDOUT, shell=True)
        cast_chosen_methods = [cast_chosen_method.strip() for cast_chosen_method in output.splitlines()]

    print('TIME: {}'.format(time))
    print('CASTING PROVENS: {}'.format(len(cast_proven_queries)))
    print('CASTING UNPROVENS: {}'.format(len(cast_unproven_queries)))
    if analysis != 'insens':
        print('CHOSEN METHODS: {}'.format(len(cast_chosen_methods)))

    return {'formula': formula, 'time': time,
            'cast_num_of_proven': len(cast_proven_queries), 'cast_proven_queries': cast_proven_queries,
            'cast_unproven_queries': cast_unproven_queries, 'cast_chosen_methods': cast_chosen_methods}


def print_perf_tbl(perf_tbl):
    data = []
    for pgm in perf_tbl.keys():
        perfs = perf_tbl[pgm]
        for perf in perfs:
            row = [pgm]
            row.append(perf['time'])
            row.append(len(perf['cast_unproven_queries']))
            row.append(formula_to_string(perf['formula']))
            if 'difference' in perf.keys():
                row.append(str(len(perf['difference'])))
            else:
                row.append('0')
            data.append(row)
    print
    tabulate(data, headers=['Program', 'Time', 'Unprovens (Casting)', 'Formula', 'Difference'])


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
            if conj[idx] == 'Empty':
                continue
            conj[idx] = convert(conj[idx])
        conj_str = '/\\'.join(conj)
        s.append('(' + conj_str + ')')
    return ('\\/'.join(s))


def search_perfs(perfs, formula):
    rst = {}
    tmp_formula = list(formula)
    if ['Empty'] in tmp_formula:
        tmp_formula.remove(['Empty'])
    for perf in perfs:
        tmp_perf_formula = list(perf['formula'])
        if ['Empty'] in tmp_perf_formula:
            tmp_perf_formula.remove(['Empty'])
        if cmp(tmp_formula, tmp_perf_formula) == 0:
            rst = perf
            break;
    if 'formula' in rst:
        return rst
    else:
        print('search_perfs failed: {}'.format(formula))
        return None


def print_perfs_of_f(perfs_of_f, pgms):
    data = []
    idx = 1
    for perf in perfs_of_f:
        row = [idx]
        sum_of_cost = 0.0
        sum_of_prec = 0
        for pgm in pgms:
            if pgm in perf.keys():
                row.append(perf[pgm]['time'])
                row.append(perf[pgm]['prec'])
                sum_of_cost += perf[pgm]['time']
                sum_of_prec += perf[pgm]['prec']
            else:
                row.append(0.0)
                row.append(0)
                sum_of_cost += 0.0
                sum_of_prec += 0
        row.append(sum_of_cost)
        row.append(sum_of_prec)
        data.append(row)
        idx += 1
    # header
    header = ['Iter']
    for pgm in pgms:
        header.append(pgm + 'C')
        header.append(pgm + 'P')
    header.append('SumC')
    header.append('SumP')
    print
    tabulate(data, headers=header)
    return


def check_cached_file(pgm, cachefilepath, featurejson):
    if not (os.path.isfile(cachefilepath)):
        print('Cache file not exist.')
        return False
    with open(cachefilepath, 'rb') as f:
        try:
            perfs = pickle.load(f)
        except EOFError:
            print('check_cached_file experienced an error while loading a pickle file.')
            return False
    # check length
    # TODO: check contents
    if not (len(perfs) == len(featurejson)):  # +1 for initial formula
        print('Cache file isn\'t correct. perfs:{}, featurejson:{}'.format(len(perfs), len(featurejson)))
        return False
    else:
        return True


def choose_atom(perf_tbl, formula, conj, featurejson, failed_atoms):
    print('Choose atom for {}'.format(conj))
    best_atom = None
    best_intersect = -1
    best_chosen_methods = -1

    cast_chosen_methods = {}
    for pgm in perf_tbl.keys():
        perfs = perf_tbl[pgm]
        perf = search_perfs(perfs, formula + [conj])
        cast_chosen_methods[pgm] = perf['cast_chosen_methods']

    for k in featurejson.keys():
        atom = 'Feature' + k
        intersect = 0
        chosen_methods_size = 0
        for pgm in perf_tbl.keys():
            perfs = perf_tbl[pgm]
            perf = search_perfs(perfs, [[atom]])

            chosen_methods_conj = set(cast_chosen_methods[pgm])
            chosen_methods_atom = set(perf['cast_chosen_methods'])
            chosen_methods_size += len(chosen_methods_atom)
            intersect += len(chosen_methods_conj.intersection(chosen_methods_atom))

        if ((intersect > 0) and (intersect > best_intersect) and not (atom in conj) and not (atom in failed_atoms)):
            best_atom = atom
            best_intersect = intersect
            best_chosen_methods = chosen_methods_size
            print('Best atom is {} with chosen methods intersection {}.'.format(atom, intersect))
        elif ((intersect == best_intersect) and not (atom in conj) and not (atom in failed_atoms) and (
                chosen_methods_size > best_chosen_methods)):
            best_atom = atom
            best_intersect = intersect
            best_chosen_methods = chosen_methods_size
        else:
            # do nothing
            continue
    return best_atom


def refine_conj(perf_tbl, conj, failed_atoms, formula, original_prec_tbl, featurejson):
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

    atom = choose_atom(perf_tbl, formula, conj, featurejson, failed_atoms)
    if atom == None:
        return (atom, 'SAME')
    else:
        new_conj = list(conj)
        new_conj.append(atom)
        if conj[0] != new_conj[0]:
            print('Head of conj is changed.')
            sys.exit()
        # Subset checking 1
        # If new_conj is definitely stronger than any conjunctions except old_conj in formula ,
        # return (conj, 'SUBSET')
        tmp_formula = list(formula)  # since we won't manipulate a conj, deepcopy isn't necessaly.
        isSubset = False
        for c in tmp_formula:
            if isSubset == True:
                continue
            else:
                isSubset = check_subset(new_conj, c)
        if isSubset == True:
            print('{} is subset of current formula'.format(formula_to_string([new_conj])))
            return (atom, 'SUBSET')
        else:
            return (atom, 'NEW')


def refine(dooppath, analysis, featurejson, perf_tbl, trainingset, original_prec_tbl, tunneling, init_formula,
           init_worklist, trainingpath, tmformula):
    # [['Empty'], ... ]
    formula = init_formula
    # ['Feature0', 'Feature1', ... ]
    worklist = init_worklist

    iteration_history = {}

    # Here we go...
    cnt = 0
    while len(worklist) > 0:
        iteration_history[cnt] = {'formula': formula, 'worklist': worklist}
        with open(os.path.join(trainingpath, 'iteration_history.pickle'), 'w') as f:
            pickle.dump(iteration_history, f)
        failed_atoms = set([])

        # Seed features
        opt_diff = True  # False: all diff, True: sum_of_diff
        features_to_remove = []

        # len(sum of diff) > 0
        for k in worklist:
            sum_of_diff = set([])
            timeout = False
            for pgm in trainingset.keys():
                if len(formula) == 1 and formula[0][0] == 'Empty':  # formula = [['Empty']]
                    perf_f_pgm = search_perfs(perf_tbl[pgm], [[k]])
                    perf_f_pgm['difference'] = list(set(trainingset[pgm]['cast_unproven_queries']).difference(
                        set(perf_f_pgm['cast_unproven_queries'])))
                    sum_of_diff.update(set(perf_f_pgm['difference']))
                else:  # formula = [['Empty'], ['Feature0', 'Feature20', ...], ... ]
                    # Initial formula is given
                    perf_of_formula = search_perfs(perf_tbl[pgm], formula)
                    if perf_of_formula == None:
                        # First iteration only.
                        perf_of_formula = runF(dooppath, trainingset[pgm]['jar'], formula, analysis, tunneling,
                                               tmformula)
                    perf_f_pgm = search_perfs(perf_tbl[pgm], [[k]])
                    perf_of_formula_atom = runF(dooppath, trainingset[pgm]['jar'], formula + [[k]], analysis, tunneling,
                                                tmformula)
                    if len(perf_of_formula_atom['cast_unproven_queries']) == 0:
                        print('Analyzing {} with {} is not scalable.'.format(pgm, formula + [[k]]))
                        timeout = True
                        break
                    perf_f_pgm['difference'] = list(set(perf_of_formula['cast_unproven_queries']).difference(
                        set(perf_of_formula_atom['cast_unproven_queries'])))
                    perf_tbl[pgm].append(perf_of_formula_atom)
                    sum_of_diff.update(perf_f_pgm['difference'])
            if not ((timeout == False) and (len(sum_of_diff) > 0)):
                features_to_remove.append(k)
            else:
                print('Seed feature: {}'.format(k))
                print('Difference: {}'.format(len(sum_of_diff)))

        worklist = set(worklist).difference(set(features_to_remove))
        worklist = list(worklist)
        if len(worklist) == 0:
            continue

        # Subset checking
        features_to_remove = set([])
        for f1 in worklist:
            if f1 in features_to_remove:
                continue
            sum_of_diff_f1 = set([])
            for pgm in trainingset.keys():
                perf_f1_pgm = search_perfs(perf_tbl[pgm], [[f1]])
                sum_of_diff_f1.update(set(perf_f1_pgm['difference']))
            for f2 in worklist:
                if f1 == f2:
                    continue
                sum_of_diff_f2 = set([])
                for pgm in trainingset.keys():
                    perf_f2_pgm = search_perfs(perf_tbl[pgm], [[f2]])
                    sum_of_diff_f2.update(set(perf_f2_pgm['difference']))
                if sum_of_diff_f2.issubset(sum_of_diff_f1):
                    print('{} is subset of {}'.format(f2, f1))
                    features_to_remove.add(f2)
        print('BEFORE: {}'.format(worklist))
        print('FEATURES TO REMOVE: {}'.format(features_to_remove))
        worklist = list(set(worklist).difference(features_to_remove))
        worklist.sort(
            key=lambda a: sum([len(search_perfs(perf_tbl[pgm], [[a]])['difference']) for pgm in trainingset.keys()]),
            reverse=True)
        print('AFTER: {}'.format(worklist))

        conj = [worklist.pop(0)]
        conj_perf_tbl = {}
        while True:
            cnt += 1

            print('==Iteration {}=='.format(cnt))
            print('Current formula: {}'.format(formula_to_string(formula)))
            print('Current conjunction under refinement: {}'.format(conj))
            print('Current worklist: {}'.format(worklist))
            print('[[Performance table]]')
            print_perf_tbl(conj_perf_tbl)
            print('')

            (atom_new, confidence) = refine_conj(perf_tbl, conj, failed_atoms, formula, original_prec_tbl, featurejson)

            # Check for conj_new's coverage and update formula and worklist accordingly.
            if confidence == 'SAME' or confidence == 'SUBSET':
                print('Refined conj is same with the original conj. Let\'s see new formula has reasonable cost')
                sum_of_orig_cost = 0.0
                sum_of_formula_cost = 0.0
                sum_of_prec_old = 0
                sum_of_prec_new = 0
                is_all_improved = True
                is_all_valid = True
                timeouted_pgms = []
                for pgm in perf_tbl.keys():
                    perf_old_f = search_perfs(perf_tbl[pgm], formula)
                    perf_new_f = search_perfs(perf_tbl[pgm], formula + [conj])

                    sum_of_orig_cost += trainingset[pgm]['cast_org_cost']
                    sum_of_formula_cost += perf_new_f['time']
                    sum_of_prec_old += len(perf_old_f['cast_unproven_queries'])
                    sum_of_prec_new += len(perf_new_f['cast_unproven_queries'])

                    if len(perf_new_f['cast_unproven_queries']) - len(perf_old_f['cast_unproven_queries']) > 0:
                        is_all_improved = False
                    if len(perf_new_f['cast_unproven_queries']) == 0:
                        is_all_valid = False
                        timeouted_pgms.append(pgm)

                print('New formula: {}'.format(formula_to_string(formula + [conj])))
                if not (is_all_valid):
                    print('New formula timeout.')
                    print(timeouted_pgms)
                elif not (is_all_improved):
                    print('No timeout, but new formula is less precise than before.')
                    print('Old formula unprovens: {}'.format(sum_of_prec_old))
                    print('New formula unprovens: {}'.format(sum_of_prec_new))
                elif (sum_of_orig_cost) <= sum_of_formula_cost:
                    print('New formula is too costly than original analysis')
                    print('New formula cost: {}'.format(str(sum_of_formula_cost)))
                    print('Original analysis cost: {}'.format(str(sum_of_orig_cost)))
                else:
                    print('Refinement successed.')
                    formula.append(conj)
                break
            else:  # NEW
                print("New conj should be checked...");
                print('{} will become {}'.format(formula_to_string([conj]), formula_to_string([conj + [atom_new]])))
                new_formula = formula + [conj + [atom_new]]
                old_formula = formula + [conj]

                for pgm in perf_tbl.keys():
                    perf = runF(dooppath, trainingset[pgm]['jar'], new_formula, analysis, tunneling, tmformula)
                    perf_f = search_perfs(perf_tbl[pgm], formula)
                    perf['difference'] = list(
                        set(perf_f['cast_unproven_queries']).difference(set(perf['cast_unproven_queries'])))
                    # perf['difference'] = list(set(trainingset[pgm]['cast_unproven_queries']).difference(set(perf['cast_unproven_queries'])))
                    # Update perf_tbl
                    perf_tbl[pgm].append(perf)
                    if pgm not in conj_perf_tbl.keys():
                        conj_perf_tbl[pgm] = []
                    conj_perf_tbl[pgm].append(perf)

                is_all_valid = True
                is_all_improved = True
                is_all_faster = True
                is_all_equal = True
                is_all_diff = True
                zero_diff_pgms = []
                sum_of_diff = 0
                sum_of_old_cost = 0.0
                sum_of_new_cost = 0.0
                sum_of_old_prec = 0
                sum_of_new_prec = 0
                for pgm in perf_tbl.keys():
                    zero_diff_pgms = []
                    perf_new_c = search_perfs(perf_tbl[pgm], new_formula)
                    perf_old_c = search_perfs(perf_tbl[pgm], old_formula)
                    if (len(perf_new_c['cast_unproven_queries']) == 0):
                        is_all_valid = False
                    if len(perf_new_c['cast_unproven_queries']) - len(perf_old_c['cast_unproven_queries']) > 0:
                        is_all_improved = False
                    if len(perf_new_c['cast_unproven_queries']) != len(perf_old_c['cast_unproven_queries']):
                        is_all_equal = False
                    if len(perf_new_c['difference']) == 0:
                        zero_diff_pgms.append(pgm)
                        is_all_diff = False
                    if (1.1 * perf_old_c['time']) < perf_new_c['time']:
                        is_all_faster = False
                    sum_of_diff += len(perf_new_c['difference'])
                    sum_of_new_cost += perf_new_c['time']
                    sum_of_old_cost += perf_old_c['time']
                    sum_of_new_prec += len(perf_new_c['cast_unproven_queries'])
                    sum_of_old_prec += len(perf_old_c['cast_unproven_queries'])

                if (is_all_valid == False) or (sum_of_diff == 0):
                    # if (is_all_valid == False) or (is_all_diff == False):
                    # print ('Newly added atom caused timeout or difference went to zero.')
                    print('Newly added atom caused timeout or difference went to zero for one or more programs.')
                    # print ('Programs that different went zero: {}'.format(zero_diff_pgms))
                    print('Difference: {}'.format(sum_of_diff))
                    failed_atoms.add(atom_new)
                # elif is_all_improved == True and is_all_equal == False:
                elif sum_of_old_prec > sum_of_new_prec:
                    print('Precision Improved!')
                    print('Difference: {}'.format(sum_of_diff))
                    conj.append(atom_new)
                elif sum_of_old_prec == sum_of_new_prec:
                    if (1.1 * sum_of_old_cost >= sum_of_new_cost):
                        # if is_all_faster:
                        print('Cost Improved!')
                        print('Difference: {}'.format(sum_of_diff))
                        conj.append(atom_new)
                    else:
                        print('Same precision, higher cost')
                        failed_atoms.add(atom_new)
                else:
                    failed_atoms.add(atom_new)
        print('')
    return formula


def learn(use_cached_perf_tbl, analysis, trainingpath, trainingjson, featurejson, dooppath, tunneling, init_formula,
          init_worklist, tmformula):
    trainingset = {}
    perf_tbl = {}

    ''' Loading training targets' info '''
    # trainingset[TITLE] = {'jar':..., 'cast_precision_empty':...,}
    trainingset = load_training_json(trainingjson)

    ''' Loading provable queries by conventional analysis 
    (all methods are importnat) of training targets '''
    # original_prec = empty tunneling
    # original_prec_tbl[TITLE] = {'cast_proven_queries':..., 'cast_unproven_queries':...}
    original_prec_tbl = {}
    for pgm in trainingset.keys():
        tbl = {}
        tbl['cast_proven_queries'] = trainingset[pgm]['cast_proven_queries']
        tbl['cast_unproven_queries'] = trainingset[pgm]['cast_unproven_queries']
        original_prec_tbl[pgm] = tbl

    ''' Building and archiving perf_tbl '''
    print('Building and archiving performance table of each atomic feature...')
    for pgm in trainingset.keys():
        print('Loading {}...'.format(pgm))
        perf_tbl_filename = os.path.join(trainingpath, pgm + '_' + tunneling + '_perf_tbl.pickle')
        cachefilepath = os.path.abspath(perf_tbl_filename)
        if use_cached_perf_tbl and check_cached_file(pgm, cachefilepath, featurejson):
            with open(cachefilepath, 'rb') as f:
                perf_tbl[pgm] = pickle.load(f)
        else:
            print('Reloading {}...'.format(cachefilepath))
            perfs = analyze_with_each_atom(trainingset[pgm]['jar'], featurejson, dooppath, analysis, trainingpath,
                                           tunneling, tmformula)
            perf_tbl[pgm] = perfs
            with open(cachefilepath, 'wb') as f:
                pickle.dump(perfs, f)
            print('{} is written.'.format(perf_tbl_filename))
        perf_tbl[pgm].append({'formula': trainingset[pgm]['formula'], 'time': trainingset[pgm]['cast_empty_cost'],
                              'cast_num_of_proven': trainingset[pgm]['cast_empty'],
                              'cast_proven_queries': trainingset[pgm]['cast_proven_queries'],
                              'cast_unproven_queries': trainingset[pgm]['cast_unproven_queries'],
                              'cast_chosen_methods': trainingset[pgm]['cast_chosen_methods']})

        for k in featurejson.keys():
            perf_f = search_perfs(perf_tbl[pgm], [['Feature' + k]])
            # unproven(all important) - unproven(a is not important)
            perf_f['difference'] = list(
                set(original_prec_tbl[pgm]['cast_unproven_queries']).difference(set(perf_f['cast_unproven_queries'])))
            # perf_f['difference'] = list(set(perf_f['cast_unproven_queries']).difference(set(original_prec_tbl[pgm]['cast_unproven_queries'])))

    print_perf_tbl(perf_tbl)
    print('Done')
    print('')

    '''
    Main refinement process
    '''
    print('Main learning process begins...')
    formula = refine(dooppath, analysis, featurejson, perf_tbl, trainingset, original_prec_tbl, tunneling, init_formula,
                     init_worklist, trainingpath, tmformula)
    # formula = [['Feature6','Feature3','Feature9','Feature5','Feature0'],['Feature4','Feature3','Feature1','Feature6','Feature9'],['Feature10', 'Feature8', 'Feature7', 'Feature0', 'Feature5']]

    return formula


def write_select_logic(formula, dooppath, tunneling, tmformula):
    tunneling_str = {'t': 'Tunneling(?meth)', 'tm': 'TunnelingM(?meth)'}
    select_logic_path = os.path.abspath(os.path.join(dooppath, 'logic', 'select.logic'))
    with open(select_logic_path, 'w') as f:
        if tunneling == 't':
            f.write(formula_to_logic(formula, tunneling_str[tunneling] + '\n'))
        else:
            f.write(
                formula_to_logic(formula, 'TunnelingM(?meth)') + '\n' + formula_to_logic(tmformula, 'Tunneling(?meth)'))
