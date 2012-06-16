/*
 * License (BSD Style License):
 * Copyright (c) 2012
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Software Technology Group or Technische
 *   Universität Darmstadt nor the names of its contributors may be used to
 *   endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.tud.cs.st.bat
package tac

import resolved.{ Instruction ⇒ BytecodeInstruction }
import resolved._
import resolved.ANEWARRAY

trait SomeMemoryLayout[+M <: SomeMemoryLayout[_]] {
    def locals: IndexedSeq[Value]
    def operands: List[Value]
    def update(instruction: BytecodeInstruction): M
}

class MemoryLayout(
    val operands: List[Value],
    val locals: IndexedSeq[Value])(
        implicit domain: Domain)
        extends SomeMemoryLayout[MemoryLayout] {

    /**
     * Extractor object that matches `Value`s which have computational type category 1.
     */
    private[this] object CTC1 {
        def unapply(value: Value): Boolean = value.computationalType.computationTypeCategory.id == 1
    }
    /**
     * Extractor object that matches `Value`s which have computational type category 2.
     */
    private[this] object CTC2 {
        def unapply(value: Value): Boolean = value.computationalType.computationTypeCategory.id == 2
    }

    def update(instruction: BytecodeInstruction): MemoryLayout = {
        import annotation.switch
        (instruction.opcode: @switch) match {

            //
            // LOAD AND STORE IN LOCAL VARIABLES
            //

            case 25 /*aload*/ ⇒
                new MemoryLayout(locals(instruction.asInstanceOf[ALOAD].lvIndex) :: operands, locals)
            case 24 /*dload*/ ⇒
                new MemoryLayout(locals(instruction.asInstanceOf[DLOAD].lvIndex) :: operands, locals)
            case 23 /*fload*/ ⇒
                new MemoryLayout(locals(instruction.asInstanceOf[FLOAD].lvIndex) :: operands, locals)
            case 21 /*iload*/ ⇒
                new MemoryLayout(locals(instruction.asInstanceOf[ILOAD].lvIndex) :: operands, locals)
            case 22 /*lload*/ ⇒
                new MemoryLayout(locals(instruction.asInstanceOf[LLOAD].lvIndex) :: operands, locals)
            case 42 /*aload_0*/ | 38 /*dload_0*/ | 34 /*fload_0*/ | 26 /*iload_0*/ | 30 /*lload_0*/ ⇒
                new MemoryLayout(locals(0) :: operands, locals)
            case 43 /*aload_1*/ | 39 /*dload_1*/ | 35 /*fload_1*/ | 27 /*iload_1*/ | 31 /*lload_1*/ ⇒
                new MemoryLayout(locals(1) :: operands, locals)
            case 44 /*aload_2*/ | 40 /*dload_2*/ | 36 /*fload_2*/ | 28 /*iload_2*/ | 32 /*lload_2*/ ⇒
                new MemoryLayout(locals(2) :: operands, locals)
            case 45 /*aload_3*/ | 41 /*dload_3*/ | 37 /*fload_3*/ | 29 /*iload_3*/ | 33 /*lload_3*/ ⇒
                new MemoryLayout(locals(3) :: operands, locals)

            case 58 /*astore*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(instruction.asInstanceOf[ASTORE].lvIndex, operands.head))
            case 57 /*dstore*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(instruction.asInstanceOf[DSTORE].lvIndex, operands.head))
            case 56 /*fstore*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(instruction.asInstanceOf[FSTORE].lvIndex, operands.head))
            case 54 /*listore*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(instruction.asInstanceOf[ISTORE].lvIndex, operands.head))
            case 55 /*lstore*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(instruction.asInstanceOf[LSTORE].lvIndex, operands.head))
            case 75 /*astore_0*/ | 71 /*dstore_0*/ | 67 /*fstore_0*/ | 63 /*lstore_0*/ | 59 /*istore_0*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(0, operands.head))
            case 76 /*astore_1*/ | 72 /*dstore_1*/ | 68 /*fstore_1*/ | 64 /*lstore_1*/ | 60 /*istore_1*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(1, operands.head))
            case 77 /*astore_2*/ | 73 /*dstore_2*/ | 69 /*fstore_2*/ | 65 /*lstore_2*/ | 61 /*istore_2*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(2, operands.head))
            case 78 /*astore_3*/ | 74 /*dstore_3*/ | 70 /*fstore_3*/ | 66 /*lstore_3*/ | 62 /*istore_3*/ ⇒
                new MemoryLayout(operands.tail, locals.updated(3, operands.head))

            //
            // CREATE ARRAYS
            // 
            case 189 /*anewarray*/ ⇒ {
                val count :: rest = operands
                val newOperands = domain.anewarray(count, instruction.asInstanceOf[ANEWARRAY].componentType) :: rest
                new MemoryLayout(newOperands, locals)
            }

            //
            // LOAD AND STORE VALUES IN ARRAYS
            //

            case 50 /*aaload*/ ⇒ {
                val index :: arrayref :: rest = operands
                val newOperands = domain.aaload(index, arrayref) :: rest
                new MemoryLayout(newOperands, locals)
            }
            case 83 /*aastore*/ ⇒ {
                val value :: index :: arrayref :: rest = operands
                domain.aastore(value, index, arrayref)
                new MemoryLayout(rest, locals)
            }

            case 51 /*baload*/ ⇒ {
                val index :: arrayref :: rest = operands
                val newOperands = domain.baload(index, arrayref) :: rest
                new MemoryLayout(newOperands, locals)
            }
            case 84 /*bastore*/ ⇒ {
                val value :: index :: arrayref :: rest = operands
                domain.bastore(value, index, arrayref)
                new MemoryLayout(rest, locals)
            }

            case 52 /*caload*/ ⇒ {
                val index :: arrayref :: rest = operands
                val newOperands = domain.caload(index, arrayref) :: rest
                new MemoryLayout(newOperands, locals)
            }
            case 85 /*castore*/ ⇒ {
                val value :: index :: arrayref :: rest = operands
                domain.castore(value, index, arrayref)
                new MemoryLayout(rest, locals)
            }
            case 49 /*daload*/ ⇒ {
                val index :: arrayref :: rest = operands
                val newOperands = domain.daload(index, arrayref) :: rest
                new MemoryLayout(newOperands, locals)
            }
            case 82 /*dastore*/ ⇒ {
                val value :: index :: arrayref :: rest = operands
                domain.dastore(value, index, arrayref)
                new MemoryLayout(rest, locals)
            }
            case 48 /*faload*/ ⇒ {
                val index :: arrayref :: rest = operands
                new MemoryLayout(domain.faload(index, arrayref) :: rest, locals)
            }
            case 81 /*fastore*/ ⇒ {
                val value :: index :: arrayref :: rest = operands
                domain.fastore(value, index, arrayref)
                new MemoryLayout(rest, locals)
            }

            //
            // LENGTH OF AN ARRAY            
            //

            case 190 /*arraylength*/ ⇒ {
                val arrayref = operands.head
                val newOperands = domain.arraylength(arrayref) :: operands.tail
                new MemoryLayout(newOperands, locals)
            }

            //
            // PUSH CONSTANT VALUES
            // 

            case 1 /*aconst_null*/ ⇒
                new MemoryLayout(domain.nullValue :: operands, locals)

            case 16 /*bipush*/ ⇒
                new MemoryLayout(domain.intValue(instruction.asInstanceOf[BIPUSH].value) :: operands, locals)

            case 14 /*dconst_0*/ ⇒ new MemoryLayout(domain.doubleValue(0.0d) :: operands, locals)
            case 15 /*dconst_1*/ ⇒ new MemoryLayout(domain.doubleValue(1.0d) :: operands, locals)

            case 11 /*fconst_0*/ ⇒ new MemoryLayout(domain.floatValue(0.0f) :: operands, locals)
            case 12 /*fconst_1*/ ⇒ new MemoryLayout(domain.floatValue(1.0f) :: operands, locals)
            case 13 /*fconst_2*/ ⇒ new MemoryLayout(domain.floatValue(2.0f) :: operands, locals)

            case 2 /*iconst_m1*/ ⇒ new MemoryLayout(domain.intValue(-1) :: operands, locals)
            case 3 /*iconst_0*/  ⇒ new MemoryLayout(domain.intValue(0) :: operands, locals)
            case 4 /*iconst_1*/  ⇒ new MemoryLayout(domain.intValue(1) :: operands, locals)
            case 5 /*iconst_2*/  ⇒ new MemoryLayout(domain.intValue(2) :: operands, locals)
            case 6 /*iconst_3*/  ⇒ new MemoryLayout(domain.intValue(3) :: operands, locals)
            case 7 /*iconst_4*/  ⇒ new MemoryLayout(domain.intValue(4) :: operands, locals)
            case 8 /*iconst_5*/  ⇒ new MemoryLayout(domain.intValue(5) :: operands, locals)

            //
            // TYPE CHECKS AND CONVERSION
            //
            case 192 /*checkcast*/ ⇒ {
                val objectref :: rest = operands
                val newOperands = domain.checkcast(objectref, instruction.asInstanceOf[CHECKCAST].referenceType) :: rest
                new MemoryLayout(newOperands, locals)
            }

            case 144 /*d2f*/ ⇒ new MemoryLayout(domain.d2f(operands.head) :: (operands.tail), locals)
            case 142 /*d2i*/ ⇒ new MemoryLayout(domain.d2i(operands.head) :: (operands.tail), locals)
            case 143 /*d2l*/ ⇒ new MemoryLayout(domain.d2l(operands.head) :: (operands.tail), locals)
            case 141 /*f2d*/ ⇒ new MemoryLayout(domain.f2d(operands.head) :: (operands.tail), locals)
            case 139 /*f2i*/ ⇒ new MemoryLayout(domain.f2i(operands.head) :: (operands.tail), locals)
            case 140 /*f2l*/ ⇒ new MemoryLayout(domain.f2l(operands.head) :: (operands.tail), locals)

            // 
            // RETURN FROM METHOD
            // 

            case 176 /*areturn*/ ⇒ {
                domain.areturn(operands.head)
                new MemoryLayout(List.empty, IndexedSeq.empty)
            }

            case 175 /*dreturn*/ ⇒ {
                domain.dreturn(operands.head)
                new MemoryLayout(List.empty, IndexedSeq.empty)
            }

            case 172 /*ireturn*/ ⇒ {
                domain.ireturn(operands.head)
                new MemoryLayout(List.empty, IndexedSeq.empty)
            }
            case 191 /*athrow*/ ⇒
                new MemoryLayout(List(domain.athrow(operands.head)), locals)

            //
            // LOCAL TRANSFER OF CONTROL
            //
            case 150 /*fcmpg*/ ⇒ {
                val value2 :: value1 :: rest = operands
                new MemoryLayout(domain.fcmpg(value1, value2) :: rest, locals)
            }
            case 149 /*fcmpl*/ ⇒ {
                val value2 :: value1 :: rest = operands
                new MemoryLayout(domain.fcmpl(value1, value2) :: rest, locals)
            }

            case 167 /*goto*/   ⇒ this
            case 200 /*goto_w*/ ⇒ this

            //////////////////////////////////////////            //////////////////////////////////////////

            case 99 /*dadd*/ ⇒ {
                val value2 :: value1 :: rest = operands
                val newOperands = domain.arithmeticExpression(DoubleType, Operator.Add, value2, value1) :: rest
                new MemoryLayout(newOperands, locals)
            }
            case 152 /*dcmpg*/ ⇒ new MemoryLayout(TypedValue.IntegerValue :: (operands.tail.tail), locals)
            case 151 /*dcmpl*/ ⇒ new MemoryLayout(TypedValue.IntegerValue :: (operands.tail.tail), locals)
            case 111 /*ddiv*/  ⇒ new MemoryLayout(operands.tail, locals)
            case 107 /*dmul*/ ⇒ {
                val value2 :: value1 :: rest = operands
                val newOperands = domain.arithmeticExpression(DoubleType, Operator.Mult, value2, value1) :: rest
                new MemoryLayout(newOperands, locals)
            }
            case 119 /*dneg*/ ⇒ new MemoryLayout(TypedValue.DoubleValue :: (operands.tail), locals)
            case 115 /*drem*/ ⇒ {
                val value2 :: value1 :: rest = operands
                val newOperands = domain.arithmeticExpression(DoubleType, Operator.Rem, value2, value1) :: rest
                new MemoryLayout(newOperands, locals)
            }

            case 103 /*dsub*/ ⇒ {
                val value2 :: value1 :: rest = operands
                val newOperands = domain.arithmeticExpression(DoubleType, Operator.Sub, value2, value1) :: rest
                new MemoryLayout(newOperands, locals)
            }
            case 89 /*dup*/ ⇒
                new MemoryLayout((operands.head) :: operands, locals)
            case 90 /*dup_x1*/ ⇒ operands match {
                case v1 :: v2 :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v1 :: rest, locals)
                case _ ⇒ sys.error("internal implementation error or invalid bytecode")
            }
            case 91 /*dup_x2*/ ⇒ operands match {
                case (v1 /*@ CTC1()*/ ) :: (v2 @ CTC1()) :: (v3 /*@ CTC1()*/ ) :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v3 :: v1 :: rest, locals)
                case (v1 /*@ CTC1()*/ ) :: v2 /* @ CTC2()*/ :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v1 :: rest, locals)
                case _ ⇒ sys.error("internal implementation error or invalid bytecode")
            }
            case 92 /*dup2*/ ⇒ operands match {
                case (v1 @ CTC1()) :: (v2 /*@ CTC1()*/ ) :: _ ⇒
                    new MemoryLayout(v1 :: v2 :: operands, locals)
                case (v /*@ CTC2()*/ ) :: _ ⇒
                    new MemoryLayout(v :: operands, locals)
                case _ ⇒ sys.error("internal implementation error or invalid bytecode")
            }
            case 93 /*dup2_x1*/ ⇒ operands match {
                case (v1 @ CTC1()) :: (v2 /*@ CTC1()*/ ) :: (v3 /*@ CTC1()*/ ) :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v3 :: v1 :: v2 :: rest, locals)
                case (v1 @ CTC2()) :: (v2 /*@ CTC1()*/ ) :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v1 :: rest, locals)
                case _ ⇒ sys.error("internal implementation error or invalid bytecode")
            }
            case 94 /*dup2_x2*/ ⇒ operands match {
                case (v1 @ CTC1()) :: (v2 @ CTC1()) :: (v3 @ CTC1()) :: (v4 /*@ CTC1()*/ ) :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v3 :: v4 :: v1 :: v2 :: rest, locals)
                case (v1 @ CTC2()) :: (v2 @ CTC1()) :: (v3 @ CTC1()) :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v3 :: v1 :: rest, locals)
                case (v1 @ CTC1()) :: (v2 @ CTC1()) :: (v3 @ CTC2()) :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v3 :: v1 :: v2 :: rest, locals)
                case (v1 @ CTC2()) :: (v2 /*@ CTC1()*/ ) :: rest ⇒
                    new MemoryLayout(v1 :: v2 :: v1 :: rest, locals)
                case _ ⇒ sys.error("internal implementation error or invalid bytecode")
            }
            case 98 /*fadd*/ ⇒ {
                val value2 :: value1 :: rest = operands
                new MemoryLayout(domain.fadd(value1, value2) :: rest, locals)
            }

            case 110 /*fdiv*/ ⇒ {
                val value2 :: value1 :: rest = operands
                new MemoryLayout(domain.fdiv(value1, value2) :: rest, locals)
            }

            case 106 /*fmul*/ ⇒ null
            case 118 /*fneg*/ ⇒ null
            case 114 /*frem*/ ⇒ null
            case 174 /*freturn*/ ⇒ {
                domain.freturn(operands.head)
                new MemoryLayout(List.empty, IndexedSeq.empty)
            }

            case 102 /*fsub*/            ⇒ null

            case 180 /*getfield*/        ⇒ null
            case 178 /*getstatic*/       ⇒ null

            case 145 /*i2b*/             ⇒ new MemoryLayout(TypedValue.ByteValue :: (operands.tail), locals)
            case 146 /*i2c*/             ⇒ new MemoryLayout(TypedValue.CharValue :: (operands.tail), locals)
            case 135 /*i2d*/             ⇒ new MemoryLayout(TypedValue.DoubleValue :: (operands.tail), locals)
            case 134 /*i2f*/             ⇒ new MemoryLayout(TypedValue.FloatValue :: (operands.tail), locals)
            case 133 /*i2l*/             ⇒ new MemoryLayout(TypedValue.LongValue :: (operands.tail), locals)
            case 147 /*i2s*/             ⇒ new MemoryLayout(TypedValue.ShortValue :: (operands.tail), locals)
            case 96 /*iadd*/             ⇒ new MemoryLayout(operands.tail, locals)
            case 46 /*iaload*/           ⇒ null
            case 126 /*iand*/            ⇒ new MemoryLayout(operands.tail, locals)
            case 79 /*iastore*/          ⇒ null

            case 108 /*idiv*/            ⇒ new MemoryLayout(operands.tail, locals)
            case 165 /*if_acmpeq*/       ⇒ new MemoryLayout(operands.tail.tail, locals)
            case 166 /*if_acmpne*/       ⇒ new MemoryLayout(operands.tail.tail, locals)
            case 159 /*if_icmpeq*/       ⇒ new MemoryLayout(operands.tail.tail, locals)
            case 160 /*if_icmpne*/       ⇒ new MemoryLayout(operands.tail.tail, locals)
            case 161 /*if_icmplt*/       ⇒ new MemoryLayout(operands.tail.tail, locals)
            case 162 /*if_icmpge*/       ⇒ new MemoryLayout(operands.tail.tail, locals)
            case 163 /*if_icmpgt*/       ⇒ new MemoryLayout(operands.tail.tail, locals)
            case 164 /*if_icmple*/       ⇒ new MemoryLayout(operands.tail.tail, locals)
            case 153 /*ifeq*/            ⇒ new MemoryLayout(operands.tail, locals)
            case 154 /*ifne*/            ⇒ new MemoryLayout(operands.tail, locals)
            case 155 /*iflt*/            ⇒ new MemoryLayout(operands.tail, locals)
            case 156 /*ifge*/            ⇒ new MemoryLayout(operands.tail, locals)
            case 157 /*ifgt*/            ⇒ new MemoryLayout(operands.tail, locals)
            case 158 /*ifle*/            ⇒ new MemoryLayout(operands.tail, locals)
            case 199 /*ifnonnull*/       ⇒ new MemoryLayout(operands.tail, locals)
            case 198 /*ifnull*/          ⇒ new MemoryLayout(operands.tail, locals)
            case 132 /*iinc*/            ⇒ this

            case 104 /*imul*/            ⇒ null
            case 116 /*ineg*/            ⇒ null
            case 193 /*instanceof*/      ⇒ null
            case 186 /*invokedynamic*/   ⇒ null
            case 185 /*invokeinterface*/ ⇒ null
            case 183 /*invokespecial*/   ⇒ null
            case 184 /*invokestatic*/    ⇒ null
            case 182 /*invokevirtual*/   ⇒ null
            case 128 /*ior*/             ⇒ null
            case 112 /*irem*/            ⇒ null

            case 120 /*ishl*/            ⇒ null
            case 122 /*ishr*/            ⇒ null

            case 100 /*isub*/            ⇒ null
            case 124 /*iushr*/           ⇒ null
            case 130 /*ixor*/            ⇒ null
            case 168 /*jsr*/             ⇒ null
            case 201 /*jsr_w*/           ⇒ null
            case 138 /*l2d*/             ⇒ null
            case 137 /*l2f*/             ⇒ null
            case 136 /*l2i*/             ⇒ null
            case 97 /*ladd*/             ⇒ null
            case 47 /*laload*/           ⇒ null
            case 127 /*land*/            ⇒ null
            case 80 /*lastore*/          ⇒ null
            case 148 /*lcmp*/            ⇒ null
            case 9 /*lconst_0*/          ⇒ null
            case 10 /*lconst_1*/         ⇒ null
            case 18 /*ldc*/              ⇒ null
            case 19 /*ldc_w*/            ⇒ null
            case 20 /*ldc2_w*/           ⇒ null
            case 109 /*ldiv*/            ⇒ null

            case 105 /*lmul*/            ⇒ null
            case 117 /*lneg*/            ⇒ null
            case 171 /*lookupswitch*/    ⇒ null
            case 129 /*lor*/             ⇒ null
            case 113 /*lrem*/            ⇒ null
            case 173 /*lreturn*/ ⇒ {
                domain.lreturn(operands.head)
                new MemoryLayout(List.empty, IndexedSeq.empty)
            }
            case 121 /*lshl*/           ⇒ null
            case 123 /*lshr*/           ⇒ null

            case 101 /*lsub*/           ⇒ null
            case 125 /*lushr*/          ⇒ null
            case 131 /*lxor*/           ⇒ null

            case 194 /*monitorenter*/   ⇒ null
            case 195 /*monitorexit*/    ⇒ null

            case 197 /*multianewarray*/ ⇒ null
            case 187 /*new*/            ⇒ null
            case 188 /*newarray*/       ⇒ null
            case 0 /*nop*/              ⇒ null
            case 87 /*pop*/             ⇒ null
            case 88 /*pop2*/            ⇒ null
            case 181 /*putfield*/       ⇒ null
            case 179 /*putstatic*/      ⇒ null
            case 169 /*ret*/            ⇒ null
            case 177 /*return*/         ⇒ null
            case 53 /*saload*/          ⇒ null
            case 86 /*sastore*/         ⇒ null
            case 17 /*sipush*/          ⇒ null
            case 95 /*swap*/            ⇒ null
            case 170 /*tableswitch*/    ⇒ null
            case 196 /*wide*/           ⇒ this // the instructions which are modified by a wide instruction already take care of the effect of wide
        }
    }
}