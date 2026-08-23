#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "runtime.h"

#define DEBUG 0

struct SymbolEntry {
    char *symbol;
    uintptr_t variableSlot;
    uintptr_t functionSlot;
};

// TODO need to implement a mangling scheme for symbols which we can't represent as C/asm variable names.  We'll hit this
//      pretty early with `+`.

uintptr_t add(uintptr_t val1, uintptr_t val2);

struct SymbolEntry t_sym = {"t", (uintptr_t)&t_sym, (uintptr_t)NULL};
struct SymbolEntry nil_sym = {"nil", (uintptr_t)&nil_sym, (uintptr_t)NULL};
struct SymbolEntry add_sym = {"add", (uintptr_t)NULL, (uintptr_t)&add};

void tag_symbol_val(struct SymbolEntry *symbolEntry) {
    // Check for alignment issues before tagging our static values
    uintptr_t sym_addr = (uintptr_t)symbolEntry;
    if ((sym_addr & 0x7) != 0) {
        printf("Init error: symbol struct for %s not aligned to 8 bytes\n", symbolEntry->symbol);
        exit(-1);
    }

    // Now tag the value of the symbol value
    symbolEntry->variableSlot |= 0x4L;
}

int init() {
    tag_symbol_val(&t_sym);
    tag_symbol_val(&nil_sym);

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
    if (DEBUG == 1) {
        printf("printResult: 0x%lx\n", result);
    }

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

char *printValue(uintptr_t taggedValue) {
    long tagMask = 0x7;
    long tag = taggedValue & tagMask;
    long value = 0L;

    char *resultStr = (char*)malloc(25 * sizeof(char));

    if (tag == 1L) {
        // fixnum
        value = taggedValue >> 3;
        sprintf(resultStr, "%ld", value);
        return resultStr;
    }
    else if (tag == 3L) {
        // closure
        value = taggedValue & 0xFFFFFFFFFFFFFFF8;
        void *closurePtr = (void*)value;
        sprintf(resultStr, "%p", closurePtr);
        return resultStr;
    }
    else if (tag == 4L) {
        // symbol
        value = taggedValue & 0xFFFFFFFFFFFFFFF8;
        char *symbolPtr = (char*)value;
        sprintf(resultStr, "%s", symbolPtr);
        return resultStr;
    }
    else {
        printf("printValue: type error for: 0x%lx\n", taggedValue);
        exit(-1);
    }
}

void typecheck_fixnum(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_FIXNUM) {
        printf("Type error; expect fixnum for value 0x%lx\n", val);
        exit(-1);
    }
}

void typecheck_symbol(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_SYMBOL) {
        printf("Type error; expect symbol for value 0x%lx\n", val);
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

uintptr_t untag_fxn_ptr(uintptr_t taggedFxnPtr) {
     return taggedFxnPtr & 0xFFFFFFFFFFFFFFF8;
}

struct Closure {
    uintptr_t taggedFxnPtr;
    uintptr_t *captures;
};

void *alloc_captures(int capturesLen) {
    return malloc(capturesLen * 8);
}

uintptr_t *add_capture(uintptr_t *capturesPtr, uintptr_t value, int index) {
    // returns capturesPtr* back to caller as we often need it in x0 for adding multiple captures in sequence
    capturesPtr[index] = value;
    if (DEBUG == 1) {
        printf("add_capture: added value %s into captures at index %d resulting in val %s\n", printValue(value), index, printValue(capturesPtr[index]));
    }
    return capturesPtr;
}

uintptr_t mk_closure(uintptr_t taggedFxnPtr, uintptr_t *captures) {
    void *heapPtr = malloc(sizeof(struct Closure));
    struct Closure test = {taggedFxnPtr, captures};
    memcpy(heapPtr, &test, sizeof(struct Closure));

    // tag the heap ptr
    uintptr_t taggedHeapPtr = (uintptr_t)heapPtr;
    taggedHeapPtr = taggedHeapPtr | TYPE_TAG_CLOSURE;

    if (DEBUG == 1) {
        printf("mk_closure: created closure with heapPtr %p, tagged heap ptr 0x%lx\n", heapPtr, taggedHeapPtr);
    }
    return taggedHeapPtr;
}

uintptr_t deref_tagged_closure_fxn_ptr(uintptr_t taggedClosurePtr) {
    void* untaggedClosurePtr = (void*)untag_fxn_ptr(taggedClosurePtr);
    struct Closure *closure = (struct Closure*)untaggedClosurePtr;
    uintptr_t taggedFxnPtr = closure->taggedFxnPtr;
    uintptr_t rawFxnPtr = untag_fxn_ptr(taggedFxnPtr);
    if (DEBUG == 1) {
        printf("deref_tagged_closure_fxn_ptr: got raw fxn ptr: 0x%lx\n", rawFxnPtr);
    }
    return rawFxnPtr;
}

uintptr_t load_captured_variable(uintptr_t taggedClosurePtr, int index) {
    void* untaggedClosurePtr = (void*)untag_fxn_ptr(taggedClosurePtr);
    struct Closure *closure = (struct Closure*)untaggedClosurePtr;
    uintptr_t *capturesPtr = closure->captures;
    return capturesPtr[index];
}
