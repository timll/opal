/* BSD 2-Clause License:
 * Copyright (c) 2009 - 2017
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
package tac

import java.io.FileInputStream

import org.opalj.ai.BaseAI
import org.opalj.ai.domain.l0.PrimitiveTACAIDomain
import org.opalj.br.Code
import org.opalj.br.ComputationalTypeReference
import org.opalj.br.reader.Java9Framework

/**
 * Collects points-to information related to a method.
 *
 * @author Michael Eichberg
 */
object LocalPointsTo {

    def main(args: Array[String]): Unit = {
        // Load the class file (we don't handle invokedynamic in this case)
        val cf = Java9Framework.ClassFile(() ⇒ new FileInputStream(args(0))).head
        // ... now let's take the first method that matches our filter
        val m = cf.methods.filter(m ⇒ m.signatureToJava().contains(args(1))).head
        // ... let's get one of the default pre-initialized class hierarchies (typically we want a project!)
        val ch = Code.BasicClassHierarchy
        // ... perform the data-flow analysis
        val aiResult = BaseAI(m, new PrimitiveTACAIDomain(ch, m))
        // now, we can transform the bytecode to three-address code
        val tac = TACAI(m, ch, aiResult)(Nil /* no tac based optimizations*/ )

        // Let's print the three address code to get a better feeling for it...
        // Please note, "pc" in the output refers to the program counter of the original
        // underlying bytecode instruction; the "pc" is kept to avoid that we have to
        // transform the other information in the class file too.
        println(tac)

        // Let's collect the information where a reference value that is passed
        // to some method is coming from.
        for {
            (MethodCallParameters(params), stmtIndex) ← tac.stmts.iterator.zipWithIndex
            (UVar(v, defSites), paramIndex) ← params.iterator.zipWithIndex
            if v.computationalType == ComputationalTypeReference
            defSite ← defSites
        } {
            if (defSite >= 0) {
                val Assignment(_, _, expr) = tac.stmts(defSite) // a def site is always an assignment
                println(s"call@$stmtIndex(param=$paramIndex) is "+expr)
            } else {
                println(s"call@$stmtIndex(param=$paramIndex) takes param "+(-defSite - 1))
            }
        }

        println("Done.")
    }
}
