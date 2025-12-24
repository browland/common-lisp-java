package value;

import syntaxtree.Atom;
import syntaxtree.RList;

import java.util.List;

public record ConsCell(Value<?> car,
                       Value<?> cdr) {

}
