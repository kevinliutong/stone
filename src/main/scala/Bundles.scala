package stone
import chisel3._
import chisel3.util._
import freechips.rocketchip.util.GenericParameterizedBundle
class StoneBundle(ap: ArchParameters) extends GenericParameterizedBundle(ap)

class FUReq(ap: ArchParameters) extends StoneBundle(ap){
  val fuCode = UInt(ap.fuCodeWidth.W)
  val robIdx = UInt(ap.robIdxBits.W)
  val dstIdx = UInt(ap.irfIdxBits.W)
  val source1Data = UInt(ap.XLen.W)
  val source2Data = UInt(ap.XLen.W)
  val source3Data = UInt(ap.XLen.W)
}
class FUFastWB(ap: ArchParameters) extends StoneBundle(ap){
  val destData = UInt(ap.XLen.W)
  val dstIdx = UInt(ap.irfIdxBits.W)
  val robIdx = UInt(ap.robIdxBits.W)
}
class FUtoROB(ap: ArchParameters) extends StoneBundle(ap){
  val robIdx = UInt(ap.robIdxBits.W)
}
