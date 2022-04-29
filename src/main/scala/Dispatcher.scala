package stone
import chisel3._
import chisel3.util._

class Dispatcher(ap: ArchParameters) extends Module {
  val issueUnitParamVec = ap.issParamVec
  val outputWidth = issueUnitParamVec.size
  val io = IO(new Bundle{
    val fromRename = (Vec(ap.dispatchWidth, Input(UInt(32.W))))
    val toIssUnits = (Vec(outputWidth, Output(UInt(32.W))))
  })
  io.toIssUnits := DontCare
  io.toIssUnits(1) := 3.U + io.fromRename(1)
  io.toIssUnits(0) := 1.U
}
