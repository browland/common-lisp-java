package evaluator;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import evaluator.special.SpecialForm;
import evaluator.special.SpecialFormRegistry;
import function.Function;
import function.FunctionRegistry;
import syntaxtree.Atom;
import value.Macro;
import value.Symbol;
import value.Value;
import value.ValueType;

import java.util.Optional;

public class OperatorLookup {
    private static final SpecialFormRegistry specialFormRegistry = new SpecialFormRegistry();
    private static final FunctionRegistry functionRegistry = new FunctionRegistry();

    public OperatorType determineOperatorType(Atom operatorAtom,
                                              Environment environment) {
        String operatorName = operatorAtom.value();
        if(specialFormRegistry.findByName(operatorName).isPresent()) {
            return OperatorType.SPECIAL_FORM;
        }
        else if(functionRegistry.findByName(operatorName).isPresent()) {
            return OperatorType.FUNCTION;
        }

        Symbol operatorSymbol = Symbols.internSymbol(operatorName);

        Optional<Value<?>> optionalOperator = environment.getFunction(operatorSymbol);
        if(optionalOperator.isEmpty()) {
            throw new IllegalArgumentException("Unknown operator " + operatorName);
        }

        Value<?> operatorValue = optionalOperator.get();
        if(operatorValue.getType() == ValueType.MACRO) {
            return OperatorType.MACRO;
        }
        else if(operatorValue.getType() == ValueType.OPERATOR) {
            // todo remaining case ValueType.OPERATOR is a bit iffy - it's a function really
            return OperatorType.FUNCTION;
        }
        else {
            throw new IllegalArgumentException("unhandled operator value type: " + operatorValue.getType());
        }
    }

    public Function lookupFunction(String name, Environment environment) {
        Optional<Function> builtInFunction = functionRegistry.findByName(name);
        if(builtInFunction.isPresent()) {
            return builtInFunction.get();
        }

        Symbol operatorSymbol = Symbols.internSymbol(name);

        Optional<Value<?>> optionalOperator = environment.getFunction(operatorSymbol);
        if(optionalOperator.isPresent()) {
            Value<?> operatorValue = optionalOperator.get();
            if(operatorValue.getType() == ValueType.OPERATOR) {
                return (Function)operatorValue.getValue();
            }
            else {
                throw new IllegalStateException("expected operator " + name + " but has different type: " + operatorValue.getType());
            }
        }
        else {
            throw new IllegalStateException("expected operator but couldn't find it: " + name);
        }
    }

    public SpecialForm lookupSpecialForm(String name, Environment environment) {
        Optional<SpecialForm> specialFormOptional = specialFormRegistry.findByName(name);
        if(specialFormOptional.isPresent()) {
            return specialFormOptional.get();
        }
        else {
            throw new IllegalStateException("expected special form but couldn't find it: " + name);
        }
    }

    public Macro lookupMacro(String name, Environment environment) {
        Symbol operatorSymbol = Symbols.internSymbol(name);

        Optional<Value<?>> optionalMacro = environment.get(operatorSymbol);
        if(optionalMacro.isPresent()) {
            Value<?> macroValue = optionalMacro.get();
            if(macroValue.getType() == ValueType.MACRO) {
                return (Macro) macroValue.getValue();
            }
            else {
                throw new IllegalStateException("expected macro " + name + " but has different type: " + macroValue.getType());
            }
        }
        else {
            throw new IllegalStateException("expected macro but couldn't find it: " + name);
        }
    }
}
