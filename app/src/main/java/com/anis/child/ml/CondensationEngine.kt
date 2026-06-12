package com.anis.child.ml

class CondensationEngine(
    private val similarityThreshold: Double = 0.95,
    private val pcaDimensions: Int = 3
) {

    fun extractKeyframes(embeddings: List<FloatArray>, k: Int = 3): List<Int> {
        if (embeddings.isEmpty()) return emptyList()
        if (embeddings.size == 1) return listOf(0)

        val filterResult = DeltaFilter.filter(embeddings, similarityThreshold)

        if (filterResult.filtered.size <= k) {
            return filterResult.originalIndices
        }

        val reduced = PCAReducer.reduce(filterResult.filtered, pcaDimensions)

        val medoidIndices = KMedoidsClusterer.cluster(reduced, k)

        return medoidIndices.map { filterResult.originalIndices[it] }
    }
}
