package compiler.specialform;

import compiler.AsmGenerator;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import treewalker.Function;
import treewalker.SymbolAtom;
import treewalker.TreeWalker;
import treewalker.TypedAtom;

import java.util.List;

public class DefunSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator) {
        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();

        Node nameNode = rlist.nodes().get(1);
        SymbolAtom nameSymbolAtom = TypedAtom.toSymbolAtom(nameNode);
        String name = nameSymbolAtom.getValue();

        // Generate the global variable for this symbol
        asmGenerator.generateDataSectionQuadWordForSymbolPtr(name);
        asmGenerator.generateCStringForSymbol(name);

        asmGenerator.initFunction(name);

        // Bindings
        Node bindingsNode = rlist.nodes().get(2);
        List<Node> bindingsList = RList.expectRList(bindingsNode).nodes();
        int numOperands = bindingsList.size();

        // We need 16 bytes for each operand, but ensure we always reserve a multiple of 16 bytes
        int stackBytes = (int)(16 * Math.ceil(numOperands/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes);

        // TODO think about bindings and scopes ...
        //      Generate code to (i) create new symbol table scope (linked list), then copy each runtime arg (from register)
        //      into matching symbol in that new scope.  E.g. if bindings are (x,y,z) then we set x to x0, y to x1 etc.
        // TODO for now just add into the existing symbol table blindly
        //      e.g. put_symbol from each register for each binding
        //      But we have to first put each arg onto the stack so they're not still in calling convention position, so we can
        //      call into our runtime to add each symbol mapping one-by-one.
        int pos = 0;
        for (Node bindingNode : bindingsList) {
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            System.out.printf("defun: storing binding for %s to stack%n", symbolAtom.value());
            asmGenerator.storeOperandFromRegisterToStack(pos++);
        }

        // TODO we should just leave our bindings on our stack and figure out how to map symbol to stack offset
//        pos = 0;
//        for (Node bindingNode : bindingsList) {
//            Atom symbolAtom = Atom.expectAtom(bindingNode);
//            System.out.println("defun: writing binding from stack to register, then calling _put_symbol for " + symbolAtom.value());
//            // load operand (value ptr) into x1 to leave x0 free for the symbol ptr we'll need ready to call _put_symbol
//            asmGenerator.loadOperandFromStackIntoRegister(pos, 1);
//
////            asmGenerator.putSymbol(symbolAtom.value());
//            pos++;
//        }

        // Function impl
        Node bodyNode = rlist.nodes().get(3);
        treeWalker.walkTree(bodyNode);

        // We need to free an additional 16 bytes for the stored x29 and x30 regs
        asmGenerator.freeSpaceOnStack(stackBytes + 16);

        asmGenerator.endFunction();

        // return the AsmGenerator from new function scope
        // TODO confusing how this differs from endFunction() - doing different things
        asmGenerator.endFunctionDef();

        // associate symbol for this function with the tagged function ptr for it
        //String cStringNameSymPtr = asmGenerator.generateCStringForSymbol(name);

        asmGenerator.putFunction(name);

        // add this function to our compile-time Map
        treeWalker.getFunctions().put(name, new Function("_" + name, name));
    }
}
