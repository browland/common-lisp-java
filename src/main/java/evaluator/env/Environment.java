package evaluator.env;

import function.Function;
import value.Value;

import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.Stack;

public class Environment {
    private GlobalEnvironment globalEnvironment;
    private Deque<ScopeEnvironment> scopes;

    public Environment(GlobalEnvironment globalEnvironment) {
        this.globalEnvironment = globalEnvironment;
    }

    public Optional<Value<?>> getValue(String symbolName) {
        Optional<Value<?>> global = globalEnvironment.getValue(symbolName);
        if(global.isPresent()) {
            return global;
        }

        // otherwise walk stack
        Iterator<ScopeEnvironment> scopeIter = scopes.descendingIterator();
        while(scopeIter.hasNext()) {
            ScopeEnvironment scope = scopeIter.next();
            Optional<Value<?>> value = scope.getBinding(symbolName);
            if(value.isPresent()) {
                return value;
            }
        }

        return Optional.empty();
    }

    public Optional<Function> getFunction(String name) {
        Optional<Function> global = globalEnvironment.getFunction(name);
        if(global.isPresent()) {
            return global;
        }

        // otherwise walk stack
        Iterator<ScopeEnvironment> scopeIter = scopes.descendingIterator();
        while(scopeIter.hasNext()) {
            ScopeEnvironment scope = scopeIter.next();
            Optional<Function> function = scope.getFunction(name);
            if(function.isPresent()) {
                return function;
            }
        }

        return Optional.empty();
    }

    public void enterScope() {
        ScopeEnvironment scope = new ScopeEnvironment(globalEnvironment);
        scopes.push(scope);
    }

    public void leaveScope() {
        scopes.pop();
    }
}
