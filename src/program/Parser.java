package program;

import java.util.*;

public class Parser {
    private final Grammar grammar;
    private HashMap<String, Set<String>> firstSet;
    private HashMap<String, Set<String>> followSet;
    private List<List<String>> productionsRhs;

    public Parser(Grammar grammar) {
        this.grammar = grammar;
        this.firstSet = new HashMap<>();
        this.followSet = new HashMap<>();

        first();
    }

    private Set<String> concatenate(List<String> nonTerminals, String terminal) {
        if (nonTerminals.size() == 0) return new HashSet<>();
        if (nonTerminals.size() == 1) return firstSet.get(nonTerminals.iterator().next());

        Set<String> concatenation = new HashSet<>();
        boolean allEpsilon = true;

        for (String nonTerminal : nonTerminals)
            if (!firstSet.get(nonTerminal).contains("epsilon"))
                allEpsilon = false;

        // 3. If FIRST (Yi) contains Є for all i = 1 to n, then add Є to FIRST(X).
        if (allEpsilon) {
            if (terminal == null) concatenation.add("epsilon");
            else concatenation.add(terminal);
        }

        int i = 0;
        // 2. If FIRST(Y1) contains Є then FIRST(X) = { FIRST(Y1) – Є } U { FIRST(Y2) }
        while (i < nonTerminals.size()) {
            boolean epsilonPresent = false;
            for (String s : firstSet.get(nonTerminals.get(i))) {
                if (s.equals("epsilon")) epsilonPresent = true;
                else concatenation.add(s);
            }

            if (epsilonPresent) i++;
            //1. FIRST(X) = FIRST(Y1)
            else
                break;
        }

        return concatenation;
    }

    public void first() {
        boolean firstSetChanged = true;

        for (String nonterminal : grammar.getNonTerminals()) {
            firstSet.put(nonterminal, new HashSet<>());

            Set<List<String>> productionForNonterminal = grammar.getProductionForNonterminal(nonterminal);
            for (List<String> production : productionForNonterminal) {
                if (grammar.getTerminals().contains(production.get(0)) || production.get(0).equals("epsilon"))
                    firstSet.get(nonterminal).add(production.get(0));
            }
        }

        while (firstSetChanged) {
            firstSetChanged = false;
            HashMap<String, Set<String>> newFirstSet = new HashMap<>();

            for (String nonterminal : grammar.getNonTerminals()) {
                Set<List<String>> productionForNonterminal = grammar.getProductionForNonterminal(nonterminal);
                Set<String> firstSetForNonTerminal = new HashSet<>(firstSet.get(nonterminal));

                for (List<String> production : productionForNonterminal) {
                    List<String> rhsNonTerminals = new ArrayList<>();
                    String rhsTerminal = null;

                    for (String symbol : production) {
                        if (this.grammar.getNonTerminals().contains(symbol))
                            rhsNonTerminals.add(symbol);
                        else {
                            rhsTerminal = symbol;
                            break;
                        }
                    }

                    firstSetForNonTerminal.addAll(concatenate(rhsNonTerminals, rhsTerminal));
                }
                if (!firstSetForNonTerminal.equals(firstSet.get(nonterminal))) {
                    firstSetChanged = true;
                }
                
                newFirstSet.put(nonterminal, firstSetForNonTerminal);
            }
            firstSet = newFirstSet;
        }
    }

    public String printFirst() {
        StringBuilder builder = new StringBuilder();
        firstSet.forEach((k, v) -> {
            builder.append(k).append(": ").append(v).append("\n");
        });
        return builder.toString();
    }

    public String printFollow() {
        StringBuilder builder = new StringBuilder();
        followSet.forEach((k, v) -> {
            builder.append(k).append(": ").append(v).append("\n");
        });
        return builder.toString();
    }

    public Grammar getGrammar() {
        return grammar;
    }
}