# -*- coding: utf-8 -*-
"""
Created on Sun Nov 27 12:45:01 2016

@author: Rakesh Ramesh
"""

"""
<PARSER>
"""


from copy import deepcopy
from ply import lex
from ply import yacc

tokens = ('VAR', 'NOT', 'AND', 'OR', 'LPAREN', 'RPAREN', 'IMPLICATION', 'COMMA', 'IGNORE')

t_VAR = r'[A-Za-z0-9]+[(]([A-Za-z0-9]+)([,][A-Za-z0-9]+)*[)]'
t_NOT = r'~'
t_AND = r'&'
t_OR = r'\|'
t_LPAREN = r'\('
t_RPAREN = r'\)'
t_IMPLICATION = r'=>'
t_COMMA = r','
t_IGNORE = r'\s\t\r'

def p_direct_NOT(p):
    """ pred : NOT pred """
    p[0] = [p[1], p[2]]

def p_pred_NOT(p):
    """ pred : LPAREN NOT pred RPAREN """
    p[0] = [p[2], p[3]]

def p_pred_AND(p):
    """ pred : LPAREN pred AND pred RPAREN """
    p[0] = [p[2], p[3], p[4]]

def p_pred_OR(p):
    """ pred : LPAREN pred OR pred RPAREN """
    p[0] = [p[2], p[3], p[4]]

def p_pred_IMPLCATION(p):
    """ pred : LPAREN pred IMPLICATION pred RPAREN """
    p[0] = [p[2], p[3], p[4]]

def p_pred_VAR(p):
    """ pred : VAR """
    p[0] = p[1]


def t_error(t):
    print "Illegal character '%s'" % t.value[0]
    t.lexer.skip(1)

def p_error(p):
    if p:
        print "Syntax error at '%s'" % p.value
    else:
        print "Syntax error at EOF"

lexer = lex.lex()
parser = yacc.yacc()

"""
</PARSER>
"""

#### <CONSTANTS>
OR = '|'
AND = '&'
IMPLIES = '=>'
NOT = '~'
#### </CONSTANTS>

"""
<CNF CONVERSION>
"""
#### <IMPLIES ELIMINATION>

def implies_elimination(predicate):
    if isinstance(predicate, str):
        return predicate
    elif len(predicate) == 2:
        return [NOT, implies_elimination(predicate[1])]
    elif len(predicate) == 3 and predicate[1] != IMPLIES:
        return [implies_elimination(predicate[0]), predicate[1], implies_elimination(predicate[2])]
    else:
        lH = [NOT, implies_elimination(predicate[0])]
        return [lH, OR, implies_elimination(predicate[2])]

#### </IMPLIES ELIMINATION>

#### <MOVING NOT INSIDE>

def move_not_inwards(predicate):
    if isinstance(predicate, str):
        return predicate
    elif len(predicate) == 2:
        return not_mover(predicate[1])
    elif len(predicate) == 3:
        lH = move_not_inwards(predicate[0])
        rH = move_not_inwards(predicate[2])
        return [lH, predicate[1], rH]

def not_mover(predicate):
    if isinstance(predicate, str):
        return [NOT,predicate]
    elif len(predicate) == 2:
        return move_not_inwards(predicate[1])
    elif len(predicate) == 3:
        symbol = AND if predicate[1] == OR else OR
        lH = not_mover(predicate[0])
        rH = not_mover(predicate[2])
        return [lH, symbol, rH]

#### </MOVING NOT INSIDE>

#### <OR DISTRIBUTION>

def or_distribute(predicate):
    if isinstance(predicate, str) or len(predicate)==2:
        return predicate
    elif len(predicate) == 3 and predicate[1] == AND:
        return [or_distribute(predicate[0]), AND, or_distribute(predicate[2])]
    elif len(predicate) == 3 and predicate[1] == OR:
        lH = or_distribute(predicate[0])
        rH = or_distribute(predicate[2])
        and_l = True if len(lH) == 3 and lH[1] == AND else False
        and_r = True if len(rH) == 3 and rH[1] == AND else False
        if and_l and and_r:
            # return [[[lH[0], OR, rH[0]], AND, [lH[0], OR, rH[2]]], AND, [[lH[2], OR, rH[0]], AND, [lH[2], OR, rH[2]]]]
            return or_distribute([or_distribute([[lH[0], OR, rH[0]], AND, or_distribute([lH[0], OR, rH[2]])]), AND, or_distribute([[lH[2], OR, rH[0]], AND, or_distribute([lH[2], OR, rH[2]])])])
        elif and_l:
            return [or_distribute([lH[0], OR, rH]), AND, or_distribute([lH[2], OR, rH])]
        elif and_r:
            return [or_distribute([lH, OR, rH[0]]), AND, or_distribute([lH, OR, rH[2]])]
        else:
            return [lH, OR, rH]
        
