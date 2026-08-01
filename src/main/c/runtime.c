#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "runtime.h"

struct SymbolEntry {
    char *symbol;
    void *variableSlot;
    void *functionSlot;
};

// TODO need to implement a mangling scheme for symbols which we can't represent as C/asm variable names.  We'll hit this
//      pretty early with `+`.

uintptr_t add(uintptr_t val1, uintptr_t val2);

struct SymbolEntry t_sym = {"t", &t_sym, NULL};
struct SymbolEntry nil_sym = {"nil", &nil_sym, NULL};
struct SymbolEntry add_sym = {"add", NULL, &add};

uintptr_t createTaggedSymbolPtr(char *symbolName) {
    // init symbol tagged pointer
    char *sym_on_heap = strdup(symbolName);
    uintptr_t symbol_ptr = (uintptr_t)sym_on_heap;
    return symbol_ptr | 0x4;
}

// TODO needs adapting for non-self-evaluating symbols
//      Need to think a bit as we pass in raw symbol string which we'll tag; the value should probably be tagged already,
//      so we end up with tagging in the caller and here too
// void addSymbol(uintptr_t taggedSymbolPtr, uintptr_t taggedVariablePtr, uintptr_t taggedFxnPtr) {
//     struct SymbolEntry symbol_entry;
//     symbol_entry.symbol = taggedSymbolPtr;
//     symbol_entry.variableSlot = taggedVariablePtr;
//     symbol_entry.functionSlot = taggedFxnPtr;
//     symbolTable[sym_size++] = symbol_entry;
// }


// void addFunction(char *name, uintptr_t (*raw_fxn_ptr)(uintptr_t, uintptr_t)) {
//     // symbol for this function
//     add_symbol_ptr = createTaggedSymbolPtr(name);
//
//     // prep. tagged function pointer
//     uintptr_t add_fxn_ptr = (uintptr_t)raw_fxn_ptr;
//     add_fxn_ptr = add_fxn_ptr | 0x4;
//
//     // add to symbol table
//     addSymbol(add_symbol_ptr, (uintptr_t)NULL, add_fxn_ptr);
// }

int init() {
//     // allocate symbol table
//     symbolTable = malloc(sym_capacity * sizeof(struct SymbolEntry));
//
//     // create built-in symbols
//     t_symbol_ptr = createTaggedSymbolPtr("t");
//     addSymbol(t_symbol_ptr, t_symbol_ptr, (uintptr_t)NULL);
//
//     nil_symbol_ptr = createTaggedSymbolPtr("nil");
//     addSymbol(nil_symbol_ptr, nil_symbol_ptr, (uintptr_t)NULL);
//
//     // create symbol for `add`
//     // duplicate code for `+`
//     addFunction("add", &add);
//     addFunction("+", &add);

    return 0;
}

RUNTIME_TYPE determineType(uintptr_t taggedVal) {
    long tag = taggedVal & TYPE_MASK;

    if (tag == TYPE_TAG_FIXNUM) {
        return TYPE_FIXNUM;
    } else if (tag == TYPE_TAG_SYMBOL) {
        return TYPE_SYMBOL;
    } else {
        return TYPE_UNKNOWN;
    }
}

void printResult(uintptr_t result) {
    printf("printResult: %lu\n", result);
    // result is a tagged pointer
    long tagMask = 0x7;
    long tag = result & tagMask;
    long value = 0L;

    if (tag == 1L) {
        // fixnum
        value = result >> 3;
        printf("%ld\n", value);
    }
    else if (tag == 4L) {
        // symbol
        value = result & 0xFFFFFFFFFFFFFFF8;
        char *symbolPtr = (char*)value;
        printf("%s\n", symbolPtr);
    }
    else {
        printf("printResult: type error for: 0x%lx\n", result);
        exit(-1);
    }
}

void typecheck_fixnum(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_FIXNUM) {
        printf("Type error; expect fixnum for value %ld\n", val);
        exit(-1);
    }
}

void typecheck_symbol(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_SYMBOL) {
        printf("Type error; expect symbol for value %ld\n", val);
        exit(-1);
    }
}

uintptr_t tag_symbol_val(void *symbol_ptr) {
    return (uintptr_t)symbol_ptr | 0x4;
}

long tagged_ptr_to_fixnum(uintptr_t val) {
    typecheck_fixnum(val);
    return val >> 3;
}

uintptr_t add(uintptr_t val1, uintptr_t val2) {
    long raw1 = tagged_ptr_to_fixnum(val1);
    long raw2 = tagged_ptr_to_fixnum(val2);

    long result = raw1 + raw2;

    uintptr_t res = ((uintptr_t)result << 3) | 0x1;
    return res;
}

// uintptr_t get_t() {
//     uintptr_t t_symbol_ptr = symbolTable[0].symbol;
//     return t_symbol_ptr;
// }

