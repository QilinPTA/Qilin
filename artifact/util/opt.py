#!/usr/bin/python3

import util.TerminalColor as tc


# OPTIONS
def makeup(str):
    return ' ' * (30 - len(str))


def bioption(opt, arg, des):
    return tc.BOLD + tc.YELLOW + opt + ' ' + tc.GREEN + arg + tc.WHITE + makeup(
        opt + arg) + des + tc.RESET + '\n'


def option(opt, des):
    return bioption(opt, '', des)
