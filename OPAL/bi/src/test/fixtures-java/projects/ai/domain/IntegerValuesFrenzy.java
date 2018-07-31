/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package ai.domain;

/**
 * A class that does perform a large number of operations related to int values.
 *
 * @author Michael Eichberg
 */
public class IntegerValuesFrenzy {

    static final int theValue = 42;

    // identity function
    static int id(int i) {
        return i;
    }

    // uses the parameter as a variable
    static int countDown(int i) {
        do {
            id(i);
            i = i - 1;
        } while (i > 0);
        return i;
    }

    // the return value is either i or j
    static int max(int i, int j) {
        if (i < j)
            return i;
        else
            return j;

    }

    // the return value is at most equal to 5
    static int max5(int i) {
        if (i < 5)
            return i;
        else
            return 5;
    }

    // the return value is at most equal to 5; however, to get this result
    // aliases have to
    // be resolved.
    //
    // Code as generated by Eclipse 4.2.1
    // static int aliasingMax5(int i);
    // 0 iload_0 [i]
    // 1 istore_1 [j]
    // 2 iload_1 [j]
    // 3 iconst_5
    // 4 if_icmpge 9
    // 7 iload_0 [i]
    // 8 ireturn
    // 9 iconst_5
    // 10 ireturn
    //
    static int aliasingMax5(int i) {
        int j = i;
        if (j < 5)
            return i;
        else
            return 5;
    }

    static int aliasingMax6(int i) {
        int j = i;
        if (j <= 5)
            return i;
        else
            return 6;
    }

    static int aliasingMinM1(int i) {
        int j = i;
        if (j >= 0)
            return j;
        else
            return -1;
    }

    static int aliasingMin0(int i) {
        int j = i;
        if (j > 0)
            return i;
        else
            return 0;
    }

    static int someSwitch(int i) {
        int j;
        switch (i) {
        case 0:
            j = 0;
            break;
        case 1:
            j = 2;
            break;
        case 2:
            j = 4;
            break;
        default:
            throw new UnknownError();
        }

        return j * i; // j is in the range 0..4 and i in the range 0..2 =>
                      // result is in
                      // the range [0..8]
    }

    static int someComparisonThatReturns5(int i, int j) {
        if (i == 5 && i == j)
            return j; // returns 5
        else if (j == i && j < 5 && i > 4)
            return -1; // unreachable code
        else
            return 5;
    }

    static int[] array10() {
        int[] is = new int[10];
        for (int i = 0; i <= 10; i++) { // DELIBERATE INDEX OUT OF BOUNDS
            is[i] = i;
        }
        return is;
    }

    private static int getByte() {
        return (int) (System.currentTimeMillis() % 10000);
    }

    // Inspired by:
    // com.sun.xml.internal.org.jvnet.mimepull.BASE64DecoderStream {
    // int decode(byte[] outbuf, int pos, int len) throws IOException
    // }
    static int deadCode(byte[] outbuf, int pos, int len) throws UnknownError {
        int pos0 = pos;
        while (len >= 3) {
            int got = 0;
            while (got < 4) {
                int i = getByte();
                if (i == -1 || i == -2) {
                    boolean atEOF;
                    if (i == -1) {
                        if (got == 0) {
                            return pos - pos0;
                        }
                        atEOF = true; // don't read any more
                    } else { // i == -2
                        if (got == 0) {
                            return pos - pos0;
                        }
                        atEOF = false;
                    }

                    // we cannot reach this point..
                    throw new UnknownError("This is dead code!" + atEOF);
                } else {
                    return -666;
                }
            }
        }
        return 666;
    }

    static void doIt(int i) {
        /* NOTHING TO DO */
    }

    static void doIt(String what, int i) {
        /* NOTHING TO DO */
    }

    public static int anInt() {
        return (int) Math.random() * 10;
    }

