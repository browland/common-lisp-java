package function;

import evaluator.AtomEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.macro.ConsToRList;
import evaluator.macro.MacroExpander;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.*;

import java.util.List;
import java.util.Optional;

public class Macroexpand1 implements Function {
    private final MacroExpander macroExpander = new MacroExpander();
    private final ConsToRList consToRList = new ConsToRList();
    private final AtomEvaluator atomEvaluator = new AtomEvaluator();

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // We'll get a single operand of type ConsCellValue.
        // The car will be the macro name so we'll need to look it up from the env as a Macro (it'll already have been
        // defmacro'd.
        ConsCellValue macroCallConsValue = (ConsCellValue) operands.getFirst();
        SymbolValue macroSymbolValue = (SymbolValue) macroCallConsValue.getValue().car();
        Symbol macroSymbol = macroSymbolValue.getValue();

        // Look up the macro from the env
        Optional<Value<?>> optionalMacroValue = environment.getMacro(macroSymbol);
        if(optionalMacroValue.isEmpty()) {
            throw new IllegalArgumentException("could not find macro for expansion: " + macroSymbol.name());
        }

        MacroValue macroValue = (MacroValue)optionalMacroValue.get();
        Macro macro = macroValue.getValue();

        // The cdr will be the arguments to the macro
        // We need to reconstruct the RList of the entire invocation as if we encounter it 'normally' so we can pass it
        // to MacroExpander.
        RList macroInvocationRList = (RList)consToRList.translate(macroCallConsValue);

        // Now pass to MacroExpander to get the syntax tree
        Evaluator evaluator = new Evaluator();  // feels wrong, but Evaluator is stateless
        Node expandedResult = macroExpander.expand(macro, macroInvocationRList, evaluator, environment);

        // Now need to convert to a ConsCellValue as we always return a Value from a function.
        if(expandedResult instanceof RList rlistResult) {
            return ConsCellValue.fromRList(rlistResult);
        }
        else if(expandedResult instanceof Atom atomResult) {
            return atomEvaluator.atomToValueNoLookup(atomResult.value());
        }
        else {
            throw new IllegalArgumentException("Unsupported macro expansion result");
        }
    }
}
