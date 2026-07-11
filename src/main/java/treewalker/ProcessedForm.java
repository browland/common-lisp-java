package treewalker;

// for want of a better name ...
public class ProcessedForm implements ProcessedNode {
    // todo do we just need the name of the generated function?  At this point we're enforcing compiler specifics on
    //      this type - OK for now
    private String generatedFunction;

    public ProcessedForm(String generatedFunction) {
        this.generatedFunction = generatedFunction;
    }

    public String toString() {
        return "apply form function " + generatedFunction;
    }
}
