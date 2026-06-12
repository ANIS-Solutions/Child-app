package com.anis.child.ml

import kotlin.math.sqrt

object KMedoidsClusterer {

    fun cluster(points: Array<DoubleArray>, k: Int): List<Int> {
        val n = points.size
        require(n > 0) { "Points array must not be empty" }
        require(k in 1..n) { "k must be between 1 and n=$n, got $k" }

        if (k == n) return (0 until n).toList()
        if (k == 1) return listOf(findCenteroid(points))

        val distances = computePairwiseDistances(points, n)

        val medoids = buildInitialMedoids(distances, n, k)

        val clusters = IntArray(n) { -1 }
        swapPhase(distances, medoids, clusters, n, k)

        return medoids.toList()
    }

    private fun findCenteroid(points: Array<DoubleArray>): Int {
        var bestIdx = 0
        var bestSum = Double.MAX_VALUE
        for (i in points.indices) {
            var sum = 0.0
            for (j in points.indices) {
                if (i != j) sum += euclideanDistance(points[i], points[j])
            }
            if (sum < bestSum) {
                bestSum = sum
                bestIdx = i
            }
        }
        return bestIdx
    }

    private fun computePairwiseDistances(points: Array<DoubleArray>, n: Int): Array<DoubleArray> {
        val distances = Array(n) { DoubleArray(n) }
        for (i in 0 until n) {
            for (j in (i + 1) until n) {
                val d = euclideanDistance(points[i], points[j])
                distances[i][j] = d
                distances[j][i] = d
            }
        }
        return distances
    }

    private fun euclideanDistance(a: DoubleArray, b: DoubleArray): Double {
        var sum = 0.0
        for (i in a.indices) {
            val diff = a[i] - b[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }

    private fun buildInitialMedoids(
        distances: Array<DoubleArray>,
        n: Int,
        k: Int
    ): MutableList<Int> {
        val medoids = mutableListOf<Int>()

        var bestIdx = 0
        var bestSum = Double.MAX_VALUE
        for (i in 0 until n) {
            var sum = 0.0
            for (j in 0 until n) {
                sum += distances[i][j]
            }
            if (sum < bestSum) {
                bestSum = sum
                bestIdx = i
            }
        }
        medoids.add(bestIdx)

        val isMedoid = BooleanArray(n)
        isMedoid[bestIdx] = true

        while (medoids.size < k) {
            bestIdx = -1
            var bestGain = 0.0

            for (candidate in 0 until n) {
                if (isMedoid[candidate]) continue
                var gain = 0.0
                for (j in 0 until n) {
                    if (isMedoid[j]) continue
                    val dToCandidate = distances[j][candidate]
                    val dToNearestMedoid = (0 until medoids.size).minOf { distances[j][medoids[it]] }
                    if (dToCandidate < dToNearestMedoid) {
                        gain += dToNearestMedoid - dToCandidate
                    }
                }
                if (gain > bestGain) {
                    bestGain = gain
                    bestIdx = candidate
                }
            }

            if (bestIdx == -1) {
                for (i in 0 until n) {
                    if (!isMedoid[i]) {
                        bestIdx = i
                        break
                    }
                }
            }

            medoids.add(bestIdx)
            isMedoid[bestIdx] = true
        }

        return medoids
    }

    private fun swapPhase(
        distances: Array<DoubleArray>,
        medoids: MutableList<Int>,
        clusters: IntArray,
        n: Int,
        k: Int
    ) {
        val isMedoid = BooleanArray(n)
        for (m in medoids) isMedoid[m] = true

        assignClusters(distances, medoids, clusters, n)

        var improved = true
        var iteration = 0
        val maxIterations = 100

        while (improved && iteration < maxIterations) {
            improved = false
            iteration++

            for (mi in medoids.indices) {
                val currentMedoid = medoids[mi]

                for (candidate in 0 until n) {
                    if (isMedoid[candidate]) continue

                    val delta = computeSwapDelta(distances, medoids, clusters, mi, candidate, isMedoid, n)

                    if (delta < -1e-12) {
                        isMedoid[currentMedoid] = false
                        isMedoid[candidate] = true
                        medoids[mi] = candidate
                        assignClusters(distances, medoids, clusters, n)
                        improved = true
                        break
                    }
                }

                if (improved) break
            }
        }
    }

    private fun computeSwapDelta(
        distances: Array<DoubleArray>,
        medoids: List<Int>,
        clusters: IntArray,
        medoidIndex: Int,
        candidate: Int,
        isMedoid: BooleanArray,
        n: Int
    ): Double {
        var delta = 0.0
        val oldMedoid = medoids[medoidIndex]

        for (j in 0 until n) {
            if (j == oldMedoid || j == candidate) continue

            val dToCandidate = distances[j][candidate]
            val assignedMedoid = medoids[clusters[j]]

            if (assignedMedoid == oldMedoid) {
                var secondBest = Double.MAX_VALUE
                for (m in medoids) {
                    if (m == oldMedoid) continue
                    val d = distances[j][m]
                    if (d < secondBest) secondBest = d
                }
                val newDist = minOf(dToCandidate, secondBest)
                val oldDist = distances[j][oldMedoid]
                delta += newDist - oldDist
            } else {
                val oldDist = distances[j][assignedMedoid]
                if (dToCandidate < oldDist) {
                    delta += dToCandidate - oldDist
                }
            }
        }

        return delta
    }

    private fun assignClusters(
        distances: Array<DoubleArray>,
        medoids: List<Int>,
        clusters: IntArray,
        n: Int
    ) {
        for (j in 0 until n) {
            var bestDist = Double.MAX_VALUE
            var bestMedoid = 0
            for (mi in medoids.indices) {
                val d = distances[j][medoids[mi]]
                if (d < bestDist) {
                    bestDist = d
                    bestMedoid = mi
                }
            }
            clusters[j] = bestMedoid
        }
    }
}