// returns 0 if val is t; for example allows a subsequent cbz or cbnz instruction to react to zero when the last result
// was t.
// This is made easier since we intern t by always using the same pointer for it.
long is_t(uintptr_t val) {
    void *untagged_val = (void*)(val & 0xFFFFFFFFFFFFFFF8);
    if (untagged_val == &t_sym) {
        return 0;
    }
    return -1;
}

uintptr_t evaluate_symbol(uintptr_t taggedPtr, NAMESPACE_TYPE namespaceType) {
//     char *namespaceTypeStr = namespaceType == NAMESPACE_VARIABLE ? "Variable" : "Function";
//     printf("evaluate_symbol: taggedPtr: %lu, namespaceType: %s\n", taggedPtr, namespaceTypeStr);
//
//     RUNTIME_TYPE rType = determineType(taggedPtr);
//     if (rType == TYPE_UNKNOWN) {
//         printf("Unknown type for likely untagged symbol ptr %ld\n", taggedPtr);
//         exit(-1);
//     }
//
//     // We can trust taggedPtr without type-checking it, as it was loaded from a well-defined variable which must have previously been set by our runtime
//     for (int i=0; i<sym_size; i++) {
//         uintptr_t this_symbol_ptr = symbolTable[i].symbol;
//         if (taggedPtr == this_symbol_ptr) {
//             return namespaceType == NAMESPACE_VARIABLE ? symbolTable[i].variableSlot : symbolTable[i].functionSlot;
//         }
//     }

    // We created the variable for this symbol but it's not in the symbol table, programming error
    printf("Could not find symbol for tagged symbol ptr %ld\n", taggedPtr);
    exit(-1);
}

// Similar to evaluate_symbol but lenient if not exists in symbol table for cases where need to check existence as a valid case
uintptr_t symbol_exists(uintptr_t taggedPtr) {
    // We can trust taggedPtr without type-checking it, as it was loaded from a well-defined variable which must have previously been set by our runtime
//     for (int i=0; i<sym_size; i++) {
//         uintptr_t this_symbol_ptr = symbolTable[i].symbol;
//         if (taggedPtr == this_symbol_ptr) {
//             return symbolTable[i].variableSlot;
//         }
//     }

    return (uintptr_t)NULL;
}

void put_symbol(uintptr_t taggedSymbolPtr, uintptr_t taggedValuePtr) {
//     printf("put_symbol: taggedSymbolPtr: %lu, taggedValuePtr: %lu\n", taggedSymbolPtr, taggedValuePtr);
//
//     // Assumption that we've already checked this symbol entry doesn't already exist, so we just add the entry to the next slot
//     struct SymbolEntry symbol_entry;
//     symbol_entry.symbol = taggedSymbolPtr;
//     symbol_entry.variableSlot = taggedValuePtr;
//     symbolTable[sym_size++] = symbol_entry;
}

void put_function(char *rawSymbol, uintptr_t rawFunctionPtr) {
    // TODO keep it v. simple and just add to the end of the symbol table
//     printf("put_function: raw symbol: %s, raw function ptr: %lu\n", rawSymbol, rawFunctionPtr);
//
//     // init symbol tagged pointer; needed for now to ensure we're aligned to 8 bytes to make tagging work
//     // TODO drawback here is always re-alloc'ing the string; problem when redefining as we'll have multiple of same symbol
//     //      Needs interning somehow.
//     char *sym_on_heap = strdup(rawSymbol);
//     uintptr_t symbol_ptr = (uintptr_t)sym_on_heap;
//     symbol_ptr = symbol_ptr | TYPE_TAG_SYMBOL;
//
//     // tag the function pointer - let's just go with a tag of 2 for now.
//     // We can assume it's aligned to 8 bytes due to the alignment we always use for user-defined functions
//     uintptr_t tagged_fxn_ptr = rawFunctionPtr | TYPE_TAG_FUNCTION;
//
//     // put tagged t pointer to first two slots of symbol table (symbol and its value in variable namespace)
//     printf("put_function: making symbol entry for %s, tagged symbol ptr: %lu, tagged function ptr: %lu\n", rawSymbol, symbol_ptr, tagged_fxn_ptr);
//     struct SymbolEntry symbol_entry;
//     symbol_entry.symbol = symbol_ptr;
//     symbol_entry.functionSlot = tagged_fxn_ptr;
//     symbolTable[sym_size++] = symbol_entry;
}

uintptr_t get_add_function_ptr() {
    // untag
//     uintptr_t taggedFxnPtr = symbolTable[2].functionSlot;  // for now hardcoding to `add`
//     return taggedFxnPtr & 0xFFFFFFFFFFFFFFF8;
    return ((uintptr_t)(add_sym.symbol)) & 0xFFFFFFFFFFFFFFF8;
}

uintptr_t untag_fxn_ptr(uintptr_t taggedFxnPtr) {
     return taggedFxnPtr & 0xFFFFFFFFFFFFFFF8;
}

