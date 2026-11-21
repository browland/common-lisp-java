#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "runtime.h"
#include <stdbool.h>

#define DEBUG 0

struct SymbolEntry {
    char *symbol;
    uintptr_t variableSlot;
    uintptr_t functionSlot;
};

struct ConsCell {
    uintptr_t car;
    uintptr_t cdr;  // tagged ptr to next element which may be another ConsCell, nil (end of list), or any other value
};

struct Closure {
    void *fxnPtr;
    uintptr_t *captures;
    char *debugString;
};

struct Function {
    char *name;
    uintptr_t rawFxnPtr;
};

// Only the ptr to the Object struct is tagged; the contents of the struct don't need tagging so we use raw pointers etc within.
struct Object {
    long type;  // Maps to OBJ_TYPE_...
    void *rawPtr;
};

// Forward references for built-in functions - these are just statically defined as C functions and directly referenced
// in the symbol table provided at runtime.
uintptr_t add(uintptr_t val1, uintptr_t val2);
uintptr_t minus(uintptr_t val1, uintptr_t val2);
uintptr_t mod(uintptr_t numberTaggedFixnum, uintptr_t modulusTaggedFixnum);
uintptr_t cons(uintptr_t car, uintptr_t cdr);
uintptr_t list(uintptr_t *args, long numArgs);
uintptr_t car(uintptr_t taggedCons);
uintptr_t cdr(uintptr_t taggedCons);
uintptr_t mapcar(uintptr_t functionObj, uintptr_t taggedCons);
uintptr_t oneplus(uintptr_t val);
uintptr_t eq(uintptr_t val1, uintptr_t val2);
uintptr_t gt(uintptr_t val1, uintptr_t val2);
uintptr_t open(uintptr_t fileSpec);
uintptr_t close(uintptr_t taggedStreamObj);
uintptr_t readLine(uintptr_t taggedStreamObj, uintptr_t terminateResult);
uintptr_t format(uintptr_t* args, long numArgs);
uintptr_t assoc(uintptr_t taggedKey, uintptr_t taggedCons);
uintptr_t funcall(uintptr_t* args, long numArgs);
uintptr_t subseq(uintptr_t* args, long numArgs);
uintptr_t stringequal(uintptr_t taggedStr1, uintptr_t taggedStr2);

// Function objects
struct Function add_fxn = {
    "+",
    (uintptr_t)&add
};

struct Function minus_fxn = {
    "-",
    (uintptr_t)&minus
};

struct Function mod_fxn = {
    "mod",
    (uintptr_t)&mod
};

struct Function cons_fxn = {
    "cons",
    (uintptr_t)&cons
};

struct Function list_fxn = {
    "list",
    (uintptr_t)&list
};

struct Function car_fxn = {
    "car",
    (uintptr_t)&car
};

struct Function cdr_fxn = {
    "cdr",
    (uintptr_t)&cdr
};

struct Function mapcar_fxn = {
    "mapcar",
    (uintptr_t)&mapcar
};

struct Function oneplus_fxn = {
    "1+",
    (uintptr_t)&oneplus
};

struct Function eq_fxn = {
    "eq",
    (uintptr_t)&eq
};

struct Function open_fxn = {
    "open",
    (uintptr_t)&open
};

struct Function close_fxn = {
    "close",
    (uintptr_t)&close
};

struct Function readLine_fxn = {
    "readLine",
    (uintptr_t)&readLine
};

struct Function format_fxn = {
    "format",
    (uintptr_t)&format
};

struct Function gt_fxn = {
    ">",
    (uintptr_t)&gt
};

struct Function assoc_fxn = {
    "assoc",
    (uintptr_t)&assoc
};

struct Function funcall_fxn = {
    "funcall",
    (uintptr_t)&funcall
};

struct Function subseq_fxn = {
    "subseq",
    (uintptr_t)&subseq
};

struct Function stringequal_fxn = {
    "string=",
    (uintptr_t)&stringequal
};

// Populate symbol table with symbol values
struct SymbolEntry t_sym = {"t", (uintptr_t)&t_sym, (uintptr_t)NULL};
struct SymbolEntry nil_sym = {"nil", (uintptr_t)&nil_sym, (uintptr_t)NULL};