#### </OR DISTRIBUTION>

#### <GENERATE CNF>

def CNF(predicate):
    imp_e = implies_elimination(predicate)
    mni = move_not_inwards(imp_e)
    or_d = or_distribute(mni)
    return or_d

#### </GENERATE CNF>

"""
</CNF CONVERSION>
"""

"""
<KB AND INDEXER>
"""

#### <ADD PREDICATE TO KB>

def KBAppend(KB, predicate):
    if isinstance(predicate, str) or len(predicate) == 2:
        if predicate not in KB:
            KB.append(predicate)
    elif len(predicate) == 3 and predicate[1] != AND:
        if predicate not in KB:
            KB.append(predicate)
    if len(predicate) == 3 and predicate[1] == AND:
        KBAppend(KB, predicate[0])
        KBAppend(KB, predicate[2])
        
#### </ADD PREDICATE TO KB>

#### <STANDARDIZE VARIABLES>

def standardize(predicate, idx):
    if isinstance(predicate, str):
        return predicate
    elif len(predicate) == 2:
        return [NOT, standardize(predicate[1], idx)]
    elif len(predicate)==3:
        lH = standardize(predicate[0], idx)
        rH = standardize(predicate[2], idx)
        if isinstance(lH, str) or (len(lH) == 2 and lH[0] == NOT):
            lH = [lH]
        if isinstance(rH, str) or (len(rH) == 2 and rH[0] == NOT):
            rH = [rH]
        lH.extend(rH)
        return lH

#### </STANDARDIZE VARIABLES>

#### <GET FUNCTION NAME>

def getFunctionName(predicate):
    if isinstance(predicate, str):
        return predicate[0:predicate.index('(')]
    elif len(predicate) == 2:
        return predicate[1][0:predicate[1].index('(')]
                         
#### </GET FUNCTION NAME>

#### <GENERATE INDEXER>

def IndexerAppend(Indexer, predicate, idx):
    if isinstance(predicate, str):
        F = getFunctionName(predicate)
        if F in Indexer:
            if idx not in Indexer[F]:
                Indexer[F].append(idx)
        else:
            Indexer[F] = [idx]
    elif len(predicate) == 2 and predicate[0] == NOT and isinstance(predicate[1], str):
        F = getFunctionName(predicate)
        if ('~',F) in Indexer:
            if idx not in Indexer[('~',F)]:
                Indexer[('~',F)].append(idx)
        else:
            Indexer[('~',F)] = [idx]
    elif isinstance(predicate, list):
        for i in predicate:
            IndexerAppend(Indexer, i, idx)

#### </GENERATE INDEXER>

#### <CREATE KB AND INDEXER>

def getKBandIndexer(data_list):
    KB = []
    Indexer = {}
    for i in data_list:
        KBAppend(KB, CNF(i))
    for i in range(len(KB)):
        KB[i] = standardize(KB[i], i)
        IndexerAppend(Indexer, KB[i], i)
    return KB, Indexer

#### </CREATE KB AND INDEXER>
    
"""
</KB AND INDEXER>
"""

"""
<UNIFICATION AND RESOLUTION>
"""

#### <EXPLODE>

def explode(predicate):
    func = predicate[0:predicate.index('(')]
    var_list = predicate[(predicate.index('(')+1): predicate.index(')')].split(',')
    return [func, var_list]

#### </EXPLODE>

#### <FLASH ADD>

def flashAdd(KB, Indexer, predicate):
        
    KB.append(predicate)
    idx = len(KB)-1
    IndexerAppend(Indexer, predicate, idx)

#### </FALSH ADD>

#### <GET SUBSTITUTION LIST>