    static void randomModulo() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);

            byte b0 = (byte) (((int) (Math.random() * 10)) % 2 == 0 ? 1 : 0);
            for (int i1 = 0; i1 < b0; ++i1) {
                System.out.println(i1);
            }
        }
        System.out.println("Done!");
    }

    static int cfDependentValuesAdd(int a, int b) {
        if (b == 0) {
            if (a < 101) {
                int c = a + b; // => c === [Int.MinValue,100]
                if (c > 99) // c === [100,100]
                    return a; // a = c-b ... [100,100] -0 === 100
                else if (c < 99)
                    return a; // => a === [Int.MinValue,98]
                else
                    return a; // a == [99,99]
            }
            return -1;

        } else {
            return -1;
        }
    }

    static int cfDependentValuesSub(int a, int b) {
        if (b == 0) {
            if (a < 101) {
                int c = a - b; // => c === [Int.MinValue,100]
                if (c > 99) // c === [100,100]
                    return a; // a = c-b ... [100,100] -0 === 100
                else if (c < 99)
                    return a; // => a === [Int.MinValue,98]
                else
                    return a; // a == [99,99]
            }
            return -1;

        } else {
            return -1;
        }
    }

    static int cfDependentValuesMult(int a, int b) {
        if (b > 1)
            return a < 0 ? a : -a; // a is [Int.MinValue,0]
        else if (b < 1) {
            int c = a * b; // c and a do not have a well defined relation
            if (a < 101) {
                if (c > 99)
                    return a; // [Int.MinValue,100]
                else if (c < 99)
                    return a; // [Int.MinValue,100]
                else
                    return a; // [Int.MinValue,100]
            }
            return c;
        } else /* if (b == 1) */ {
            int c = a * b; // => c === a
            if (a < 101) {
                if (c > 99) // c === [100,100]
                    return a; // a = c ... [100,100] -0 === 100
                else if (c < 99)
                    return a; // => a === [Int.MinValue,98]
                else
                    return a; // a == [99,99]
            }
            return c;
        }
    }

    // THERE IS NO IDEMPOTENT OPERATION FOR "REMAINDER" CALCULATIONS

    static int cfDependentValuesDiv(int a, int b) {
        if (b > 1)
            return b;
        else if (b < 1) {
            return b;
        } else /* if (b == 1) */ {
            int c = a / b; // => c === a
            if (a < 101) {
                if (c > 99) // c === [100,100]
                    return a; // a = c ... [100,100] -0 === 100
                else if (c < 99)
                    return a; // => a === [Int.MinValue,98]
                else
                    return a; // a == [99,99]
            }
            return c;
        }
    }

    static int cfDependentValuesBitwiseAnd(int a, int b) {
        if (b >= 0)
            return b;
        else if (b < -1) {
            return b;
        } else /* if (b == -1) */ {
            int c = a & b; // => c === a
            if (a < 101) {
                if (c > 99) // c === [100,100]
                    return a; // a = c ... [100,100] -0 === 100
                else if (c < 99)
                    return a; // => a === [Int.MinValue,98]
                else
                    return a; // a == [99,99]
            }
            return c;
        }
    }

    static int cfDependentValuesBitwiseOr(int a, int b) {
        if (b > 0)
            return b;
        else if (b < 0) {
            return b;
        } else /* if (b == 0) */ {
            int c = a | 0; // => c === a
            if (a < 101) {
                if (c > 99) // c === [100,100]
                    return a; // a = c ... [100,100] -0 === 100
                else if (c < 99)
                    return a; // => a === [Int.MinValue,98]
                else
                    return a; // a == [99,99]
            }
            return c;
        }
    }

    static void cfDependentValues1_v1() {
        int b = 0;
        int c = 0;
        int i = 0;
        while (i < 2) {
            int a = anInt();
            if (i == 0)
                b = a;
            else
                c = 1; // c is set to "1"
            i++;
        }
        if (c == 0) { // this is dead code... c is always guaranteed to be "1"
            doIt(b);
            doIt(c);
        }
    }

    static void cfDependentValues1_v2() {
        int b = 0;
        int c = 0;
        int i = 0;
        while (i < 2) {
            int a = anInt();
            if (i == 0)
                c = 1; // c is set to "1"
            else
                b = a;
            i++;
        }
        if (c == 0) { // this is dead code... c is always guaranteed to be "1"
            doIt(b);
            doIt(c);
        }
    }

    static void cfDependentValues1_v3() {
        int b = 0;
        int c = 0;
        int i = 0;
        while (i < 2) {
            int a = anInt();
            if (i == 1)
                c = 1; // c is set to "1"
            else
                b = a;
            i++;
        }
        if (c == 0) { // this is dead code... c is always guaranteed to be "1"
            doIt(b);
            doIt(c);
        }
    }

    static void cfDependentValues2() {
        //
        // The following is basically equivalent to
        // b = anInt
        // c = anInt
        // if(c == 0) {doIt(b);doIt(0)}
        //
        int b = 0;
        int c = 0;
        int i = 0;
        while (i < 2) {
            int a = anInt();
            if (i == 1)
                b = a; // <--- b is the value returned by the second call to
                       // anInt
            else
                c = a; // <--- c is the value returned by the first call to
                       // anInt
            i++;
        }
        if (c == 0) { // this only constraints "c" and not "b" - though both
                      // have the same
                      // origin
            doIt(b); // we know nothing about b here
            doIt(c); // c is "0"
        }
    }

    static void cfDependentValues3() {
        int b = 0;
        int c = 0;
        int i = 0;
        int j = i; // <--- j is just an alias for i
        while (j < 2) {
            int a = anInt();
            if (i == 1)
                b = a;
            else
                c = a;
            i++;
            j = i;
        }
        if (c == 0) { // this only constraints "c" and not "b" - though both
                      // have the same
                      // origin
            doIt(b); // we know nothing about b here
            doIt(c); // c is "0"
        }
    }

    static void cfDependentValues4() {
        int b = 0;
        int c = 0;
        int i = 0;
        int j = i; // j is just an alias for i
        while (j < 2) {
            int a = anInt();
            if (i == 1)
                b = a;
            else
                c = a;
            i++;
            j = i;
        }
        if (i == 2) {
            doIt(j); // j == 2
            doIt(b); // some value (but not "c")
            doIt(c); // some value (but not "b")
        }
    }

    static void cfDependentValues5() {
        int b = 0;
        int c = 0;
        int i = 0;
        int j = i; // j is just an alias for i
        while (j < 2) {
            int a = anInt();
            if (a == 1)
                b = a; // <--- the value depends on a
            else
                c = a; // <--- the value depends on a
            i++;
            j = i;
        }
        // b and c may refer to the same value or different values
        if (i == 2) {
            doIt(j); // j is "2"
            doIt(b); // b is either "0" or "1"
            doIt(c);
        }
    }

    static void cfDependentValues6() {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;

        for (int i = 0; i < 3; i++) {
            int o = anInt();
            switch (i) {
            case 0:
                a = o;
                break;
            case 1:
                b = o;
                break;
            case 2:
                c = o;
                break;
            }
        }
        if (a == 0) {
            doIt(/* "a: a===0", */a);
            doIt(/* "a: b.is0.isUnknown", */b);
            doIt(/* "a: c.is0.isUnknown", */c);
            doIt(/* "a: d===null", */d);
        }
        if (b == 0) {
            doIt(/* "b: a.is0.isUnknown", */a);
            doIt(/* "b: b===0", */b);
            doIt(/* "b: c.is0.isUnknown", */c);
            doIt(/* "b: d===0", */d);
        }
        if (c == 0) {
            doIt(/* "c: a.is0.isUnknown", */a);
            doIt(/* "c: b.is0.isUnknown", */b);
            doIt(/* "c: c===0", */c);
            doIt(/* "c: d===0", */d);
        }

    }

    static void complexConditions1(int c) {
        int i = 0;
        if (c == 0)
            i = 1;
        else if (c == 1)
            i = 2;
        else
            i = -1;
        // here is the join...
        doIt(i); // i is either 1, 2 or -1
    }

    static void complexConditions2(int c, int d) {
        int i = 0;
        if (c == 0)
            i = 1;
        if (d == 1)
            i = 2;

        // here is the join...
        doIt(i); // i either 0,1 or 2 value (we now nothing about c and d)
    }

    static void complexConditions3(int c, int d) {
        int i = 0;
        if (c == 0) {
            i = 1;
            if (d == 1) {
                i += 1;
            }
            doIt(i); // i is either 1 or 2
        }

        // here is the join...
        doIt(i); // i either 0,1 or 2 value (we now nothing about c and d)
    }

    @SuppressWarnings("all")
    static void complexLoop(int c, int d) {
        int i = 0;
        t: while (true) {
            if (c * d > 100)
                break t;
            if (c == 1)
                i = 1;
            else
                i = 2;
            c += 1;
            d += 1;
        }

        // we know nothing about c and d
        doIt(i); // i is either 0,1 or 2
    }

    static void casts() {

        int read = 0;
        while ((read = anInt()) > 0) {
            byte readed = (byte) read;
            if (readed == (byte) -1) {
                continue;
            }
            if (readed == (byte) 127) {
                break;
            }
            doIt(readed); // a value in the range [-128,126] except -1 (cannot
                          // be
                          // captured) and 127
        }
    }

    @SuppressWarnings("all")
    static int countingLoop(int k) {
        int i = 0;
        int j = 1;
        while (i < 5 && k < 10) {
            j += 1;
            i += 1;
            k += 1;
        }
        return j;
    }

    static int moreComplexAliasing() {
        // Inspired by:
        // 107  public int stringToIndex(String s)
        // 108    {
        // 109      if(s==null) return NULL;
        // 110
        // 111      int hashslot=s.hashCode()%HASHPRIME;
        // 112      if(hashslot<0) hashslot=-hashslot;
        // 113
        // 114      // Is it one we already know?
        // 115      int hashlast=m_hashStart[hashslot];
        // 116      int hashcandidate=hashlast;
        // 117      while(hashcandidate!=NULL)
        // 118        {
        // 119          if(m_intToString.elementAt(hashcandidate).equals(s))
        // 120            return hashcandidate;
        // 121
        // 122          hashlast=hashcandidate;
        // 123          hashcandidate=m_hashChain.elementAt(hashcandidate);
        // 124        }
        // 125
        // 126      // New value. Add to tables.
        // 127      int newIndex=m_intToString.size();
        // 128      m_intToString.addElement(s);
        // 129
        // 130      m_hashChain.addElement(NULL);     // Initialize to no-following-same-hash
        // 131      if(hashlast==NULL)  // First for this hash
        // 132        m_hashStart[hashslot]=newIndex;
        // 133      else // Link from previous with same hash
        // 134        m_hashChain.setElementAt(newIndex,hashlast);
        // 135
        // 136      return newIndex;
        // 137    }
        int hashlast = anInt();
        int hashcandidate = hashlast;
        while (hashcandidate != 0) {
            hashlast = hashcandidate;
            hashcandidate = anInt();
        }

        return hashlast; // 0 if 0 right from the start; non-zero in all other
                         // cases
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // To catch the bugs in the following a "relational domain" (that puts two
    // different values in relation) is needed.
    //

    static void multipleConstraints1(int a, int b) {
        if (a < b) // we know nothing about a and b...
            doIt(-1);
        else if (a == b)
            doIt(0);
        else if (b > a) // <== "BUG" - no longer possible
            doIt(1);
    }

    static int multipleConstraints2(int a, int b) {
        if (a < b)
            return (-1);
        else if (a == b)
            return (0);

        doIt(Integer.MAX_VALUE);

        if (b > a) // <== "BUG" - already tested above...
            return (1);
        else
            throw new Error();
    }

    // Inspired by: java.awt.font.StyledParagraph.findRunContaining
    static int findRunContaining(int index, int[] starts) {
        for (int i = 1; true; i++) {
            if (starts[i] > index) {
                return i - 1;
            }
        }
    }
    // ORIGINAL ISSUE: LED TO AN EXCESSIVE NUMBER OF CONSTRAINTS BETWEEN INTEGER
    // VALUES!

    public static void lowerBoundUpperBound(int size) throws Exception {
        for (int i = 0; i < size; i++) {
            ;
        }
        for (int i = 0; i < size; i++) {
            ;
        }
    }

    public static String simpleConstantComputations(int i) throws Exception {
        int j = i << 2;
        int k = j % 2; // ==> k == 0 ALWAYS!
        if (k == 3)
            return "dead code...";

        return "as expected";
    }

    public static String semiComplexConstantComputations(int i) throws Exception {
        if (i < 0 || i > 65535) return "forget about it...";

        int j = i << 2;// the value is > 3 and is additionally a multiplicativ of 4
        int k = j % 2; // ==> k == 0 ALWAYS!
        if (k == 1)
            return "dead code...";

        return "as expected";
    }

    public static String complexConstantComputations(int i) throws Exception {
        int j = i << 2; // the value is NOT in [-3..+3] and is additionally a multiplicativ of 4
        int k = j % 2; // ==> k == 0 ALWAYS!
        if (k == 1)
           return "dead code...";

        return "as expected";
    }
}
