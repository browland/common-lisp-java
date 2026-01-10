package evaluator.env;

import value.Symbol;
import value.Value;

import java.util.Deque;
import java.util.Iterator;
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

    public Optional<Value<?>> get(Symbol symbol) {
        // first walk stack of lexical scopes
        for (ScopeEnvironment scope : scopes) {
            Optional<Value<?>> value = scope.getBinding(symbol);
            if (value.isPresent()) {
                return value;
            }
        }

        // otherwise try to find a global variable
        return globalEnvironment.getValue(symbol);
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

    /**
     * E.g. for setq we find the binding at the closest lexical level.  We work through lexical
     * scopes from inner to outer, and then consider the globals last.
     */
    public void setInMostLocalScope(Symbol symbol,
                                    Value<?> value) {
        // walk stack of scopes first
        for (ScopeEnvironment scope : scopes) {
            Optional<Value<?>> possibleBinding = scope.getBinding(symbol);
            if (possibleBinding.isPresent()) {
                scope.setBinding(symbol, value);
                return;
            }
        }

        setGlobal(symbol, value);
    }

    /**
     * todo Bug?  Should look at scopes first and then globals last?
     */
    public Optional<Value<?>> getFunction(Symbol symbol) {
        Optional<Value<?>> global = globalEnvironment.getFunction(symbol);
        if(global.isPresent()) {
            return global;
        }

        // otherwise walk stack of scopes
        // todo probably wrong and should be regular iterator using enhanced for loop
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
