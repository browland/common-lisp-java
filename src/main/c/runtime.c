#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "runtime.h"

int sym_capacity = 100;
int sym_size = 0;  // todo will eventually need

uintptr_t t_symbol_ptr;

// We use uintptr_t due to tagged pointers.  We can't modify a char* for example by tagging it, so we fall back to raw uintptr_t.
struct SymbolEntry {
    uintptr_t symbol;
    uintptr_t variableSlot;
    uintptr_t functionSlot;
};

struct SymbolEntry *symbolTable = NULL;

int init() {
    // allocate symbol table
    symbolTable = malloc(sym_capacity * sizeof(struct SymbolEntry));

    char *t_on_heap = strdup("t");

    // init "t" symbol tagged pointer
    t_symbol_ptr = (uintptr_t)t_on_heap;
    t_symbol_ptr = t_symbol_ptr | 0x4;
    
    // put tagged t pointer to first two slots of symbol table (symbol and its value in variable namespace)
    struct SymbolEntry t_symbol_entry;
    t_symbol_entry.symbol = t_symbol_ptr;
    t_symbol_entry.variableSlot = t_symbol_ptr;
    symbolTable[0] = t_symbol_entry;

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
        printf("Type error; expect fixnum for value %ld", val);
        exit(-1);
    }
}

void typecheck_symbol(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_SYMBOL) {
        printf("Type error; expect symbol for value %ld", val);
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

