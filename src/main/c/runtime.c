#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "runtime.h"

int sym_capacity = 100;
int sym_size = 0;

uintptr_t t_symbol_ptr;
uintptr_t nil_symbol_ptr;
uintptr_t add_symbol_ptr;

// We use uintptr_t due to tagged pointers.  We can't modify a char* for example by tagging it, so we fall back to raw uintptr_t.
struct SymbolEntry {
    uintptr_t symbol;
    uintptr_t variableSlot;
    uintptr_t functionSlot;
};

struct SymbolEntry *symbolTable = NULL;

uintptr_t createTaggedSymbolPtr(char *symbolName) {
    // init symbol tagged pointer
    char *sym_on_heap = strdup(symbolName);
    uintptr_t symbol_ptr = (uintptr_t)sym_on_heap;
    return symbol_ptr | 0x4;
}

// TODO needs adapting for non-self-evaluating symbols
//      Need to think a bit as we pass in raw symbol string which we'll tag; the value should probably be tagged already,
//      so we end up with tagging in the caller and here too
void addSymbol(uintptr_t taggedSymbolPtr, uintptr_t taggedVariablePtr, uintptr_t taggedFxnPtr) {
    struct SymbolEntry symbol_entry;
    symbol_entry.symbol = taggedSymbolPtr;
    symbol_entry.variableSlot = taggedVariablePtr;
    symbol_entry.functionSlot = taggedFxnPtr;
    symbolTable[sym_size++] = symbol_entry;
}


uintptr_t add(uintptr_t val1, uintptr_t val2);
int init() {
    // allocate symbol table
    symbolTable = malloc(sym_capacity * sizeof(struct SymbolEntry));

    // create built-in symbols
    t_symbol_ptr = createTaggedSymbolPtr("t");
    addSymbol(t_symbol_ptr, t_symbol_ptr, (uintptr_t)NULL);

    nil_symbol_ptr = createTaggedSymbolPtr("nil");
    addSymbol(nil_symbol_ptr, nil_symbol_ptr, (uintptr_t)NULL);

    // create function entry for `add`
    add_symbol_ptr = createTaggedSymbolPtr("add");
    uintptr_t (*raw_fxn_ptr)(uintptr_t, uintptr_t);  // raw function pointer
    raw_fxn_ptr = &add;
    uintptr_t add_fxn_ptr = (uintptr_t)raw_fxn_ptr;
    add_fxn_ptr = add_fxn_ptr | 0x4;                // tag it
    addSymbol(add_symbol_ptr, (uintptr_t)NULL, add_fxn_ptr);

    return 0;
}

// before tagging function ptr        0x000000016fdfef98
// after calling get_add_function_ptr 0x000000016fdfef98

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

uintptr_t get_t() {
    uintptr_t t_symbol_ptr = symbolTable[0].symbol;
    return t_symbol_ptr;
}

// returns 0 if val is t; for example allows a subsequent cbz or cbnz instruction to react to zero when the last result
// was t.
// This is made easier since we intern t by always using the same pointer for it.
long is_t(uintptr_t val) {
    if (val == t_symbol_ptr) {
        return 0;
    }
    return -1;
}

uintptr_t evaluate_symbol(uintptr_t taggedPtr) {
    // We can trust taggedPtr without type-checking it, as it was loaded from a well-defined variable which must have previously been set by our runtime
    for (int i=0; i<sym_size; i++) {
        uintptr_t this_symbol_ptr = symbolTable[i].symbol;
        if (taggedPtr == this_symbol_ptr) {
            return symbolTable[i].variableSlot;
        }
    }

    // We created the variable for this symbol but it's not in the symbol table, programming error
    printf("Could not find symbol for tagged symbol ptr %ld\n", taggedPtr);
    exit(-1);
}

// Similar to evaluate_symbol but lenient if not exists in symbol table for cases where need to check existence as a valid case
uintptr_t symbol_exists(uintptr_t taggedPtr) {
    // We can trust taggedPtr without type-checking it, as it was loaded from a well-defined variable which must have previously been set by our runtime
    for (int i=0; i<sym_size; i++) {
        uintptr_t this_symbol_ptr = symbolTable[i].symbol;
        if (taggedPtr == this_symbol_ptr) {
            return symbolTable[i].variableSlot;
        }
    }

    return (uintptr_t)NULL;
}

void put_symbol(uintptr_t taggedSymbolPtr, uintptr_t valuePtr) {
    // Assumption that we've already checked this symbol entry doesn't already exist, so we just add the entry to the next slot
    struct SymbolEntry symbol_entry;
    symbol_entry.symbol = taggedSymbolPtr;
    symbol_entry.variableSlot = valuePtr;
    symbolTable[sym_size++] = symbol_entry;
}

void put_function(char *rawSymbol, uintptr_t rawFunctionPtr) {
    // TODO keep it v. simple and just add to the end of the symbol table
    printf("raw symbol: %s\n", rawSymbol);
    printf("raw function ptr: %lu\n", rawFunctionPtr);

    // init symbol tagged pointer
    char *sym_on_heap = strdup(rawSymbol);
    uintptr_t symbol_ptr = (uintptr_t)sym_on_heap;
    symbol_ptr = symbol_ptr | 0x4;  // TODO use runtime.h constant

    // tag the function pointer - let's just go with a tag of 2 for now.
    // We can assume it's aligned to 8 bytes due to the alignment we always use for user-defined functions
    uintptr_t tagged_fxn_ptr = rawFunctionPtr | 0x2;  // TODO put in runtime.h when happy

    // put tagged t pointer to first two slots of symbol table (symbol and its value in variable namespace)
    struct SymbolEntry symbol_entry;
    symbol_entry.symbol = symbol_ptr;
    symbol_entry.functionSlot = tagged_fxn_ptr;
    symbolTable[sym_size++] = symbol_entry;
}

uintptr_t get_add_function_ptr() {
    // untag
    uintptr_t taggedFxnPtr = symbolTable[2].functionSlot;  // for now hardcoding to `add`
    return taggedFxnPtr & 0xFFFFFFFFFFFFFFF8;
}

