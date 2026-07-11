package treewalker;

public class ApplyFormFunctionGenerator {
    private static int counter;

    public static String getNextFunctionName() {
        return "applyForm_" + counter++;
    }
}
