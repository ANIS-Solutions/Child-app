package com.anis.child.ml

data class FilterResult(
    val filtered: List<FloatArray>,
    val originalIndices: List<Int>
)

object DeltaFilter {

    fun filter(embeddings: List<FloatArray>, threshold: Double = 0.95): FilterResult {
        if (embeddings.isEmpty()) return FilterResult(emptyList(), emptyList())

        val filtered = mutableListOf<FloatArray>()
        val indices = mutableListOf<Int>()

        filtered.add(embeddings[0])
        indices.add(0)

        for (i in 1 until embeddings.size) {
            val sim = cosineSimilarity(embeddings[i], filtered.last())
            if (sim <= threshold) {
                filtered.add(embeddings[i])
                indices.add(i)
            }
        }

        return FilterResult(filtered, indices)
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        var dot = 0.0
        for (i in a.indices) {
            dot += a[i].toDouble() * b[i].toDouble()
        }
        return dot
    }
}
