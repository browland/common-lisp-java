package evaluator.env;

import value.Value;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

public class Environment {
    private GlobalEnvironment globalEnvironment;
    private Deque<ScopeEnvironment> scopes;

    public Environment() {
        this(new GlobalEnvironment());
    }

    public Environment(GlobalEnvironment globalEnvironment) {
        this.globalEnvironment = globalEnvironment;
        this.scopes = new LinkedList<>();
    }

    public Optional<Value<?>> get(String symbolName) {
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

    public void setGlobal(String name, Value<?> value) {
        if(globalEnvironment.isReserved(name)) {
            throw new RuntimeException("Can't set for name which already exists in global env " + name);
        }

        switch(value.type()) {
            case MACRO:
                globalEnvironment.setMacro(name, value);
            case OPERATOR:
                globalEnvironment.setFunction(name, value);
            // todo bug!
            default:
                globalEnvironment.setGlobal(name, value);
        }
    }

    public void setInScope(String name, Value<?> value) {
        if(globalEnvironment.isReserved(name)) {
            throw new RuntimeException("Can't set for name which already exists in global env " + name);
        }

        ScopeEnvironment thisScopeEnv = scopes.peek();
        if(thisScopeEnv == null) {
            throw new RuntimeException("Can't set in scope as no scopes exist!");
        }

        // todo should have some kind of protection around this
        thisScopeEnv.setBinding(name, value);
    }

    public Optional<Value<?>> getFunction(String name) {
        Optional<Value<?>> global = globalEnvironment.getFunction(name);
        if(global.isPresent()) {
            return global;
        }

        // otherwise walk stack
        Iterator<ScopeEnvironment> scopeIter = scopes.descendingIterator();
        while(scopeIter.hasNext()) {
            ScopeEnvironment scope = scopeIter.next();
            Optional<Value<?>> function = scope.getFunction(name);
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
        Environment capturedEnvironment = new Environment(this.globalEnvironment);
        capturedEnvironment.scopes = new LinkedList<>(this.scopes);
        return capturedEnvironment;
    }
}