def getSubstitution(predicate, unifier):
    if isinstance(predicate, str) or (len(predicate)==2 and predicate[0]==NOT):
        p_pos = False if len(predicate) == 2 and predicate[0] == NOT else True
        u_pos = False if len(unifier) == 2 and unifier[0] == NOT else True
        if (p_pos and u_pos) or (not p_pos and not u_pos):
            return None, None
        else:
            if p_pos:
                E1 = explode(predicate)
            else:
                E1 = explode(predicate[1])
            if u_pos:
                E2 = explode(unifier)
            else:
                E2 = explode(unifier[1])
            if E1[0] != E2[0]:
                return None, None
            substList = []
            mergingClause = predicate
            for i in range(len(E1[1])):
                if E1[1][i][0].isupper() and E2[1][i][0].isupper() and E1[1][i] != E2[1][i]:
                    return None, None
                elif E1[1][i][0].isupper() and E2[1][i][0].islower():
                    substList.append([E2[1][i], E1[1][i]])
                elif E1[1][i][0].islower() and E2[1][i][0].isupper():
                    substList.append([E1[1][i], E2[1][i]])
                elif E1[1][i][0].islower() and E2[1][i][0].islower():
                    substList.append([E1[1][i], E2[1][i]])
                else:
                    substList.append([E1[1][i], E2[1][i]])
            # Change done HERE
            for i in range(len(substList)):
                for j in range(len(substList)):
                    if i == j:
                        continue
                    else:
                        if substList[i][0] == substList[j][0]:
                            if substList[i][1][0].isupper() and substList[j][1][0].isupper() and substList[i][1] != substList[j][1]:
                                return None, None
                        if substList[i][0] == substList[j][1]:
                            if substList[i][1][0].isupper():
                                substList[j][1] = substList[i][1]
            return substList, mergingClause
    else:
        for i in predicate:
            res, merger = getSubstitution(i, unifier)
            if res:
                return res, merger
        return None, None

#### </GET SUBSTITUTION LIST>

#### <COMBINE>
        
def combine(p1, p2, unifier, subst, merger):
    if p1 == unifier:
        p1 = []
    else:
        p1.remove(unifier)
    if p2 == merger:
        p2 = []
    else:
        p2.remove(merger)
    p2.extend(p1)
    for i in range(len(p2)):
        pos = False
        if isinstance(p2[i], str):
            E = explode(p2[i])
            pos = True
        else:
            E = explode(p2[i][1])
        for j in range(len(subst)):
            for k in range(len(E[1])):
                if E[1][k] == subst[j][0]:
                    E[1][k] = subst[j][1]
        if pos:
            p2[i] = E[0]+"("+','.join(E[1])+")"
        else:
            p2[i] = ['~',E[0]+"("+','.join(E[1])+")"]
    flag = set()
    for i in range(len(p2)):
        for j in range((i+1), len(p2)):
            if p2[i] == p2[j]:
                flag.add(j)
    res = []
    for i in range(len(p2)):
        if i not in flag:
            res.append(p2[i])
    return res

#### </COMBINE>

#### <UNIFY AND RESOLVE>

def unify_resolve(pred1, pred2, unifier):
    pred1 = deepcopy(pred1)
    pred2 = deepcopy(pred2)
    subst, mergingList = getSubstitution(pred2, unifier)
    if subst is None:
        return None
    combined = combine(pred1, pred2, unifier, subst, mergingList)
    return combined

#### </UNIFY AND RESOLVE>


#### <RESOLUTION RECURSIVE>

def resolve_rec(KB, Indexer, query):
    if query in KB:
        return False
    else:
        flashAdd(KB, Indexer, query)    
    if isinstance(query, str) or (len(query)==2 and query[0] == NOT):
        query_val = query
    else:
        query_val = query[0]
    F = getFunctionName(query_val)
    pos = []
    if isinstance(query_val, str):
        if ('~',F) in Indexer:
            pos = Indexer[('~',F)]
    else:
        if F in Indexer:
            pos = Indexer[F]
    for i in pos:
        res = unify_resolve(query, KB[i],query_val)
        if res is not None:
            if res == []:
                return True
            R = resolve_rec(KB, Indexer, res)
            if R is True:
                return True
    return False

#### </RESOLTUION RECURSIVE>

"""
</UNIFICATION AND RESOLUTION>
"""

""" 
<MAIN CALLER>
"""

filepath = "./input.txt"
fo = open(filepath)
n = int(fo.readline().strip())
prove = []
for i in range(n):
    prove.append(parser.parse(fo.readline().strip().replace(" ",""), lexer = lexer))
x = int(fo.readline().strip())
data_list = []
for i in range(x):
    data_list.append(CNF(parser.parse(fo.readline().strip().replace(" ",""), lexer = lexer)))
KB, Indexer = getKBandIndexer(data_list)
f = open("./output.txt","w")
for i in range(len(prove)):
    KB_C = deepcopy(KB)
    Indexer_C = deepcopy(Indexer)
    prove[i] = move_not_inwards(['~',prove[i]])
    f.write(str(resolve_rec(KB_C, Indexer_C, prove[i])).upper()+"\n")
f.close()
    
""" 
</MAIN CALLER>
"""