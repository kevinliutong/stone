package stone
import chisel3._
import chisel3.util._

case class IssueUnitParam(fuParam: Seq[FUParam]){
  val supportAdd = fuParam.map(_.supportAdd).reduce(_ || _)
  val supportShift = fuParam.map(_.supportShift).reduce(_ || _)
  val supportDivide = fuParam.map(_.supportDivide).reduce(_ || _)
  val supportLoad = fuParam.map(_.supportLoad).reduce(_ || _)
  val supportStore = fuParam.map(_.supportStore).reduce(_ || _)
  val supportBranch = fuParam.map(_.supportBranch).reduce(_ || _)
  val fuParamSeq = fuParam
  val outputWidth: Int = fuParam.size
  val nIRFReadPorts = fuParam.map(_.nISources).max
}
/** each issue unit only has a set of read port from rf. */
class IssueUnit(ap: ArchParameters, issParam: IssueUnitParam) extends Module {
  val outputWidth: Int = issParam.outputWidth
  val maxIRFReadPorts = issParam.nIRFReadPorts
  val io = IO(new Bundle{
    val toFU = Vec(outputWidth, Decoupled(new FUReq(ap)))
    val fromDis = Vec(ap.dispatchWidth, Input(UInt(32.W)))
    //val readIRFReq = Vec(maxIRFReadPorts, UInt(ap.irfIdxBits.W))
    //val fromIRFData = Vec(maxIRFReadPorts, UInt(ap.XLen.W))
  })
  io.toFU := DontCare
  val supportAddVec = issParam.fuParam.map(_.supportAdd)
  io.toFU.foreach{ out =>
    when(io.fromDis(1) === 5.U && out.ready){
      out.valid := true.B
      out.bits.dstIdx := 4.U
      out.bits.source2Data := 1.U
      out.bits.source1Data := 3.U
    }
  }


}
