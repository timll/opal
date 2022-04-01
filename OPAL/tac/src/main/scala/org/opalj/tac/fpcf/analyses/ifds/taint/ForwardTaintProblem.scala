/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.ifds.taint

import org.opalj.br.Method
import org.opalj.br.analyses.{DeclaredMethodsKey, SomeProject}
import org.opalj.tac._
import org.opalj.tac.fpcf.analyses.ifds.JavaIFDSProblem.V
import org.opalj.tac.fpcf.analyses.ifds.old.AbstractIFDSAnalysis
import org.opalj.tac.fpcf.analyses.ifds.{JavaIFDSProblem, JavaMethod, JavaStatement}

abstract class ForwardTaintProblem(project: SomeProject)
    extends JavaIFDSProblem[Fact](project)
    with TaintProblem[Method, JavaStatement, Fact] {
    val declaredMethods = project.get(DeclaredMethodsKey)
    override def nullFact: Fact = NullFact

    override def needsPredecessor(statement: JavaStatement): Boolean = false

    /**
     * If a variable gets assigned a tainted value, the variable will be tainted.
     */
    override def normalFlow(statement: JavaStatement, in: Fact, predecessor: Option[JavaStatement]): Set[Fact] = {
        val in_set = Set(in) // dirty hack for old code
        statement.stmt.astID match {
            case Assignment.ASTID ⇒
                in_set ++ createNewTaints(statement.stmt.asAssignment.expr, statement, in)
            case ArrayStore.ASTID ⇒
                val store = statement.stmt.asArrayStore
                val definedBy = store.arrayRef.asVar.definedBy
                val arrayIndex = TaintProblem.getIntConstant(store.index, statement.code)
                if (isTainted(store.value, in)) {
                    if (arrayIndex.isDefined)
                        // Taint a known array index
                        definedBy.foldLeft(in_set) { (c, n) ⇒
                            c + ArrayElement(n, arrayIndex.get)
                        }
                    else
                        // Taint the whole array if the index is unknown
                        definedBy.foldLeft(in_set) { (c, n) ⇒
                            c + Variable(n)
                        }
                } else if (arrayIndex.isDefined && definedBy.size == 1)
                    // Untaint if possible
                    in_set - ArrayElement(definedBy.head, arrayIndex.get)
                else in_set
            case PutField.ASTID ⇒
                val put = statement.stmt.asPutField
                val definedBy = put.objRef.asVar.definedBy
                if (isTainted(put.value, in))
                    definedBy.foldLeft(in_set) { (in, defSite) ⇒
                        in + InstanceField(defSite, put.declaringClass, put.name)
                    }
                else
                    in_set
            case PutStatic.ASTID ⇒
                val put = statement.stmt.asPutStatic
                if (isTainted(put.value, in))
                    in_set + StaticField(put.declaringClass, put.name)
                else
                    in_set
            case _ ⇒ in_set
        }
    }

    /**
     * Propagates tainted parameters to the callee. If a call to the sink method with a tainted
     * parameter is detected, no call-to-start
     * edges will be created.
     */
    override def callFlow(call: JavaStatement, callee: Method, in: Fact): Set[Fact] = {
        val in_set = Set(in) // compatilibity for old code
        val callObject = asCall(call.stmt)
        val allParams = callObject.allParams
        var facts = Set.empty[Fact]

        if (relevantCallee(callee)) {
            val allParamsWithIndices = allParams.zipWithIndex
            in_set.foreach {
                // Taint formal parameter if actual parameter is tainted
                case Variable(index) ⇒
                    allParamsWithIndices.foreach {
                        case (param, paramIndex) if param.asVar.definedBy.contains(index) ⇒
                            facts += Variable(AbstractIFDSAnalysis.switchParamAndVariableIndex(
                                paramIndex,
                                callee.isStatic
                            ))
                        case _ ⇒ // Nothing to do
                    }

                // Taint element of formal parameter if element of actual parameter is tainted
                case ArrayElement(index, taintedIndex) ⇒
                    allParamsWithIndices.foreach {
                        case (param, paramIndex) if param.asVar.definedBy.contains(index) ⇒
                            facts += ArrayElement(
                                AbstractIFDSAnalysis.switchParamAndVariableIndex(paramIndex, callee.isStatic),
                                taintedIndex
                            )
                        case _ ⇒ // Nothing to do
                    }

                case InstanceField(index, declClass, taintedField) ⇒
                    // Taint field of formal parameter if field of actual parameter is tainted
                    // Only if the formal parameter is of a type that may have that field!
                    allParamsWithIndices.foreach {
                        case (param, pIndex) if param.asVar.definedBy.contains(index) &&
                            (AbstractIFDSAnalysis.switchParamAndVariableIndex(pIndex, callee.isStatic) != -1 ||
                                project.classHierarchy.isSubtypeOf(declClass, declaredMethods(callee).declaringClassType)) ⇒
                            facts += InstanceField(
                                AbstractIFDSAnalysis.switchParamAndVariableIndex(pIndex, callee.isStatic),
                                declClass, taintedField
                            )
                        case _ ⇒ // Nothing to do
                    }

                case sf: StaticField ⇒ facts += sf

                case _               ⇒ // Nothing to do
            }
        }

        facts
    }

    /**
     * Taints an actual parameter, if the corresponding formal parameter was tainted in the callee.
     * If the callee's return value was tainted and it is assigned to a variable in the callee, the
     * variable will be tainted.
     * If a FlowFact held in the callee, this method will be appended to a new FlowFact, which holds
     * at this method.
     * Creates new taints and FlowFacts, if necessary.
     * If the sanitize method was called, nothing will be tainted.
     */
    override def returnFlow(exit: JavaStatement, in: Fact, call: JavaStatement, callFact: Fact): Set[Fact] = {
        val in_set = Set(in) // dirty hack for compatibility with old code
        val callee = exit.callable()
        /**
         * Checks whether the callee's formal parameter is of a reference type.
         */
        def isRefTypeParam(index: Int): Boolean =
            if (index == -1) true
            else {
                val parameterOffset = if (callee.isStatic) 0 else 1
                callee.descriptor.parameterType(
                    AbstractIFDSAnalysis.switchParamAndVariableIndex(index, callee.isStatic)
                        - parameterOffset
                ).isReferenceType
            }

        if (sanitizesReturnValue(callee)) return Set.empty
        val callStatement = asCall(call.stmt)
        val allParams = callStatement.allParams
        var flows: Set[Fact] = Set.empty
        in_set.foreach {
            // Taint actual parameter if formal parameter is tainted
            case Variable(index) if index < 0 && index > -100 && isRefTypeParam(index) ⇒
                val param = allParams(
                    AbstractIFDSAnalysis.switchParamAndVariableIndex(index, callee.isStatic)
                )
                flows ++= param.asVar.definedBy.iterator.map(Variable)

            // Taint element of actual parameter if element of formal parameter is tainted
            case ArrayElement(index, taintedIndex) if index < 0 && index > -100 ⇒
                val param = allParams(
                    AbstractIFDSAnalysis.switchParamAndVariableIndex(index, callee.isStatic)
                )
                flows ++= param.asVar.definedBy.iterator.map(ArrayElement(_, taintedIndex))

            case InstanceField(index, declClass, taintedField) if index < 0 && index > -10 ⇒
                // Taint field of actual parameter if field of formal parameter is tainted
                val param =
                    allParams(AbstractIFDSAnalysis.switchParamAndVariableIndex(index, callee.isStatic))
                param.asVar.definedBy.foreach { defSite ⇒
                    flows += InstanceField(defSite, declClass, taintedField)
                }

            case sf: StaticField ⇒ flows += sf

            // Track the call chain to the sink back
            case FlowFact(flow) if !flow.contains(JavaMethod(call.method)) ⇒
                flows += FlowFact(JavaMethod(call.method) +: flow)
            case _ ⇒
        }

        // Propagate taints of the return value
        if (exit.stmt.astID == ReturnValue.ASTID && call.stmt.astID == Assignment.ASTID) {
            val returnValueDefinedBy = exit.stmt.asReturnValue.expr.asVar.definedBy
            in_set.foreach {
                case Variable(index) if returnValueDefinedBy.contains(index) ⇒
                    flows += Variable(call.index)
                case ArrayElement(index, taintedIndex) if returnValueDefinedBy.contains(index) ⇒
                    flows += ArrayElement(call.index, taintedIndex)
                case InstanceField(index, declClass, taintedField) if returnValueDefinedBy.contains(index) ⇒
                    flows += InstanceField(call.index, declClass, taintedField)
                case NullFact ⇒
                    val taints = createTaints(callee, call)
                    if (taints.nonEmpty) flows ++= taints
                case _ ⇒ // Nothing to do
            }
        }
        val flowFact = createFlowFact(callee, call, in)
        if (flowFact.isDefined) flows += flowFact.get

        flows
    }

    /**
     * Removes taints according to `sanitizesParameter`.
     */
    override def callToReturnFlow(call: JavaStatement, in: Fact): Set[Fact] =
        if (sanitizesParameter(call, in)) Set() else Set(in)

    /**
     * Called, when the exit to return facts are computed for some `callee` with the null fact and
     * the callee's return value is assigned to a variable.
     * Creates a taint, if necessary.
     *
     * @param callee The called method.
     * @param call The call.
     * @return Some variable fact, if necessary. Otherwise none.
     */
    protected def createTaints(callee: Method, call: JavaStatement): Set[Fact]

    /**
     * Called, when the call to return facts are computed for some `callee`.
     * Creates a FlowFact, if necessary.
     *
     * @param callee The method, which was called.
     * @param call The call.
     * @return Some FlowFact, if necessary. Otherwise None.
     */
    protected def createFlowFact(callee: Method, call: JavaStatement,
                                 in: Fact): Option[FlowFact]

    /**
     * If a parameter is tainted, the result will also be tainted.
     * We assume that the callee does not call the source method.
     */
    override def outsideAnalysisContext(callee: Method): Option[OutsideAnalysisContextHandler] = {
        super.outsideAnalysisContext(callee) match {
            case Some(_) ⇒ Some(((call: JavaStatement, successor: JavaStatement, in: Fact) ⇒ {
                val allParams = asCall(call.stmt).receiverOption ++ asCall(call.stmt).params
                if (call.stmt.astID == Assignment.ASTID && (in match {
                    case Variable(index) ⇒
                        allParams.zipWithIndex.exists {
                            case (param, _) if param.asVar.definedBy.contains(index) ⇒ true
                            case _                                                   ⇒ false
                        }
                    case ArrayElement(index, _) ⇒
                        allParams.zipWithIndex.exists {
                            case (param, _) if param.asVar.definedBy.contains(index) ⇒ true
                            case _                                                   ⇒ false
                        }
                    case _ ⇒ false
                })) Set(Variable(call.index))
                else Set.empty
            }): OutsideAnalysisContextHandler)
            case None ⇒ None
        }

    }

    /**
     * Checks, if a `callee` should be analyzed, i.e. callFlow and returnFlow should create facts.
     * True by default. This method can be overwritten by a subclass.
     *
     * @param callee The callee.
     * @return True, by default.
     */
    protected def relevantCallee(callee: Method): Boolean = true

    /**
     * Creates new facts for an assignment. A new fact for the assigned variable will be created,
     * if the expression contains a tainted variable
     *
     * @param expression The source expression of the assignment
     * @param statement The assignment statement
     * @param in The incoming facts
     * @return The new facts, created by the assignment
     */
    private def createNewTaints(expression: Expr[V], statement: JavaStatement, in: Fact): Set[Fact] = {
        val in_set: Set[Fact] = Set(in) // dirty fix to reuse old code
        expression.astID match {
            case Var.ASTID ⇒
                val definedBy = expression.asVar.definedBy
                in_set.collect {
                    case Variable(index) if definedBy.contains(index) ⇒
                        Variable(statement.index)
                }
            case ArrayLoad.ASTID ⇒
                val loadExpression = expression.asArrayLoad
                val arrayDefinedBy = loadExpression.arrayRef.asVar.definedBy
                if (in_set.exists {
                    // One specific array element may be tainted
                    case ArrayElement(index, taintedElement) ⇒
                        val loadedIndex = TaintProblem.getIntConstant(loadExpression.index, statement.code)
                        arrayDefinedBy.contains(index) &&
                            (loadedIndex.isEmpty || taintedElement == loadedIndex.get)
                    // Or the whole array
                    case Variable(index) ⇒ arrayDefinedBy.contains(index)
                    case _               ⇒ false
                }) Set(Variable(statement.index))
                else
                    Set.empty
            case GetField.ASTID ⇒
                val get = expression.asGetField
                val objectDefinedBy = get.objRef.asVar.definedBy
                if (in_set.exists {
                    // The specific field may be tainted
                    case InstanceField(index, _, taintedField) ⇒
                        taintedField == get.name && objectDefinedBy.contains(index)
                    // Or the whole object
                    case Variable(index) ⇒ objectDefinedBy.contains(index)
                    case _               ⇒ false
                })
                    Set(Variable(statement.index))
                else
                    Set.empty
            case GetStatic.ASTID ⇒
                val get = expression.asGetStatic
                if (in_set.contains(StaticField(get.declaringClass, get.name)))
                    Set(Variable(statement.index))
                else Set.empty
            case BinaryExpr.ASTID | PrefixExpr.ASTID | Compare.ASTID | PrimitiveTypecastExpr.ASTID |
                NewArray.ASTID | ArrayLength.ASTID ⇒
                (0 until expression.subExprCount).foldLeft(Set.empty[Fact])((acc, subExpr) ⇒
                    acc ++ createNewTaints(expression.subExpr(subExpr), statement, in))
            case _ ⇒ Set.empty
        }
    }

    /**
     * Checks, if the result of some variable expression could be tainted.
     *
     * @param expression The variable expression.
     * @param in The current data flow facts.
     * @return True, if the expression could be tainted
     */
    private def isTainted(expression: Expr[V], in: Fact): Boolean = {
        val definedBy = expression.asVar.definedBy
        expression.isVar && (in match {
            case Variable(index)            ⇒ definedBy.contains(index)
            case ArrayElement(index, _)     ⇒ definedBy.contains(index)
            case InstanceField(index, _, _) ⇒ definedBy.contains(index)
            case _                          ⇒ false
        })
    }
}