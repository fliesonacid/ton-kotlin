package org.ton.block.tlb

import org.ton.block.VmStack
import org.ton.block.VmStackList
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

fun VmStack.Companion.tlbCodec(): TlbCodec<VmStack> = VmStackTlbConstructor()

private class VmStackTlbConstructor : TlbConstructor<VmStack>(
    schema = "vm_stack#_ depth:(## 24) stack:(VmStackList depth) = VmStack;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmStack
    ) = cellBuilder {
        storeUInt(value.depth, 24)
        storeTlb(VmStackList.tlbCodec(value.depth), value.stack)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmStack = cellSlice {
        val depth = loadUInt(24).toInt()
        val stack = loadTlb(VmStackList.tlbCodec(depth))
        VmStack(depth, stack)
    }
}
