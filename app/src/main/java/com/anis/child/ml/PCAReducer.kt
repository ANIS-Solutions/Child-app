package com.anis.child.ml

import kotlin.math.sqrt

object PCAReducer {

    fun reduce(embeddings: List<FloatArray>, targetDimensions: Int = 3): Array<DoubleArray> {
        val n = embeddings.size
        require(n > 0) { "Embeddings list must not be empty" }
        require(targetDimensions in 1..n) {
            "targetDimensions must be between 1 and n=$n, got $targetDimensions"
        }

        val d = embeddings[0].size
        val data = Array(n) { i -> DoubleArray(d) { j -> embeddings[i][j].toDouble() } }

        val mean = DoubleArray(d) { j ->
            var sum = 0.0
            for (i in 0 until n) sum += data[i][j]
            sum / n
        }
        for (i in 0 until n) {
            for (j in 0 until d) {
                data[i][j] -= mean[j]
            }
        }

        val gram = computeGramMatrix(data, n, d)

        val (eigenvalues, eigenvectors) = symmetricEigenDecomposition(gram)

        val sorted = eigenvalues.indices
            .map { idx -> idx to eigenvalues[idx] }
            .sortedByDescending { (_, value) -> value }
            .take(targetDimensions)

        val reduced = Array(n) { DoubleArray(targetDimensions) }
        for (col in 0 until targetDimensions) {
            val (eigenIdx, eigenValue) = sorted[col]
            val singularValue = sqrt(maxOf(eigenValue, 0.0))
            for (row in 0 until n) {
                reduced[row][col] = eigenvectors[row][eigenIdx] * singularValue
            }
        }

        return reduced
    }

    private fun computeGramMatrix(data: Array<DoubleArray>, n: Int, d: Int): Array<DoubleArray> {
        val gram = Array(n) { DoubleArray(n) }
        for (i in 0 until n) {
            for (j in i until n) {
                var sum = 0.0
                for (k in 0 until d) {
                    sum += data[i][k] * data[j][k]
                }
                gram[i][j] = sum
                gram[j][i] = sum
            }
        }
        return gram
    }

    private fun symmetricEigenDecomposition(
        matrix: Array<DoubleArray>,
        maxIterations: Int = 200,
        tolerance: Double = 1e-12
    ): Pair<DoubleArray, Array<DoubleArray>> {
        val n = matrix.size
        val a = Array(n) { i -> matrix[i].copyOf() }
        val eigenvectors = Array(n) { i -> DoubleArray(n) { j -> if (i == j) 1.0 else 0.0 } }

        for (iter in 0 until maxIterations) {
            var maxOffDiagonal = 0.0
            var p = 0
            var q = 1

            for (i in 0 until n) {
                for (j in (i + 1) until n) {
                    val absVal = kotlin.math.abs(a[i][j])
                    if (absVal > maxOffDiagonal) {
                        maxOffDiagonal = absVal
                        p = i
                        q = j
                    }
                }
            }

            if (maxOffDiagonal < tolerance) break

            val tau = (a[q][q] - a[p][p]) / (2.0 * a[p][q])
            val t = if (tau >= 0.0) {
                1.0 / (tau + sqrt(1.0 + tau * tau))
            } else {
                -1.0 / (-tau + sqrt(1.0 + tau * tau))
            }
            val c = 1.0 / sqrt(1.0 + t * t)
            val s = t * c

            val app = a[p][p]
            val aqq = a[q][q]
            val apq = a[p][q]

            a[p][p] = c * c * app - 2.0 * s * c * apq + s * s * aqq
            a[q][q] = s * s * app + 2.0 * s * c * apq + c * c * aqq
            a[p][q] = 0.0
            a[q][p] = 0.0

            for (r in 0 until n) {
                if (r != p && r != q) {
                    val apr = a[p][r]
                    val aqr = a[q][r]
                    a[p][r] = c * apr - s * aqr
                    a[r][p] = a[p][r]
                    a[q][r] = s * apr + c * aqr
                    a[r][q] = a[q][r]
                }
            }

            for (r in 0 until n) {
                val epr = eigenvectors[r][p]
                val eqr = eigenvectors[r][q]
                eigenvectors[r][p] = c * epr - s * eqr
                eigenvectors[r][q] = s * epr + c * eqr
            }
        }

        val eigenvalues = DoubleArray(n) { i -> a[i][i] }
        return eigenvalues to eigenvectors
    }
}
