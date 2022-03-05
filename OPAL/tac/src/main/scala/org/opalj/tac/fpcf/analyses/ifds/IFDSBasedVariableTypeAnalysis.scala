/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.ifds

import java.io.File
import java.io.PrintWriter

import org.opalj.fpcf.PropertyKey
import org.opalj.fpcf.PropertyStore
import org.opalj.fpcf.PropertyBounds

import org.opalj.br.analyses.SomeProject

import org.opalj.tac.fpcf.properties.cg.Callers
import org.opalj.tac.fpcf.properties.IFDSProperty
import org.opalj.tac.fpcf.properties.IFDSPropertyMetaInformation
import org.opalj.tac.fpcf.properties.TACAI

/**
 * A variable type analysis implemented as an IFDS analysis.
 * In contrast to an ordinary variable type analysis, which also determines types of fields,
 * this analysis only determines the types of local variables.
 * The subsuming traint can be mixed in to enable subsuming.
 *
 * @param project The analyzed project.
 * @author Mario Trageser
 */
class IFDSBasedVariableTypeAnalysis private (ifdsProblem: VariableTypeProblem)(implicit val project: SomeProject)
    extends ForwardIFDSAnalysis[VTAFact](ifdsProblem, VTAResult)

object IFDSBasedVariableTypeAnalysis extends IFDSAnalysis[VTAFact] {

    var SUBSUMING: Boolean = true

    override val uses: Set[PropertyBounds] =
        Set(PropertyBounds.finalP(TACAI), PropertyBounds.finalP(Callers))

    override def init(p: SomeProject, ps: PropertyStore): IFDSBasedVariableTypeAnalysis = {
        implicit val project = p
        if (SUBSUMING) new IFDSBasedVariableTypeAnalysis(new VariableTypeProblem(p)) with Subsuming[VTAFact]
        else new IFDSBasedVariableTypeAnalysis(new VariableTypeProblem(p))
    }

    override def property: IFDSPropertyMetaInformation[JavaStatement, VTAFact] = VTAResult
}

/**
 * The IFDSProperty for this analysis.
 */
case class VTAResult(flows: Map[JavaStatement, Set[VTAFact]]) extends IFDSProperty[JavaStatement, VTAFact] {

    override type Self = VTAResult
    override def create(result: Map[JavaStatement, Set[VTAFact]]): IFDSProperty[JavaStatement, VTAFact] = new VTAResult(result)

    override def key: PropertyKey[VTAResult] = VTAResult.key
}

object VTAResult extends IFDSPropertyMetaInformation[JavaStatement, VTAFact] {

    override type Self = VTAResult
    override def create(result: Map[JavaStatement, Set[VTAFact]]): IFDSProperty[JavaStatement, VTAFact] = new VTAResult(result)

    val key: PropertyKey[VTAResult] = PropertyKey.create("VTA", new VTAResult(Map.empty))
}

class IFDSBasedVariableTypeAnalysisRunner extends AbsractIFDSAnalysisRunner {

    override def analysisClass: IFDSBasedVariableTypeAnalysis.type = IFDSBasedVariableTypeAnalysis

    override def printAnalysisResults(analysis: AbstractIFDSAnalysis[_], ps: PropertyStore): Unit = {}

    override protected def additionalEvaluationResult(
        analysis: AbstractIFDSAnalysis[_]
    ): Option[Object] =
        analysis match {
            case subsuming: Subsuming[_] ⇒ Some(subsuming.numberOfSubsumptions)
            case _                       ⇒ None
        }

    override protected def writeAdditionalEvaluationResultsToFile(
        writer:                      PrintWriter,
        additionalEvaluationResults: Seq[Object]
    ): Unit = {
        val numberOfSubsumptions = additionalEvaluationResults.map(_.asInstanceOf[NumberOfSubsumptions])
        val length = additionalEvaluationResults.length
        val tries = numberOfSubsumptions.map(_.triesToSubsume).sum / length
        val successes = numberOfSubsumptions.map(_.successfulSubsumes).sum / length
        writer.println(s"Average tries to subsume: $tries")
        writer.println(s"Average successful subsumes: $successes")
    }
}

object IFDSBasedVariableTypeAnalysisRunner {
    def main(args: Array[String]): Unit = {
        if (args.contains("--help")) {
            println("Potential parameters:")
            println(" -seq (to use the SequentialPropertyStore)")
            println(" -l2 (to use the l2 domain instead of the default l1 domain)")
            println(" -delay (for a three seconds delay before the taint flow analysis is started)")
            println(" -debug (for debugging mode in the property store)")
            println(" -evalSchedulingStrategies (evaluates all available scheduling strategies)")
            println(" -f <file> (Stores the average runtime to this file)")
        } else {
            val fileIndex = args.indexOf("-f")
            new IFDSBasedVariableTypeAnalysisRunner().run(
                args.contains("-debug"),
                args.contains("-l2"),
                args.contains("-delay"),
                args.contains("-evalSchedulingStrategies"),
                if (fileIndex >= 0) Some(new File(args(fileIndex + 1))) else None
            )
        }
    }
}
