package stone
import chisel3._
import chisel3.util._
case class WriteBackArbParam(fuParam: Seq[FUParam])
class WriteBackArb(ap: ArchParameters, wbArbParam: WriteBackArbParam) extends Module {
  val fuParam = wbArbParam.fuParam
  val inputWidth = fuParam.filter(p => p.fastWB.isDefined).size
  val io = IO(new Bundle{
    val fromFU = Flipped(Vec(inputWidth, Decoupled(new FUFastWB(ap))))
    val wb = Decoupled(new FUFastWB(ap))
    val toROB = Valid(UInt(ap.robIdxBits.W))
  })
  val arb = Module(new Arbiter(new FUFastWB(ap), inputWidth))
  arb.io.in <> io.fromFU
  io.wb <> arb.io.out
  io.toROB.valid := arb.io.out.valid
  io.toROB.bits := arb.io.out.bits.robIdx
}
