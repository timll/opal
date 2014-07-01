/* BSD 2-Clause License:
 * Copyright (c) 2009 - 2014
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
package org.opalj
package br
package instructions

/**
 * Load int from local variable.
 *
 * @author Michael Eichberg
 */
final class ILOAD private ( final override val lvIndex: Int)
        extends LoadLocalVariableInstruction
        with ExplicitLocalVariableIndex {

    final override def opcode: Opcode = ILOAD.opcode

    final override def mnemonic: String = "iload"

    override def equals(other: Any): Boolean =
        other match {
            case that: ILOAD ⇒ that.lvIndex == this.lvIndex
            case _           ⇒ false
        }

    override def hashCode: Int = ILOAD.opcode * 17 + lvIndex

    override def toString: String = "ILOAD_"+lvIndex
}
object ILOAD {

    final val opcode = 21

    def apply(lvIndex: Int): LoadLocalVariableInstruction =
        (lvIndex: @scala.annotation.switch) match {
            case 0 ⇒ ILOAD_0
            case 1 ⇒ ILOAD_1
            case 2 ⇒ ILOAD_2
            case 3 ⇒ ILOAD_3
            case _ ⇒ new ILOAD(lvIndex)
        }

    def unapply(iload: ILOAD): Option[Int] = Some(iload.lvIndex)
}