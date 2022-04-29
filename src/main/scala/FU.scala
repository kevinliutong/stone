package stone
import chisel3._
import chisel3.util._
case class FastWB(maxLatency: Int, minLatency: Int)

trait FUParam{
  val name: String
  val supportAdd: Boolean = false
  val supportShift: Boolean = false
  val supportDivide: Boolean = false
  val supportLoad: Boolean = false
  val supportStore: Boolean = false
  val supportBranch: Boolean = false
  val longWB: Boolean = false
  val pipelined: Boolean = true
  /** int source */
  val nISources: Int
  val fastWB: Option[FastWB]
}

case class AdderParam(adderName: String) extends FUParam{
  val name = adderName
  override val  supportAdd = true
  val  nISources = 2
  val  fastWB = Some(FastWB(1,1))
}
case class ShifterParam(shifterName: String) extends FUParam {
  val name = shifterName
  override val supportShift = true
  val nISources = 2
  val fastWB = Some(FastWB(1, 1))
}
case class DividerParam(DividerName: String) extends FUParam{
   val name = DividerName
   override val supportDivide = true
   override val pipelined = false
   val fastWB = None
   val nISources = 2
}
case class LoadUnitParam(LoadUnitName: String) extends FUParam {
  val name = LoadUnitName
  override val supportLoad = true
  val fastWB = Some(FastWB(3, 3))
  override val longWB = true
  val nISources = 2
}
case class StoreUnitParam(StoreUnitName: String) extends FUParam {
  val name = StoreUnitName
  override val supportStore = true
  val fastWB = None
  val nISources = 2
}
case class BranchUnitParam(BranchUnitName: String) extends FUParam {
  val name = BranchUnitName
  override val supportBranch = true
  val nISources = 2
  val fastWB = None
}

abstract class FUModuleBase(ap: ArchParameters) extends Module {
  val io = IO(new Bundle{
    val req = Flipped(Decoupled(new FUReq(ap)))
    val fastWB = Decoupled(new FUFastWB(ap))
  })
}
/*object FUModuleBase{
  def apply[fup <: FUParam, fum <: FUModuleBase](ap: ArchParameters, fuParam: fup) = {
    val fuModule = fuParam match {
      case AdderParam(name) => Module(new Adder(ap, _))
      case ShifterParam(name) => Module(new Shifter(ap,_))
    }
    fuModule
  }
}*/

class Adder(ap: ArchParameters) extends FUModuleBase(ap) {
  val reg = RegInit(0.U.asTypeOf(Valid(new FUReq(ap))))
  io.fastWB := DontCare
  io.req.ready := !reg.valid
  when(io.req.fire()){
    reg.valid := true.B
    reg.bits := io.req.bits
  }
  when(reg.valid){
    io.fastWB.valid := true.B
    io.fastWB.bits.dstIdx := reg.bits.dstIdx
    io.fastWB.bits.destData := reg.bits.source1Data + reg.bits.source2Data
    io.fastWB.bits.robIdx := reg.bits.robIdx
  }
  when(io.fastWB.ready && reg.valid){
    reg.valid := false.B
    io.req.ready := true.B
  }
}
class Shifter(ap: ArchParameters) extends FUModuleBase(ap) {
  val reg = RegInit(0.U.asTypeOf(Valid(new FUReq(ap))))
  io.fastWB := DontCare
  io.req.ready := !reg.valid
  when(io.req.fire()){
    reg.valid := true.B
    reg.bits := io.req.bits
  }
  when(reg.valid){
    io.fastWB.valid := true.B
    io.fastWB.bits.dstIdx := reg.bits.dstIdx
    io.fastWB.bits.destData := reg.bits.source1Data >> reg.bits.source2Data
    io.fastWB.bits.robIdx := reg.bits.robIdx
  }
  when(io.fastWB.ready && reg.valid){
    reg.valid := false.B
    io.req.ready := true.B
  }
}