package stone
import chisel3._
import chisel3.util._
import freechips.rocketchip.tilelink.{TLEdge, TLEdgeOut}
trait product
case class A72() extends product
case class A74() extends product
case class CustomerParameters(
                               productName: product = A74()
                             )

case class GeneralParameters(cp: CustomerParameters){
  val FUParamGroupSeq = cp.productName match {
    case A72() => {
      val adder1 = AdderParam("adder1")
      val adder2 = AdderParam("adder2")
      val shifter1 = ShifterParam("shifter1")
      Seq(Seq(adder1, adder2), Seq(shifter1))
    }
    case A74() => {
      val adder1 = AdderParam("adder1")
      val adder2 = AdderParam("adder2")
      val adder3 = AdderParam("adder3")
      val adder4 = AdderParam("adder4")
      val adder5 = AdderParam("adder5")
      Seq(Seq(adder1, adder2), Seq(adder3), Seq(adder4, adder5))
    }
  }
  val coreWidth = cp.productName match {
    case A72() => 2
    case A74() => 3
  }
}
case class ArchParameters(gp: GeneralParameters) {
  /** true means need wb arb to be a real arb, false means just or all inputs. */
  val jammingIssue = true
  val XLen = 64
  val fuCodeWidth = 8
  val dispatchWidth = gp.coreWidth
  val nIRF = 128
  val irfIdxBits = log2Ceil(nIRF)
  val robEntries = 128
  val robIdxBits = log2Ceil(robEntries)
  val fuParamMatrix = gp.FUParamGroupSeq
  val issParamVec: Seq[IssueUnitParam] = fuParamMatrix.map(IssueUnitParam(_))
  val wbArbParamVec: Seq[WriteBackArbParam] = fuParamMatrix.map(WriteBackArbParam(_))
  val nIrfReadPorts: Int = issParamVec.map(_.nIRFReadPorts).reduce(_ + _)
  val nIrfWritePorts: Int = wbArbParamVec.size
  val robWBWidth = issParamVec.size
}

