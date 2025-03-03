package org.ton.block.tlb

import org.ton.block.SimpleLib
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor

fun SimpleLib.Companion.tlbCodec(): TlbCodec<SimpleLib> = SimpleLibTlbConstructor()

private class SimpleLibTlbConstructor : TlbConstructor<SimpleLib>(
    schema = "simple_lib\$_ public:Bool root:^Cell = SimpleLib;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: SimpleLib
    ) = cellBuilder {
        storeBit(value.public)
        storeRef(value.root)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): SimpleLib = cellSlice {
        val public = loadBit()
        val root = loadRef()
        SimpleLib(public, root)
    }
}
