package evaluator;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import evaluator.special.SpecialForm;
import evaluator.special.SpecialFormRegistry;
import exception.EvaluationException;
import function.Function;
import syntaxtree.Atom;
import value.Macro;
import value.Symbol;
import value.Value;
import value.ValueType;

import java.util.Optional;

public class OperatorLookup {
    private static final SpecialFormRegistry specialFormRegistry = new SpecialFormRegistry();

    public OperatorType determineOperatorType(Atom operatorAtom,
                                              Environment environment) {
        String operatorName = operatorAtom.value();
        if(specialFormRegistry.findByName(operatorName).isPresent()) {
            return OperatorType.SPECIAL_FORM;
        }

        Symbol operatorSymbol = Symbols.internSymbol(operatorName);

        Optional<Value<?>> optionalFunction = environment.getFunction(operatorSymbol);
        if(optionalFunction.isPresent()) {
            // todo assuming the ValueType is ValueType.OPERATOR - not sure if we need that distinction since we found it in the function namespace?
            return OperatorType.FUNCTION;
        }

        Optional<Value<?>> optionalMacro = environment.getMacro(operatorSymbol);
        if(optionalMacro.isPresent()) {
            return OperatorType.MACRO;
        }

        throw new EvaluationException("Unknown operator " + operatorName);
    }

    public Optional<Function> lookupFunction(String name, Environment environment) {
        Symbol operatorSymbol = Symbols.internSymbol(name);

        Optional<Value<?>> optionalOperator = environment.getFunction(operatorSymbol);
        if(optionalOperator.isPresent()) {
            Value<?> operatorValue = optionalOperator.get();
            if(operatorValue.getType() == ValueType.OPERATOR) {
                return Optional.of((Function)operatorValue.getValue());
            }
            else {
                throw new IllegalStateException("expected operator " + name + " but has different type: " + operatorValue.getType());
            }
        }
        else {
            return Optional.empty();
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

        Optional<Value<?>> optionalMacro = environment.getMacro(operatorSymbol);
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
