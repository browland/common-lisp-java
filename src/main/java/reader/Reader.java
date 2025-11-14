package reader;

import java.util.Set;

public class Reader {
    private final static Set<Character> PREFIX_CHARS = Set.of(
            // todo more ...
            '\'', '#', '`'
    );

    private final StringBuilder prefixBuilder = new StringBuilder();
    private final StringBuilder atomStringBuilder = new StringBuilder();

    private boolean readPrefix;
    private RList.Builder listBuilder;
    private int depth;
    private String prefix;

    public RList read(String program) {
        for(char c : program.toCharArray()) {
            if(!readPrefix) {
                if(PREFIX_CHARS.contains(c)) {
                    prefixBuilder.append(c);
                    continue;
                }
                else {
                    readPrefix = true;
                    prefix = prefixBuilder.length() > 0 ? prefixBuilder.toString() : null;
                }
            }

            // now deal with atom or list
            if(c == '(') {
                startList();
            }
            else if(c == ')') {
                endList();
            }
            else if(c == ' ') {
                endNode();
            }
            else {
                if(c != '\n') {  // todo check for whitespace char instead
                    atomStringBuilder.append(c);
                }
            }
        }

        return listBuilder.build();
    }

    private void endNode() {
        // if we don't have any characters from an atom then we may have just completed parsing a list
        if(atomStringBuilder.isEmpty()) {
            readPrefix = false;  // Todo this feels like a bit of a hack; we should be clearing this flag earlier, not when we've processed a list and we're now on the space after it
            return;
        }

        String atom = atomStringBuilder.toString();
        Atom.Builder atomBuilder = new Atom.Builder()
                .value(atom)
                .prefix(prefix);
        atomStringBuilder.delete(0, atomStringBuilder.length());

        readPrefix = false;  // whenever we consume 'prefix' we need to reset the flag to say prefix has been read
        prefixBuilder.delete(0, atomStringBuilder.length());

        listBuilder.addNodeBuilder(atomBuilder);  // todo need to collect the prefix into whatever we add here, not just a string
    }

    private void endList() {
        if(!atomStringBuilder.isEmpty()) {
            Atom.Builder atomBuilder = new Atom.Builder()
                    .value(atomStringBuilder.toString())
                    .prefix(prefix);
            listBuilder.addNodeBuilder(atomBuilder);
            atomStringBuilder.delete(0, atomStringBuilder.length());
        }

        listBuilder = listBuilder.getParentListBuilder() != null ? listBuilder.getParentListBuilder() : listBuilder;
        depth--;
    }

    private void startList() {
        RList.Builder tempListBuilder = new RList.Builder()
                .parentListBuilder(listBuilder)
                .depth(depth)
                .prefix(prefix);

        readPrefix = false;  // whenever we consume 'prefix' we need to reset the flag to say prefix has been read
        prefixBuilder.delete(0, prefixBuilder.length());  // whenever we consume 'prefix' we need to reset the flag to say prefix has been read

        if(listBuilder != null) {
            listBuilder.addNodeBuilder(tempListBuilder);
        }
        listBuilder = tempListBuilder;
        depth++;
    }


    public static void main(String[] args) {
        String program = """
                (defun even (num) (= (mod num 2) 0))
                """;
        Reader reader = new Reader();
        RList list = reader.read(program);
        System.out.println(list);
    }
}
