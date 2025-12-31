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
        registry.put("setf", new Setf());
        registry.put("if", new If());
        registry.put("defmacro", new DefMacro());
        registry.put("quote", new Quote());
    }

    public Optional<SpecialForm> findByName(String name) {
        return Optional.ofNullable(registry.get(name));
    }
}
