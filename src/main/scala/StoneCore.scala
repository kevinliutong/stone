package stone
import chisel3._
import chisel3.util._

class StoneTopBundle(ap: ArchParameters) extends StoneBundle(ap){
  //val irfReadReq = Vec(ap.nIrfReadPorts, Decoupled(UInt(ap.irfIdxBits.W)))
  //val irfReadResp = Flipped(Vec(ap.nIrfReadPorts, UInt(ap.XLen.W)))
  val irfWriteReq = Vec(ap.nIrfWritePorts, Decoupled(new FUFastWB(ap)))
  val fromRename = Flipped(Vec(ap.dispatchWidth, UInt(32.W)))
}

class StoneBackEnd(ap: ArchParameters) extends Module{
  val renameWidth = ap.dispatchWidth
  val io = IO(new StoneTopBundle(ap))
  val dispatcher = Module(new Dispatcher(ap))

  val issueUnitVec = ap.issParamVec.map(p => Module(new IssueUnit(ap, p)))
  val fuUnitMatrix = ap.fuParamMatrix.map(_.map{p => p match {
    case AdderParam(name) => Module(new Adder(ap)).suggestName(name)
    case ShifterParam(name) => Module(new Shifter(ap)).suggestName(name)
  }})
  val wbArbVec = ap.wbArbParamVec.map(p => Module(new WriteBackArb(ap, p)))

  // dis <> Seq[issue] <> Seq[Seq[fu]] <> Seq[wbarb]
  dispatcher.io.fromRename <> io.fromRename
  issueUnitVec.map(_.io.fromDis).foreach(_ <> dispatcher.io.toIssUnits)

  //Seq[issue] <> Seq[Seq[fu]]
  (issueUnitVec zip fuUnitMatrix).foreach{case (iss, fuVec) =>
    fuVec.zipWithIndex.foreach{ case (fu, i) =>
      fu.io.req <> iss.io.toFU(i)
    }
  }
  // Seq[Seq[fu]] <> Seq[wbarb]
  (wbArbVec zip fuUnitMatrix).foreach{case (wbArb, fuVec) =>
    fuVec.zipWithIndex.foreach{ case (fu, i) =>
      wbArb.io.fromFU(i) <> fu.io.fastWB
    }
  }
  //Seq[wbarb] <> irfWrite
  (wbArbVec.map(_.io.wb) zip io.irfWriteReq).foreach{case (wb, irf) =>
    irf <> wb
  }

}
