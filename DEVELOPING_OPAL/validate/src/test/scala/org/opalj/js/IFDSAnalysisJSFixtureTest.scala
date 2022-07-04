/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.js

import org.opal.js.IFDSAnalysisJSFixtureScheduler
import org.opalj.fpcf.PropertiesTest
import org.opalj.ai.domain.l2
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
import org.opalj.br.analyses.Project
import org.opalj.fpcf.properties.taint.ForwardFlowPath
import org.opalj.tac.cg.RTACallGraphKey
import org.opalj.tac.fpcf.analyses.ifds.taint.TaintNullFact

import java.net.URL

class IFDSAnalysisJSFixtureTest extends PropertiesTest {
    override def fixtureProjectPackage: List[String] = List(
        "org/opalj/fpcf/fixtures/js"
    )
    override def withRT = true

    override def init(p: Project[URL]): Unit = {
        p.updateProjectInformationKeyInitializationData(
            AIDomainFactoryKey
        )(
            (_: Option[Set[Class[_ <: AnyRef]]]) =>
                Set[Class[_ <: AnyRef]](
                    classOf[l2.DefaultPerformInvocationsDomainWithCFGAndDefUse[URL]]
                )
        )
        p.get(RTACallGraphKey)
    }

    describe("Test the ForwardFlowPath annotations") {
        val testContext = executeAnalyses(IFDSAnalysisJSFixtureScheduler)
        val project = testContext.project
        val eas = methodsWithAnnotations(project).map {
            case (method, entityString, annotations) =>
                ((method, TaintNullFact), entityString, annotations)
        }
        testContext.propertyStore.shutdown()
        validateProperties(testContext, eas, Set(ForwardFlowPath.PROPERTY_VALIDATOR_KEY))
    }
}
