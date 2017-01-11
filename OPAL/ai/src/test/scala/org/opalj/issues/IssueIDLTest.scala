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
package issues

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import play.api.libs.json.JsString
import play.api.libs.json.Json

/**
 * Tests the toIDL method of Issue
 *
 * @author Lukas Berg
 */
@RunWith(classOf[JUnitRunner])
class IssueIDLTest extends FlatSpec with Matchers {

    import IDLTestsFixtures._

    behavior of "the toIDL method"

    it should "return a valid issue description for a most basic Issue" in {
        val issue = Issue(
            null,
            Relevance.OfNoRelevance,
            "foo",
            Set.empty,
            Set.empty,
            Seq(simplePackageLocation)
        )

        issue.toIDL should be(Json.obj(
            "analysis" → JsString(null),
            "relevance" → toIDL(Relevance.OfNoRelevance),
            "summary" → "foo",
            "categories" → Json.arr(),
            "kinds" → Json.arr(),
            "details" → Json.arr(),
            "locations" → Json.arr(simplePackageLocationIDL)
        ))
    }

    it should "return a valid issue description for an Issue which specifies the underlying analysis" in {
        val issue = Issue(
            "bar",
            Relevance.OfNoRelevance,
            "foo",
            Set.empty,
            Set.empty,
            Seq(simplePackageLocation)
        )

        issue.toIDL should be(Json.obj(
            "analysis" → "bar",
            "relevance" → toIDL(Relevance.OfNoRelevance),
            "summary" → "foo",
            "categories" → Json.arr(),
            "kinds" → Json.arr(),
            "details" → Json.arr(),
            "locations" → Json.arr(simplePackageLocationIDL)
        ))
    }

    it should "return a valid issue description for an Issue which defines some categories" in {
        val issue = Issue(
            null,
            Relevance.OfNoRelevance,
            "bar",
            Set("a", "b"),
            Set.empty,
            Seq(simplePackageLocation)
        )

        issue.toIDL should be(Json.obj(
            "analysis" → JsString(null),
            "relevance" → toIDL(Relevance.OfNoRelevance),
            "summary" → "bar",
            "categories" → Json.arr("a", "b"),
            "kinds" → Json.arr(),
            "details" → Json.arr(),
            "locations" → Json.arr(simplePackageLocationIDL)
        ))
    }

    it should "return a valid issue description for an Issue which defines the kinds" in {
        val issue = Issue(
            null,
            Relevance.OfNoRelevance,
            "foo",
            Set.empty,
            Set("c", "d"),
            Seq(simplePackageLocation)
        )

        issue.toIDL should be(Json.obj(
            "analysis" → JsString(null),
            "relevance" → toIDL(Relevance.OfNoRelevance),
            "summary" → "foo",
            "categories" → Json.arr(),
            "kinds" → Json.arr("c", "d"),
            "details" → Json.arr(),
            "locations" → Json.arr(simplePackageLocationIDL)
        ))
    }

    it should "return a valid issue description for an Issue which specifies further details" in {
        val issue = Issue(
            null,
            Relevance.OfNoRelevance,
            "foo",
            Set.empty,
            Set.empty,
            Seq(simplePackageLocation),
            Seq(simpleOperands, simpleLocalVariables)
        )

        issue.toIDL should be(Json.obj(
            "analysis" → JsString(null),
            "relevance" → toIDL(Relevance.OfNoRelevance),
            "summary" → "foo",
            "categories" → Json.arr(),
            "kinds" → Json.arr(),
            "details" → Json.arr(simpleOperandsIDL, simpleLocalVariablesIDL),
            "locations" → Json.arr(simplePackageLocationIDL)
        ))
    }

    it should "return a valid issue description for an Issue which specifies all standard attributes" in {
        val issue = Issue(
            "foo",
            Relevance.OfUtmostRelevance,
            "bar",
            Set("b", "a"),
            Set("d", "c"),
            Seq(simplePackageLocation),
            Seq(simpleLocalVariables, simpleOperands)
        )

        issue.toIDL should be(Json.obj(
            "analysis" → "foo",
            "relevance" → toIDL(Relevance.OfUtmostRelevance),
            "summary" → "bar",
            "categories" → Json.arr("b", "a"),
            "kinds" → Json.arr("d", "c"),
            "details" → Json.arr(simpleLocalVariablesIDL, simpleOperandsIDL),
            "locations" → Json.arr(simplePackageLocationIDL)
        ))
    }
}
