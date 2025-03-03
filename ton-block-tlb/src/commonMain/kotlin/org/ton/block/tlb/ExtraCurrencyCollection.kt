package org.ton.block.tlb

import org.ton.block.ExtraCurrencyCollection
import org.ton.block.VarUInteger
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.hashmap.HashMapE
import org.ton.hashmap.tlb.tlbCodec
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

fun ExtraCurrencyCollection.Companion.tlbCodec(): TlbCodec<ExtraCurrencyCollection> =
    ExtraCurrencyCollectionTlbConstructor()

private class ExtraCurrencyCollectionTlbConstructor : TlbConstructor<ExtraCurrencyCollection>(
    schema = "extra_currencies\$_ dict:(HashmapE 32 (VarUInteger 32)) = ExtraCurrencyCollection;"
) {
    private val varUInteger32Codec by lazy {
        VarUInteger.tlbCodec(32)
    }
    private val hashMapE32Codec by lazy {
        HashMapE.tlbCodec(32, varUInteger32Codec)
    }

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ExtraCurrencyCollection
    ) = cellBuilder {
        storeTlb(hashMapE32Codec, value.dict)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ExtraCurrencyCollection = cellSlice {
        val dict = loadTlb(hashMapE32Codec)
        ExtraCurrencyCollection(dict)
    }
}
