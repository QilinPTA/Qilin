#!/usr/bin/python3

# latex code before table definition.
def genDocHeadPart():
    headPart = [r"\documentclass{article}",
                r"\usepackage{adjustbox}",
                r"\usepackage{booktabs}",
                r"\setlength\heavyrulewidth{0.3ex}",
                r"\usepackage{multirow}",
                r"\usepackage{geometry}",
                r"\usepackage{xspace}",
                r"\geometry{a4paper, top=10mm, left=10mm, right=10mm}",
                r"\usepackage[table,xcdraw]{xcolor}",
                r"\newcommand{\kobj}{\textsc{$k$obj}\xspace}",
                r"\newcommand{\twoobj}{\textsc{$2$obj}\xspace}",
                r"\newcommand{\eagleobj}[1]{\textsc{E-#1obj}\xspace}",
                r"\newcommand{\zipperobj}[1]{\textsc{Z-#1obj}\xspace}",
                r"\newcommand{\toolobj}[1]{\textsc{T-#1obj}\xspace}",
                r"\newcommand{\threeobj}{\textsc{$3$obj}\xspace}",
                r"\newcommand{\zipper}{\textsc{zipper}\xspace}",
                r"\newcommand{\eagle}{\textsc{eagle}\xspace}",
                r"\newcommand{\tool}{\textsc{Turner}\xspace}",
                r"\definecolor{floralwhite}{rgb}{1.0, 0.98, 0.94}",
                r"\definecolor{gainsboro}{rgb}{0.86, 0.86, 0.86}",
                r"\definecolor{ghostwhite}{rgb}{0.97, 0.97, 1.0}",
                r"\definecolor{deepchampagne}{rgb}{0.98, 0.84, 0.65}",
                r"\definecolor{darkgray}{rgb}{0.66, 0.66, 0.66}",
                r"\definecolor{cornsilk}{rgb}{1.0, 0.97, 0.86}",
                r"\definecolor{antiquewhite}{rgb}{0.98, 0.92, 0.84}",
                r"\begin{document}",
                r"",
                ]
    return "\n".join(headPart)


# latex code for ending the table.
def genTableTailPart():
    return "\n".join(["\t\end{tabular}", "}", "\end{table}"])


def genDocTailPart():
    return "\n\end{document}\n"
