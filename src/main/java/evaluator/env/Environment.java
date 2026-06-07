package evaluator.env;

import value.Symbol;
import value.Value;
import value.ValueType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

public class Environment {
    private final GlobalEnvironment globalEnvironment;
    private Deque<ScopeEnvironment> scopes;

    public Environment() {
        this(new GlobalEnvironment());
    }

    public Environment(GlobalEnvironment globalEnvironment) {
        this.globalEnvironment = globalEnvironment;
        this.scopes = new LinkedList<>();
    }

    public Optional<Value<?>> getVariable(Symbol symbol) {
        // first walk stack of lexical scopes
        for (ScopeEnvironment scope : scopes) {
            Optional<Value<?>> value = scope.getVariable(symbol);
            if (value.isPresent()) {
                return value;
            }
        }

        // otherwise try to find a global variable
        return globalEnvironment.getVariable(symbol);
    }

    public void setVariable(Symbol symbol, Value<?> value) {
        // first walk stack of lexical scopes
        for (ScopeEnvironment scope : scopes) {
            if(scope.getVariable(symbol).isPresent()) {
                scope.setVariable(symbol, value);
                return;
            }
        }

        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't set for name which already exists in global env " + symbol);
        }

        globalEnvironment.setVariable(symbol, value);
    }

    @Deprecated
    public void setGlobal(Symbol symbol, Value<?> value) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't set for name which already exists in global env " + symbol);
        }

        switch(value.getType()) {
            case MACRO:
                globalEnvironment.setMacro(symbol, value);
                break;
            case OPERATOR:
                globalEnvironment.setFunction(symbol, value);
                break;
            default:
                globalEnvironment.setGlobal(symbol, value);
        }
    }

    public void setInScope(Symbol symbol,
                           Value<?> value,
                           Namespace namespace) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't set for symbol which already exists in global env " + symbol);
        }

        ScopeEnvironment thisScopeEnv = scopes.peek();
        if(thisScopeEnv == null) {
            throw new RuntimeException("Can't set in scope as no scopes exist!");
        }

        switch(namespace) {
            case VARIABLE -> thisScopeEnv.setVariable(symbol, value);
            case FUNCTION -> thisScopeEnv.setFunction(symbol, value);
            case BLOCK -> thisScopeEnv.setBlock(symbol, value);
        }
    }

    // Should pass a namespace as it depends on the caller, not the type of the value
    @Deprecated
    public void setInScope(Symbol symbol, Value<?> value) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't set for symbol which already exists in global env " + symbol);
        }

        ScopeEnvironment thisScopeEnv = scopes.peek();
        if(thisScopeEnv == null) {
            throw new RuntimeException("Can't set in scope as no scopes exist!");
        }

        if(value.getType() == ValueType.OPERATOR) {
            thisScopeEnv.setFunction(symbol, value);
        }
        else {
            thisScopeEnv.setVariable(symbol, value);
        }
    }

    /**
     * E.g. for setq we find the binding at the closest lexical level.  We work through lexical
     * scopes from inner to outer, and then consider the globals last.
     */
    public void setVariableInMostLocalScope(Symbol symbol,
                                            Value<?> value) {
        // walk stack of scopes first
        for (ScopeEnvironment scope : scopes) {
            Optional<Value<?>> possibleBinding = scope.getVariable(symbol);
            if (possibleBinding.isPresent()) {
                scope.setVariable(symbol, value);
                return;
            }
        }

        setVariable(symbol, value);
    }

    public Optional<Value<?>> getFunction(Symbol symbol) {
        for (ScopeEnvironment scope : scopes) {
            Optional<Value<?>> function = scope.getFunction(symbol);
            if (function.isPresent()) {
                return function;
            }
        }

        return globalEnvironment.getFunction(symbol);
    }

    public void setFunction(Symbol symbol, Value<?> functionValue) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't set for symbol which already exists in global env " + symbol);
        }

        globalEnvironment.setFunction(symbol, functionValue);
    }

    public Optional<Value<?>> getMacro(Symbol symbol) {
        for (ScopeEnvironment scope : scopes) {
            Optional<Value<?>> macro = scope.getMacro(symbol);
            if (macro.isPresent()) {
                return macro;
            }
        }

        return globalEnvironment.getMacro(symbol);
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

        // We have to be very careful re. scopes captured by closures:
        // 1. We must create a new stack (LinkedList) so closures don't lose any enclosing scope when that scope terminates.
        // 2. However, we must also keep a 'live view' of the captured scopes (HashMaps) so we see any changes to the
        //    variables within the scopes.  These changes may happen after a closure is created but before it's applied,
        //    and we should see the new value at application time.
        // 3. When multiple closures are created within a certain enclosing scope, they all see a single shared view of the
        //    captured scope.  The impl below fulfils this, as the captured scopes (HashMaps) are pointed to by the new
        //    stack (LinkedList).  The only small problem is we create a new stack (LinkedList) for each closure (in order
        //    to fulfil point 1) which could become memory-inefficient, but is simple enough for now.
        //
        // By creating a new LinkedList and passing the "canonical" one into the constructor, we fulfil all these
        // requirements.
        //
        // Additionally .. the captured scope still 'sees' a live (updating) view of the global variables.  So a closure
        // can reference global variables which aren't captured at creation time, but will be set at application time.
        // This is fulfilled by using the Environment copy constructor above, which points to the existing (single) global
        // environment.
        capturedEnvironment.scopes = new LinkedList<>(this.scopes);
        return capturedEnvironment;
    }
}
