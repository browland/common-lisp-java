package evaluator.special;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpecialFormRegistry {
    private final Map<String, SpecialForm> registry = new HashMap<>();

    public SpecialFormRegistry() {
        registry.put("lambda", new Lambda());
        registry.put("defun", new Defun());
        registry.put("defvar", new Defvar());
        registry.put("setq", new Setq());
        registry.put("if", new If());
        registry.put("defmacro", new DefMacro());
        registry.put("quote", new Quote());
        registry.put("let", new Let());
        registry.put("quasiquote", new Quasiquote());
        registry.put("block", new Block());
        registry.put("return-from", new ReturnFrom());
        registry.put("progn", new Progn());
        registry.put("function", new Function());
        registry.put("flet", new FLet());
        registry.put("tagbody", new Tagbody());
        registry.put("go", new Go());
        registry.put("cond", new Cond());
    }

    public Optional<SpecialForm> findByName(String name) {
        return Optional.ofNullable(registry.get(name));
    }
}
