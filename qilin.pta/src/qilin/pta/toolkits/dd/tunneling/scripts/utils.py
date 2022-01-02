import subprocess
import re
import os
import sys
import json


def runDoop(cmd, dooppath):
    print(cmd)
    cwd = os.getcwd()

    os.chdir(dooppath)
    output = subprocess.check_output(dooppath + '/' + cmd + '; exit 0;', stderr=subprocess.STDOUT, shell=True)
    os.chdir(cwd)

    time_pattern = re.compile(r'analysis time:\s+([0-9,\.]+)s')
    m = time_pattern.search(output)
    if m == None:
        print('Time string doesn\'t exist: {}'.format(output))
        sys.exit()

    return output


def beep():
    print
    '\a'
    return


def show_delta_time(t1, t2):
    spendtime = t2 - t1
    m, s = divmod(spendtime, 60)
    h, m = divmod(m, 60)
    print("%d:%02d:%02d" % (h, m, s))
    return


def check_path(path, prefix):
    abspath = os.path.abspath(path)
    if not (os.path.isdir(abspath) or os.path.isfile(abspath)):
        print('ERROR: {} is not valid path for {}.'.format(abspath, prefix))
        sys.exit()
    else:
        return abspath
