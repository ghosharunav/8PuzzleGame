package com.example.eightpuzzle

import java.util.PriorityQueue

enum class GameMode {
    MODE1, // Goal: 1,2,3,4,5,6,7,8,0
    MODE2  // Goal: 0,8,7,6,5,4,3,2,1
}

class PuzzleManager {

    companion object {
        const val SIZE = 3

        val GOAL_MODE1 = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val GOAL_MODE2 = intArrayOf(0, 8, 7, 6, 5, 4, 3, 2, 1)
    }

    var mode: GameMode = GameMode.MODE1

    val goalState: IntArray
        get() = if (mode == GameMode.MODE1) GOAL_MODE1 else GOAL_MODE2

    var tiles: IntArray = GOAL_MODE1.copyOf()
        private set

    var emptyIndex: Int = 8
        private set

    var moveCount: Int = 0
        private set

    // ─── Shuffle ─────────────────────────────────────────────────────────────

    fun shuffle() {
        moveCount = 0
        do {
            tiles = (0..8).shuffled().toIntArray()
            emptyIndex = tiles.indexOf(0)
        } while (!isSolvable() || isSolved())
    }

    // ─── Solvability ─────────────────────────────────────────────────────────

    fun isSolvable(): Boolean {
        // Count inversions relative to the goal ordering
        val goalOrder = goalState
        val goalRank = IntArray(9)
        for (i in goalOrder.indices) goalRank[goalOrder[i]] = i

        val ranked = tiles.map { goalRank[it] }
        var inversions = 0
        for (i in ranked.indices) {
            if (ranked[i] == goalRank[0]) continue // skip blank
            for (j in i + 1 until ranked.size) {
                if (ranked[j] == goalRank[0]) continue
                if (ranked[i] > ranked[j]) inversions++
            }
        }
        return inversions % 2 == 0
    }

    // ─── Move ─────────────────────────────────────────────────────────────────

    fun moveTile(tappedIndex: Int): Boolean {
        if (!isAdjacentToEmpty(tappedIndex)) return false
        tiles[emptyIndex] = tiles[tappedIndex]
        tiles[tappedIndex] = 0
        emptyIndex = tappedIndex
        moveCount++
        return true
    }

    private fun isAdjacentToEmpty(index: Int): Boolean {
        val emptyRow = emptyIndex / SIZE
        val emptyCol = emptyIndex % SIZE
        val tileRow = index / SIZE
        val tileCol = index % SIZE
        return (emptyRow == tileRow && Math.abs(emptyCol - tileCol) == 1) ||
                (emptyCol == tileCol && Math.abs(emptyRow - tileRow) == 1)
    }

    fun isSolved(): Boolean = tiles.contentEquals(goalState)

    // ─── Optimal Moves Remaining (A* with Manhattan Distance heuristic) ───────

    fun optimalMovesRemaining(): Int {
        if (isSolved()) return 0
        return aStarSolve(tiles, goalState) ?: -1
    }

    private fun manhattanDistance(state: IntArray, goal: IntArray): Int {
        // Build goal position map: value → (row, col)
        val goalPos = Array(9) { Pair(0, 0) }
        for (i in goal.indices) goalPos[goal[i]] = Pair(i / SIZE, i % SIZE)

        var dist = 0
        for (i in state.indices) {
            val v = state[i]
            if (v == 0) continue
            val curRow = i / SIZE
            val curCol = i % SIZE
            val (gr, gc) = goalPos[v]
            dist += Math.abs(curRow - gr) + Math.abs(curCol - gc)
        }
        return dist
    }

    private data class Node(
        val state: IntArray,
        val g: Int,       // moves so far
        val h: Int,       // heuristic
        val emptyIdx: Int
    ) : Comparable<Node> {
        val f get() = g + h
        override fun compareTo(other: Node) = this.f.compareTo(other.f)
        override fun equals(other: Any?): Boolean {
            if (other !is Node) return false
            return state.contentEquals(other.state)
        }
        override fun hashCode() = state.contentHashCode()
    }

    private fun aStarSolve(start: IntArray, goal: IntArray): Int? {
        val startH = manhattanDistance(start, goal)
        val startNode = Node(start.copyOf(), 0, startH, start.indexOf(0))

        val openSet = PriorityQueue<Node>()
        openSet.add(startNode)

        // visited stores best g score seen for each state
        val visited = HashMap<String, Int>()
        visited[start.toList().toString()] = 0

        val dx = intArrayOf(-1, 1, 0, 0)
        val dy = intArrayOf(0, 0, -1, 1)

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()!!

            if (current.state.contentEquals(goal)) return current.g

            // Safety cap to avoid ANR on very complex states
            if (current.g > 50) continue

            val eRow = current.emptyIdx / SIZE
            val eCol = current.emptyIdx % SIZE

            for (d in 0 until 4) {
                val nRow = eRow + dx[d]
                val nCol = eCol + dy[d]
                if (nRow < 0 || nRow >= SIZE || nCol < 0 || nCol >= SIZE) continue

                val neighborIdx = nRow * SIZE + nCol
                val newState = current.state.copyOf()
                newState[current.emptyIdx] = newState[neighborIdx]
                newState[neighborIdx] = 0

                val newG = current.g + 1
                val key = newState.toList().toString()

                if (visited.getOrDefault(key, Int.MAX_VALUE) <= newG) continue
                visited[key] = newG

                val newH = manhattanDistance(newState, goal)
                openSet.add(Node(newState, newG, newH, neighborIdx))
            }
        }
        return null
    }
    // Public wrapper so MainActivity can call A* on a snapshot
    fun aStarPublic(start: IntArray, goal: IntArray): Int? {
        return aStarSolve(start, goal)
    }
}