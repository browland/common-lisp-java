package evaluator.env;

import value.Symbol;
import value.Value;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

public class Environment {
    private GlobalEnvironment globalEnvironment;
    private Deque<ScopeEnvironment> scopes;
    private Symbols symbols;

    public Environment() {
        this(new GlobalEnvironment(), new Symbols());
    }

    public Environment(GlobalEnvironment globalEnvironment, Symbols symbols) {
        this.globalEnvironment = globalEnvironment;
        this.scopes = new LinkedList<>();
        this.symbols = symbols;
    }


    public Optional<Value<?>> get(Symbol symbol) {
        Optional<Value<?>> global = globalEnvironment.getValue(symbol);
        if(global.isPresent()) {
            return global;
        }

        // otherwise walk stack
        Iterator<ScopeEnvironment> scopeIter = scopes.descendingIterator();
        while(scopeIter.hasNext()) {
            ScopeEnvironment scope = scopeIter.next();
            Optional<Value<?>> value = scope.getBinding(symbol);
            if(value.isPresent()) {
                return value;
            }
        }

        return Optional.empty();
    }

    public void setGlobal(Symbol symbol, Value<?> value) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't set for name which already exists in global env " + symbol);
        }

        switch(value.getType()) {
            case MACRO:
                globalEnvironment.setMacro(symbol, value);
            case OPERATOR:
                globalEnvironment.setFunction(symbol, value);
            // todo bug!
            default:
                globalEnvironment.setGlobal(symbol, value);
        }
    }

    public void setInScope(Symbol symbol, Value<?> value) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't set for symbol which already exists in global env " + symbol);
        }

        ScopeEnvironment thisScopeEnv = scopes.peek();
        if(thisScopeEnv == null) {
            throw new RuntimeException("Can't set in scope as no scopes exist!");
        }

        // todo should have some kind of protection around this
        thisScopeEnv.setBinding(symbol, value);
    }

    public Optional<Value<?>> getFunction(Symbol symbol) {
        Optional<Value<?>> global = globalEnvironment.getFunction(symbol);
        if(global.isPresent()) {
            return global;
        }

        // otherwise walk stack
        Iterator<ScopeEnvironment> scopeIter = scopes.descendingIterator();
        while(scopeIter.hasNext()) {
            ScopeEnvironment scope = scopeIter.next();
            Optional<Value<?>> function = scope.getFunction(symbol);
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
        if(scopes.isEmpty()) {
            throw new IllegalStateException("Leaving scope but no scopes exist");
        }
        scopes.pop();
    }

    public Environment capture() {
        Environment capturedEnvironment = new Environment(this.globalEnvironment, this.symbols);
        capturedEnvironment.scopes = new LinkedList<>(this.scopes);
        return capturedEnvironment;
    }

    public Symbols getSymbols() {
        return symbols;
    }
}