// Populate symbol table with functions
// We need to tag them using + rather than | so we achieve compile-time constants
struct SymbolEntry CL_2B_sym = {"+", (uintptr_t)NULL, (uintptr_t)&add_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry CL_2D_sym = {"-", (uintptr_t)NULL, (uintptr_t)&minus_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry mod_sym = {"mod", (uintptr_t)NULL, (uintptr_t)&mod_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry cons_sym = {"cons", (uintptr_t)NULL, (uintptr_t)&cons_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry list_sym = {"list", (uintptr_t)NULL, (uintptr_t)&list_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry car_sym = {"car", (uintptr_t)NULL, (uintptr_t)&car_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry cdr_sym = {"cdr", (uintptr_t)NULL, (uintptr_t)&cdr_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry mapcar_sym = {"mapcar", (uintptr_t)NULL, (uintptr_t)&mapcar_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry CL_312B_sym = {"1+", (uintptr_t)NULL, (uintptr_t)&oneplus_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry CL_3E_sym = {">", (uintptr_t)NULL, (uintptr_t)&gt_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry eq_sym = {"eq", (uintptr_t)NULL, (uintptr_t)&eq_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry open_sym = {"open", (uintptr_t)NULL, (uintptr_t)&open_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry close_sym = {"close", (uintptr_t)NULL, (uintptr_t)&close_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry readLine_sym = {"read-line", (uintptr_t)NULL, (uintptr_t)&readLine_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry format_sym = {"format", (uintptr_t)NULL, (uintptr_t)&format_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry assoc_sym = {"assoc", (uintptr_t)NULL, (uintptr_t)&assoc_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry funcall_sym = {"funcall", (uintptr_t)NULL, (uintptr_t)&funcall_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry subseq_sym = {"subseq", (uintptr_t)NULL, (uintptr_t)&subseq_fxn + TYPE_TAG_FUNCTION};
struct SymbolEntry CL_737472696E673D_sym = {"string=", (uintptr_t)NULL, (uintptr_t)&stringequal_fxn + TYPE_TAG_FUNCTION};

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
    } else if (tag == TYPE_TAG_FUNCTION) {
        return TYPE_FUNCTION;
    } else if (tag == TYPE_TAG_CONS) {
        return TYPE_CONS;
    } else if (tag == TYPE_TAG_STR) {
        return TYPE_STR;
    } else if (tag == TYPE_TAG_OBJ) {
        return TYPE_OBJ;
    } else if (tag == TYPE_TAG_CLOSURE) {
        return TYPE_CLOSURE;
    } else {
        return TYPE_UNKNOWN;
    }
}

// forward declaration needed for printValue()
char *get_closure_debugString(uintptr_t taggedClosurePtr);

char *printValue(uintptr_t taggedValue, bool useQuotes) {
    long tagMask = 0x7;
    long tag = taggedValue & tagMask;
    long value = 0L;

    if (tag == TYPE_TAG_FIXNUM) {
        // TODO Be careful with string termination at 255 chars
        char *resultStr = (char*)malloc(255 * sizeof(char));
        value = taggedValue >> 3;
        sprintf(resultStr, "%ld", value);
        return resultStr;
    }
    else if (tag == TYPE_TAG_CLOSURE ) {
        // TODO Be careful with string termination at 255 chars
        char *resultStr = (char*)malloc(255 * sizeof(char));
        char *debugString = get_closure_debugString(taggedValue);
        value = taggedValue & 0xFFFFFFFFFFFFFFF8;
        sprintf(resultStr, "#<FUNCTION %s 0x%lx\n", debugString, value);
        return resultStr;
    }
    else if (tag == TYPE_TAG_FUNCTION) {
        // TODO Be careful with string termination at 255 chars
        char *resultStr = (char*)malloc(255 * sizeof(char));
        uintptr_t untaggedPtr = taggedValue & 0xFFFFFFFFFFFFFFF8;
        struct Function *functionPtr = (struct Function*)untaggedPtr;
        sprintf(resultStr, "#<FUNCTION %s>\n", functionPtr->name);
        return resultStr;
    }
    else if (tag == TYPE_TAG_SYMBOL) {
        // TODO Be careful with string termination at 255 chars
        char *resultStr = (char*)malloc(255 * sizeof(char));
        struct SymbolEntry *symEntry = (struct SymbolEntry*)(taggedValue & 0xFFFFFFFFFFFFFFF8);
        sprintf(resultStr, "%s", symEntry->symbol);
        return resultStr;
    }
    else if (tag == TYPE_TAG_CONS) {
        value = taggedValue & 0xFFFFFFFFFFFFFFF8;
        void *valuePtr = (void*)value;
        struct ConsCell *c = (struct ConsCell*)valuePtr;
        uintptr_t car = c->car;
        uintptr_t cdr = c->cdr;
        char *carStr = printValue(car, true);
        char *cdrStr = printValue(cdr, true);

        // Allocate result buffer which is size of car and cdr, plus the enclosing brackets, spaces and dot and null char.
        size_t resultSize = strlen(carStr) + strlen(cdrStr) + 5 +1;
        char *resultStr = malloc(resultSize * sizeof(char));

        sprintf(resultStr, "(%s . %s)", carStr, cdrStr);
        return resultStr;
    }
    else if (tag == TYPE_TAG_STR) {
        // TODO Be careful with string termination at 255 chars
        char *resultStr = (char*)malloc(255 * sizeof(char));
        char *rawString = (char*)(taggedValue & 0xFFFFFFFFFFFFFFF8);
        if (useQuotes) {
            sprintf(resultStr, "\"%s\"", rawString);
        }
        else {
            sprintf(resultStr, "%s", rawString);
        }
        return resultStr;
    }
    else {
        printf("printValue: type error for: 0x%lx\n", taggedValue);
        exit(-1);
    }
}

void printResult(uintptr_t result) {
    if (DEBUG == 1) {
        printf("printResult: 0x%lx\n", result);
    }

    if (result == 0x0) {
        printf("printResult: trying to print null! Possibly an empty program as the init() function returns 0.\n");
        exit(-1);
    }

    // result is a tagged pointer
    long tagMask = 0x7;
    long tag = result & tagMask;
    long value = 0L;

    if (tag == TYPE_TAG_FIXNUM) {
        // Cast result to long before shifting so it's treated as a signed type, so we get correct negative values.
        value = (long)result >> 3;
        printf("%ld\n", value);
    }
    else if (tag == TYPE_TAG_SYMBOL) {
        struct SymbolEntry *symEntry = (struct SymbolEntry*)(result & 0xFFFFFFFFFFFFFFF8);
        printf("%s\n", symEntry->symbol);
    }
    else if (tag == TYPE_TAG_CONS) {
        value = result & 0xFFFFFFFFFFFFFFF8;
        void *valuePtr = (void*)value;
        struct ConsCell *c = (struct ConsCell*)valuePtr;
        uintptr_t car = c->car;
        uintptr_t cdr = c->cdr;
        char *carStr = printValue(car, true);
        char *cdrStr = printValue(cdr, true);
        printf("(%s . %s)\n", carStr, cdrStr);
    }
    else if (tag == TYPE_TAG_FUNCTION) {
        uintptr_t untaggedPtr = result & 0xFFFFFFFFFFFFFFF8;
        struct Function *functionPtr = (struct Function*)untaggedPtr;
        printf("#<FUNCTION %s>\n", functionPtr->name);
    }
    else if (tag == TYPE_TAG_CLOSURE) {
        char *debugString = get_closure_debugString(result);
        char *hexMemoryAddress = (char*)malloc(25 * sizeof(char));
        uintptr_t untaggedClosurePtr = result & 0xFFFFFFFFFFFFFFF8;
        sprintf(hexMemoryAddress, "%lx",  untaggedClosurePtr);
        printf("#<FUNCTION %s {%s}\n", debugString, hexMemoryAddress);
    }
    else if (tag == TYPE_TAG_STR) {
        char *rawString = (char*)(result & 0xFFFFFFFFFFFFFFF8);
        printf("\"%s\"\n", rawString);

    }
    else if (tag == TYPE_TAG_OBJ) {
        uintptr_t untaggedPtr = result & 0xFFFFFFFFFFFFFFF8;
        struct Object *objectPtr = (struct Object*)untaggedPtr;
        if (objectPtr->type == OBJ_TYPE_STREAM) {
            printf("#<STREAM %p>\n", objectPtr->rawPtr);
        }
        else {
            printf("printResult: unknown object type: %ld\n", objectPtr->type);
            exit(-1);
        }
    }
    else {
        printf("printResult: type error for: 0x%lx\n", result);
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

    // Remember TYPE_SYMBOL is numbered differently than TYPE_TAG_SYMBOL, etc.!  The former is like an enum whereas the
    // latter is the actual tag we use.
    if (type != TYPE_SYMBOL) {
        printf("Type error; expect symbol for value 0x%lx\n", val);
        exit(-1);
    }
}

void typecheck_function(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_FUNCTION) {
        printf("Type error; expect function for value 0x%lx\n", val);
        exit(-1);
    }
}

void typecheck_cons(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_CONS) {
        printf("Type error; expect cons for value %s\n", printValue(val, false));
        exit(-1);
    }
}

void typecheck_string(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_STR) {
        printf("Type error; expect string for value 0x%lx\n", val);
        exit(-1);
    }
}

void typecheck_obj(uintptr_t val) {
    RUNTIME_TYPE type = determineType(val);

    if (type != TYPE_OBJ) {
        printf("Type error; expect object for value 0x%lx\n", val);
        exit(-1);
    }
}

long tagged_ptr_to_fixnum(uintptr_t val) {
    typecheck_fixnum(val);
    return (long)val >> 3;
}

uintptr_t add(uintptr_t val1, uintptr_t val2) {
    long raw1 = tagged_ptr_to_fixnum(val1);
    long raw2 = tagged_ptr_to_fixnum(val2);

    long result = raw1 + raw2;

    uintptr_t res = ((uintptr_t)result << 3) | 0x1;
    return res;
}

uintptr_t minus(uintptr_t val1, uintptr_t val2) {
    long raw1 = tagged_ptr_to_fixnum(val1);
    long raw2 = tagged_ptr_to_fixnum(val2);

    long result = raw1 - raw2;

    uintptr_t res = ((uintptr_t)result << 3) | 0x1;
    return res;
}

uintptr_t mod(uintptr_t numberTaggedFixnum, uintptr_t modulusTaggedFixnum) {
    long rawNumber = tagged_ptr_to_fixnum(numberTaggedFixnum);
    long rawModulus = tagged_ptr_to_fixnum(modulusTaggedFixnum);

    long result = rawNumber % rawModulus;

    // Quirk of Common Lisp - if the result is negative, then we subtract if from the modulus (essentially going round
    // the ring clockwise) so we never get a negative result from mod.
    if (result < 0) {
        result = rawModulus + result;  // result is negative
    }

    uintptr_t res = ((uintptr_t)result << 3) | 0x1;
    return res;
}

// returns 0 if val is t; for example allows a subsequent cbz or cbnz instruction to react to zero when the last result
// was t.
// This is made easier since we intern t by always using the same pointer for it.
long is_t(uintptr_t taggedVal) {
    void *untagged_val = (void*)(taggedVal & 0xFFFFFFFFFFFFFFF8);
    if (untagged_val == &t_sym) {
        return 0;
    }
    return -1;
}

uintptr_t untag_ptr(uintptr_t taggedPtr) {
     return taggedPtr & 0xFFFFFFFFFFFFFFF8;
}

void *alloc_captures(int capturesLen) {
    return malloc(capturesLen * 8);
}

uintptr_t *add_capture(uintptr_t *capturesPtr, uintptr_t value, int index) {
    // returns capturesPtr* back to caller as we often need it in x0 for adding multiple captures in sequence
    capturesPtr[index] = value;
    if (DEBUG == 1) {
        printf("add_capture: added value %s into captures at index %d resulting in val %s\n", printValue(value, true), index, printValue(capturesPtr[index], true));
    }
    return capturesPtr;
}

uintptr_t mk_closure(void *fxnPtr, uintptr_t *captures, char *debugString) {
    void *heapPtr = malloc(sizeof(struct Closure));
    struct Closure test = {fxnPtr, captures, debugString};
    memcpy(heapPtr, &test, sizeof(struct Closure));

    // tag the heap ptr
    uintptr_t taggedHeapPtr = (uintptr_t)heapPtr;
    taggedHeapPtr = taggedHeapPtr | TYPE_TAG_CLOSURE;

    if (DEBUG == 1) {
        printf("mk_closure: created closure with heapPtr %p, tagged heap ptr 0x%lx\n", heapPtr, taggedHeapPtr);
    }
    return taggedHeapPtr;
}

void *tagged_closure_ptr_to_fxn_ptr(uintptr_t taggedClosurePtr) {
    void* untaggedClosurePtr = (void*)untag_ptr(taggedClosurePtr);
    struct Closure *closure = (struct Closure*)untaggedClosurePtr;
    void *fxnPtr = closure->fxnPtr;
    if (DEBUG == 1) {
        printf("tagged_closure_ptr_to_fxn_ptr: got raw fxn ptr: 0x%px\n", fxnPtr);
    }
    return fxnPtr;
}

uintptr_t load_captured_variable(uintptr_t taggedClosurePtr, int index) {
    void* untaggedClosurePtr = (void*)untag_ptr(taggedClosurePtr);
    struct Closure *closure = (struct Closure*)untaggedClosurePtr;
    uintptr_t *capturesPtr = closure->captures;
    return capturesPtr[index];
}

char *get_closure_debugString(uintptr_t taggedClosurePtr) {
    void* untaggedClosurePtr = (void*)untag_ptr(taggedClosurePtr);
    struct Closure *closure = (struct Closure*)untaggedClosurePtr;
    return closure->debugString;
}

uintptr_t cons(uintptr_t car, uintptr_t cdr) {
    struct ConsCell cons = {car, cdr};
    void *heapPtr = malloc(sizeof(struct ConsCell));
    memcpy(heapPtr, &cons, sizeof(struct ConsCell));

    // tag the ptr to return our value
    uintptr_t taggedHeapPtr = (uintptr_t)heapPtr;
    taggedHeapPtr = taggedHeapPtr | TYPE_TAG_CONS;

    return taggedHeapPtr;
}

uintptr_t list(uintptr_t *args, long numArgs) {
    void *consHeadPtr = malloc(sizeof(struct ConsCell) * numArgs);

    for (int i = 0; i < numArgs; i++) {
        uintptr_t thisConsPtr = (uintptr_t)consHeadPtr + (i * sizeof(struct ConsCell));
        uintptr_t nextConsPtr = thisConsPtr + sizeof(struct ConsCell);

        uintptr_t cdr;
        if (i < numArgs-1) {
            cdr = nextConsPtr | TYPE_TAG_CONS;
        }
        else {
            cdr = nil_sym.variableSlot;
        }

        uintptr_t car = args[i];
        struct ConsCell cons = {car, cdr};

        memcpy((void*)thisConsPtr, &cons, sizeof(struct ConsCell));
    }

    return ((uintptr_t)consHeadPtr) | TYPE_TAG_CONS;
}

uintptr_t car(uintptr_t taggedCons) {
    typecheck_cons(taggedCons);
    void* untaggedConsPtr = (void*)untag_ptr(taggedCons);
    struct ConsCell *cons = (struct ConsCell*)untaggedConsPtr;

    return cons->car;
}

uintptr_t cdr(uintptr_t taggedCons) {
    typecheck_cons(taggedCons);
    void* untaggedConsPtr = (void*)untag_ptr(taggedCons);
    struct ConsCell *cons = (struct ConsCell*)untaggedConsPtr;

    return cons->cdr;
}

// Function pointer type for any function taking one uintptr_t and returning uintptr_t.
typedef uintptr_t (*FuncType)(uintptr_t);

uintptr_t mapcar(uintptr_t taggedFunctionObj, uintptr_t taggedCons) {
    typecheck_function(taggedFunctionObj);
    typecheck_cons(taggedCons);

    void* untaggedConsPtr = (void*)untag_ptr(taggedCons);
    struct ConsCell *cons = (struct ConsCell*)untaggedConsPtr;

    void *untaggedFunctionObjPtr = (void*)untag_ptr(taggedFunctionObj);
    struct Function *function = (struct Function*)untaggedFunctionObjPtr;
    void* rawFxnPtr = (void*)function->rawFxnPtr;

    // Get function pointer
    FuncType func = (FuncType)rawFxnPtr;

    // We don't know how long the list is.  For now we'll assume no more than 10 ...
    uintptr_t *resultListPtr = (uintptr_t*)malloc(sizeof(uintptr_t) * 10);
    uintptr_t car;

    int numArgs = 0;
    for (int i=0; i<10; i++) {
        car = cons->car;

        uintptr_t res = func(car);

        resultListPtr[i] = res;

        numArgs++;

        uintptr_t cdr = cons->cdr;
        RUNTIME_TYPE cdrType = determineType(cdr);
        if (cdrType == TYPE_CONS) {
            void *cdrPtr = (void*)(untag_ptr(cdr));
            cons = (struct ConsCell*)cdrPtr;
            continue;
        }
        else if(cdrType == TYPE_SYMBOL) {
            void *cdrPtr = (void*)(untag_ptr(cdr));
            struct SymbolEntry *symbolEntry = (struct SymbolEntry*)cdrPtr;
            if (symbolEntry->variableSlot == nil_sym.variableSlot) {
                break;
            }
            cons = (struct ConsCell*)cdrPtr;
            continue;
        }
        else {
            resultListPtr[i+1] = cdr;
            numArgs++;
            break;
        }
    }

    return list(resultListPtr, numArgs);
}

uintptr_t oneplus(uintptr_t val) {
    long raw = tagged_ptr_to_fixnum(val);

    long rawResult = raw + 1;

    uintptr_t res = ((uintptr_t)rawResult << 3) | 0x1;
    return res;
}

uintptr_t eq(uintptr_t val1, uintptr_t val2) {
    // We can do a direct comparison of va1 and val2 - the result of eq is true if and only if we're referring to
    // either the same object in memory (tagged pointers should be equal) or a fixnum, or a symbol table entry etc etc.
    if (val1 == val2) {
        return t_sym.variableSlot;
    }
    else {
        return nil_sym.variableSlot;
    }
}

void inc_global(uintptr_t symbolEntry) {
    typecheck_symbol(symbolEntry);
    struct SymbolEntry *symEntry = (struct SymbolEntry*)(symbolEntry & 0xFFFFFFFFFFFFFFF8);
    uintptr_t currentVal = symEntry->variableSlot;
    typecheck_fixnum(currentVal);

    // Instead of incrementing fixnum by 1, we increment by 8 as the low 3 bits are for the type tag
    symEntry->variableSlot = symEntry->variableSlot+8;
}

uintptr_t open(uintptr_t fileSpec) {
    typecheck_string(fileSpec);
    char *fileSpecRaw = (char*)(fileSpec & 0xFFFFFFFFFFFFFFF8);
    if (DEBUG == 1) {
        printf("opening file %s\n", fileSpecRaw);
    }

    // open file ro for now
    FILE *streamPtr = fopen(fileSpecRaw, "r");
    if (streamPtr == NULL) {
        printf("File open error");
        exit(-1);
    }

    struct Object obj = {OBJ_TYPE_STREAM, streamPtr};
    void *heapObjPtr = malloc(sizeof(struct Object));
    memcpy(heapObjPtr, &obj, sizeof(struct Object));

    // tag the heap ptr
    uintptr_t taggedHeapPtr = (uintptr_t)heapObjPtr;
    taggedHeapPtr = taggedHeapPtr | TYPE_TAG_OBJ;

    if (DEBUG == 1) {
        printf("open: created stream obj with heapObjPtr %p, tagged heap ptr 0x%lx\n", heapObjPtr, taggedHeapPtr);
    }
    return taggedHeapPtr;
}

uintptr_t close(uintptr_t taggedStreamObj) {
    typecheck_obj(taggedStreamObj);
    struct Object *rawObjectPtr = (struct Object*)(taggedStreamObj & 0xFFFFFFFFFFFFFFF8);
    if (rawObjectPtr->type != OBJ_TYPE_STREAM) {
        printf("Expected stream obj!");
        exit(-1);
    }
    void *streamPtr = rawObjectPtr->rawPtr;
    fclose(streamPtr);
    return t_sym.variableSlot;
}

uintptr_t readLine(uintptr_t taggedStreamObj, uintptr_t terminateResult) {
    typecheck_obj(taggedStreamObj);
    struct Object *rawObjectPtr = (struct Object*)(taggedStreamObj & 0xFFFFFFFFFFFFFFF8);
    if (rawObjectPtr->type != OBJ_TYPE_STREAM) {
        printf("Expected stream obj!");
        exit(-1);
    }
    void *streamPtr = rawObjectPtr->rawPtr;

    char *line = NULL;
    size_t len = 0;

    // getline allocates/resizes *line automatically
    ssize_t read_bytes = getline(&line, &len, streamPtr);
    if (read_bytes == -1) {
        return terminateResult;
    }

    // Strip trailing newline if present
    if (read_bytes > 0 && line[read_bytes - 1] == '\n') {
        line[read_bytes - 1] = '\0';
        read_bytes--;
    }

    uintptr_t lineTaggedPtr = (uintptr_t)line;
    lineTaggedPtr = lineTaggedPtr | TYPE_TAG_STR;
    return lineTaggedPtr;
}

char *rewriteFormattedString(char* formatString, uintptr_t *formatArgs, size_t lenPlaceholders) {
    char *result;
    char *tempInput = malloc(strlen(formatString)+1);  // Include null char terminator
    if (!tempInput) {
        printf("format: could not allocate\n");
        exit(-1);
    }

    memcpy(tempInput, formatString, strlen(formatString));

    for (int placeholderIx = 0; placeholderIx<lenPlaceholders; placeholderIx++) {
        char *placeholderPos = strstr(tempInput, "~S");
        if (!placeholderPos) {
            printf("Missing placeholder in: %s (expected %zu placeholders)\n", formatString, lenPlaceholders);
            exit(-1);
        }

        char *currentReplacement = printValue(formatArgs[placeholderIx], false);

        size_t lenPrefix = placeholderPos - tempInput;
        size_t lenReplacement = strlen(currentReplacement);
        size_t lenRemainingFormatString = strlen(tempInput)-lenPrefix-2;  // Every placeholder is 2 chars long
        size_t totalLength = lenPrefix + lenReplacement + lenRemainingFormatString;

        result = malloc(totalLength + 1);  // plus 1 for null terminator
        if (!result) {
            printf("format: could not allocate string\n");
            exit(-1);
        }

        memcpy(result, tempInput, lenPrefix);
        memcpy(result + lenPrefix, currentReplacement, lenReplacement);
        memcpy(result + lenPrefix + lenReplacement, tempInput + lenPrefix + 2, lenRemainingFormatString);
        result[totalLength] = '\0';

        memcpy(tempInput, result, strlen(result));
        free(tempInput);
        free(currentReplacement);

        tempInput = result;
    }

    return result;
}

uintptr_t format(uintptr_t *args, long numArgs) {
    // Minimum of 2 args (stream designator and control string).  Optionally can have format arguments used in control string.
    // For now stream designator is assumed to be t (stdout)
    // We only handle ~S general placeholders for now.
    if (numArgs < 2) {
        printf("Too few arguments to format (%ld)\n", numArgs);
        exit(-1);
    }

    uintptr_t streamDesignator = args[0];
    RUNTIME_TYPE type = determineType(streamDesignator);
    if (type == TYPE_SYMBOL) {
        if (args[0] == t_sym.variableSlot) {
            // Not handling placeholders yet
            uintptr_t taggedFormatString = args[1];
            typecheck_string(taggedFormatString);
            char *formatStringRaw = (char*)(taggedFormatString & 0xFFFFFFFFFFFFFFF8);

            char *formattedString;
            if (numArgs > 2) {
                formattedString = rewriteFormattedString(formatStringRaw, &(args[2]), numArgs-2);
            }
            else {
                formattedString = formatStringRaw;
            }
            printf("%s\n", formattedString);
            return t_sym.variableSlot;
        }
        else {
            printf("Unhandled stream designator (not t) for format in: (%lx)\n", args[0]);
            exit(-1);
        }
    }
    else {
        printf("Unhandled stream designator (not symbol) for format in: (%d)\n", type);
        exit(-1);
    }
}

uintptr_t gt(uintptr_t arg1, uintptr_t arg2) {
    typecheck_fixnum(arg1);
    typecheck_fixnum(arg2);

    long val1 = arg1 >> 3;
    long val2 = arg2 >> 3;

    if (val1 > val2) {
        return t_sym.variableSlot;
    }
    else {
        return nil_sym.variableSlot;
    }
}


uintptr_t assoc(uintptr_t taggedKey, uintptr_t taggedCons) {
    RUNTIME_TYPE keyType = determineType(taggedKey);
    if (keyType == TYPE_UNKNOWN) {
        printf("Unknown key value type in: 0x%lx\n", taggedKey);
        exit(-1);
    }

    typecheck_cons(taggedCons);

    void* untaggedConsPtr = (void*)untag_ptr(taggedCons);
    struct ConsCell *cons = (struct ConsCell*)untaggedConsPtr;

    uintptr_t car;

    // TODO assuming no more than a 10 element cons list!  Could add the length to the ConsCell struct.
    // Each element is itself a 2-element list and we check the first element of each one for a match to taggedKey.
    for (int i=0; i<10; i++) {
        car = cons->car;
        RUNTIME_TYPE carType = determineType(car);
        if (carType != TYPE_CONS) {
            printf("assoc: element is not a list: 0x%lx\n", car);
            exit(-1);
        }

        void* untaggedInnerConsPtr = (void*)untag_ptr(car);
        struct ConsCell *innerCons = (struct ConsCell*)untaggedInnerConsPtr;
        uintptr_t innerCar = innerCons->car;
        if (taggedKey == innerCar) {
            // TODO check if safe to return a direct pointer to the inner list, should be ok as immutable?
            return car;
        }

        // We've not found the entry in the current car; now inspect the cdr which can only be either the next cons cell, or nil.

        uintptr_t cdr = cons->cdr;
        RUNTIME_TYPE cdrType = determineType(cdr);
        if (cdrType == TYPE_CONS) {
            void *cdrPtr = (void*)(untag_ptr(cdr));
            cons = (struct ConsCell*)cdrPtr;
            // Next loop for next cons cell
            continue;
        }
        // Otherwise must be nil at end of list
        else if(cdrType == TYPE_SYMBOL) {
            void *cdrPtr = (void*)(untag_ptr(cdr));
            struct SymbolEntry *symbolEntry = (struct SymbolEntry*)cdrPtr;
            if (symbolEntry->variableSlot == nil_sym.variableSlot) {
                break;
            }

            printf("assoc: cdr is a symbol but not nil: 0x%lx\n", cdr);
            exit(-1);
        }
        else {
            printf("assoc: cdr is not nil or a list: 0x%lx\n", cdr);
            exit(-1);
        }
    }

    return nil_sym.variableSlot;
}

// TODO this asm block is AI-generated, spend time understanding it
static uintptr_t call_closure_variadic(uintptr_t *args, long numArgs, void *rawFunctionPtr) {
    if (numArgs < 1 || numArgs > 8) {
        printf("funcall: unsupported numArgs=%ld (expected 1..8)\n", numArgs);
        exit(-1);
    }

    uintptr_t closure = args[0];
    register long r10 __asm__("x10") = numArgs;
    register void *r9  __asm__("x9")  = rawFunctionPtr;
    uintptr_t result = 0;

    switch (numArgs) {
        case 1: { /* 0 bindings; closure in x0 */
            register uintptr_t a0 __asm__("x0") = closure;
            __asm__ __volatile__ (
                "blr %[fn]\n\t"
                "mov %[res], x0\n\t"
                : [res] "=r" (result)
                : [fn] "r" (r9), "r" (r10), "r" (a0)
                : "x1", "x2", "x3", "x4", "x5", "x6", "x7",
                  "x8", "x11", "x12", "x13", "x14", "x15", "x16", "x17",
                  "memory", "cc"
            );
            break;
        }
        case 2: { /* 1 binding in x0; closure in x1 */
            register uintptr_t b0 __asm__("x0") = args[1];
            register uintptr_t c1 __asm__("x1") = closure;
            __asm__ __volatile__ (
                "blr %[fn]\n\t"
                "mov %[res], x0\n\t"
                : [res] "=r" (result)
                : [fn] "r" (r9), "r" (r10), "r" (b0), "r" (c1)
                : "x2", "x3", "x4", "x5", "x6", "x7",
                  "x8", "x11", "x12", "x13", "x14", "x15", "x16", "x17",
                  "memory", "cc"
            );
            break;
        }
        case 3: { /* 2 bindings in x0..x1; closure in x2 */
            register uintptr_t b0 __asm__("x0") = args[1];
            register uintptr_t b1 __asm__("x1") = args[2];
            register uintptr_t c2 __asm__("x2") = closure;
            __asm__ __volatile__ (
                "blr %[fn]\n\t"
                "mov %[res], x0\n\t"
                : [res] "=r" (result)
                : [fn] "r" (r9), "r" (r10), "r" (b0), "r" (b1), "r" (c2)
                : "x3", "x4", "x5", "x6", "x7",
                  "x8", "x11", "x12", "x13", "x14", "x15", "x16", "x17",
                  "memory", "cc"
            );
            break;
        }
        case 4: { /* 3 bindings in x0..x2; closure in x3 */
            register uintptr_t b0 __asm__("x0") = args[1];
            register uintptr_t b1 __asm__("x1") = args[2];
            register uintptr_t b2 __asm__("x2") = args[3];
            register uintptr_t c3 __asm__("x3") = closure;
            __asm__ __volatile__ (
                "blr %[fn]\n\t"
                "mov %[res], x0\n\t"
                : [res] "=r" (result)
                : [fn] "r" (r9), "r" (r10), "r" (b0), "r" (b1), "r" (b2), "r" (c3)
                : "x4", "x5", "x6", "x7",
                  "x8", "x11", "x12", "x13", "x14", "x15", "x16", "x17",
                  "memory", "cc"
            );
            break;
        }
        case 5: { /* 4 bindings in x0..x3; closure in x4 */
            register uintptr_t b0 __asm__("x0") = args[1];
            register uintptr_t b1 __asm__("x1") = args[2];
            register uintptr_t b2 __asm__("x2") = args[3];
            register uintptr_t b3 __asm__("x3") = args[4];
            register uintptr_t c4 __asm__("x4") = closure;
            __asm__ __volatile__ (
                "blr %[fn]\n\t"
                "mov %[res], x0\n\t"
                : [res] "=r" (result)
                : [fn] "r" (r9), "r" (r10), "r" (b0), "r" (b1), "r" (b2), "r" (b3), "r" (c4)
                : "x5", "x6", "x7",
                  "x8", "x11", "x12", "x13", "x14", "x15", "x16", "x17",
                  "memory", "cc"
            );
            break;
        }
        case 6: { /* 5 bindings in x0..x4; closure in x5 */
            register uintptr_t b0 __asm__("x0") = args[1];
            register uintptr_t b1 __asm__("x1") = args[2];
            register uintptr_t b2 __asm__("x2") = args[3];
            register uintptr_t b3 __asm__("x3") = args[4];
            register uintptr_t b4 __asm__("x4") = args[5];
            register uintptr_t c5 __asm__("x5") = closure;
            __asm__ __volatile__ (
                "blr %[fn]\n\t"
                "mov %[res], x0\n\t"
                : [res] "=r" (result)
                : [fn] "r" (r9), "r" (r10), "r" (b0), "r" (b1), "r" (b2), "r" (b3), "r" (b4), "r" (c5)
                : "x6", "x7",
                  "x8", "x11", "x12", "x13", "x14", "x15", "x16", "x17",
                  "memory", "cc"
            );
            break;
        }
        case 7: { /* 6 bindings in x0..x5; closure in x6 */
            register uintptr_t b0 __asm__("x0") = args[1];
            register uintptr_t b1 __asm__("x1") = args[2];
            register uintptr_t b2 __asm__("x2") = args[3];
            register uintptr_t b3 __asm__("x3") = args[4];
            register uintptr_t b4 __asm__("x4") = args[5];
            register uintptr_t b5 __asm__("x5") = args[6];
            register uintptr_t c6 __asm__("x6") = closure;
            __asm__ __volatile__ (
                "blr %[fn]\n\t"
                "mov %[res], x0\n\t"
                : [res] "=r" (result)
                : [fn] "r" (r9), "r" (r10), "r" (b0), "r" (b1), "r" (b2), "r" (b3), "r" (b4), "r" (b5), "r" (c6)
                : "x7",
                  "x8", "x11", "x12", "x13", "x14", "x15", "x16", "x17",
                  "memory", "cc"
            );
            break;
        }
        case 8: { /* 7 bindings in x0..x6; closure in x7 */
            register uintptr_t b0 __asm__("x0") = args[1];
            register uintptr_t b1 __asm__("x1") = args[2];
            register uintptr_t b2 __asm__("x2") = args[3];
            register uintptr_t b3 __asm__("x3") = args[4];
            register uintptr_t b4 __asm__("x4") = args[5];
            register uintptr_t b5 __asm__("x5") = args[6];
            register uintptr_t b6 __asm__("x6") = args[7];
            register uintptr_t c7 __asm__("x7") = closure;
            __asm__ __volatile__ (
                "blr %[fn]\n\t"
                "mov %[res], x0\n\t"
                : [res] "=r" (result)
                : [fn] "r" (r9), "r" (r10), "r" (b0), "r" (b1), "r" (b2), "r" (b3), "r" (b4), "r" (b5), "r" (b6), "r" (c7)
                : "x8", "x11", "x12", "x13", "x14", "x15", "x16", "x17",
                  "memory", "cc"
            );
            break;
        }
    }

    return result;
}

uintptr_t funcall(uintptr_t *args, long numArgs) {
    uintptr_t taggedFunctionPtr = *args;
    RUNTIME_TYPE functionObjType = determineType(taggedFunctionPtr);
    if (functionObjType != TYPE_FUNCTION && functionObjType != TYPE_CLOSURE) {
        printf("funcall: expect a function object but received: 0x%lx\n", taggedFunctionPtr);
        exit(-1);
    }

    if (functionObjType == TYPE_CLOSURE) {
        uintptr_t untaggedClosurePtr = taggedFunctionPtr & 0xFFFFFFFFFFFFFFF8;
        void *closureStructPtr = (void*)untaggedClosurePtr;
        struct Closure *closurePtr = (struct Closure*)closureStructPtr;
        uintptr_t result;
        void *rawFunctionPtr = closurePtr->fxnPtr;

        return call_closure_variadic(args, numArgs, rawFunctionPtr);
    }
    else {
        printf("funcall: unimplemented for functions (only for closures)\n");
        exit(-1);
    }
}

uintptr_t subseq(uintptr_t *args, long numArgs) {
    if (numArgs < 2 || numArgs > 3) {
        printf("subseq: expect 2 or 3 args\n");
        exit(-1);
    }

    // type-check and acquire input string
    uintptr_t inStringTaggedPtr = (uintptr_t)args[0];
    typecheck_string(inStringTaggedPtr);
    char *rawInputStr = (char*)(inStringTaggedPtr & 0xFFFFFFFFFFFFFFF8);

    // type-check and acquire start offset
    uintptr_t startPosTaggedPtr = args[1];
    typecheck_fixnum(startPosTaggedPtr);
    long startPos = (long)(startPosTaggedPtr >> 3);

    long endPos = strlen(rawInputStr);
    if (numArgs == 3) {
        uintptr_t endPosTaggedPtr = args[2];
        typecheck_fixnum(endPosTaggedPtr);
        endPos = (long)(endPosTaggedPtr >> 3);
    }

    long length = endPos - startPos;

    char *resRawStr = (char*)malloc(length + 1);
    memcpy(resRawStr, rawInputStr+startPos, length);
    resRawStr[length] = '\0';

    uintptr_t stringTaggedPtr = (uintptr_t)resRawStr;
    if ((stringTaggedPtr & 0x7) != 0) {
        printf("subseq: raw string result ptr not aligned to 8 bytes \n");
        exit(-1);
    }

    return (uintptr_t)stringTaggedPtr | TYPE_TAG_STR;
}

uintptr_t stringequal(uintptr_t taggedStr1, uintptr_t taggedStr2) {
    typecheck_string(taggedStr1);
    char *rawInputStr1 = (char*)(taggedStr1 & 0xFFFFFFFFFFFFFFF8);

    typecheck_string(taggedStr2);
    char *rawInputStr2 = (char*)(taggedStr2 & 0xFFFFFFFFFFFFFFF8);

    if (strcmp(rawInputStr1, rawInputStr2) == 0) {
        return t_sym.variableSlot;
    }
    else {
        return nil_sym.variableSlot;
    }
}
