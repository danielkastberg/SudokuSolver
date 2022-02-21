package se.umu.bio17dkg.sudokusolver


import androidx.lifecycle.MutableLiveData

private const val EMPTY = 0

class Solver {
    var selectedRow = -1
    var selectedCol = -1
    var cellsLiveData = MutableLiveData<List<Cell>>()
    val selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var unsolvable: Boolean = false

    private var board: Board
    private var size = 0
    private var counter: Int = 0

    /**
     * Creates a new board and gets the size of the board.
     * Fills the MutableLiveData list with the cells and selects a
     * row and column for the selectedCellLiveData
     */
    init {
        board = Board()
        size = board.size
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.cells)
    }

    /**
     * Adds the number in the selected row and column.
     * If the number matches the number already in place,
     * the values is changed to a zero.
     * @param number The number to be added.
     */
    fun addNumber(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) {
            return
        }
        if (board.getCell(selectedRow, selectedCol).value == number) {
            board.getCell(selectedRow, selectedCol).value = 0
        } else {
            board.getCell(selectedRow, selectedCol).value = number
        }
        cellsLiveData.postValue(board.cells)

    }

    /**
     * Updates the selected row and column and adds it to the MutableLiveData
     */
    fun updateSelectedCell(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        selectedCellLiveData.postValue(Pair(row, col))
    }

    /**
     * Creates a new board which clears the board of all the numbers
     */
    fun clearBoard() {
        board = Board()
        cellsLiveData.postValue(board.cells)

    }

    /**
     * Solves the sudoku.
     * @param The sudoku to be solved
     * @return true if solved false if unsolvable
     */
    fun solve(): Boolean {
        counter++
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board.getCell(r, c).value == 0) {
                    for (number in 1..9) {
                        if (!isInRow(number, r) && !isInCol(number, c) &&
                            !isInBox(number, r, c)) {
                            board.getCell(r, c).value = number
                            if (solve()) {
                                cellsLiveData.postValue(board.cells)
                                return true
                            } else {
                                board.getCell(r, c).value = 0
                            }
                        }
                    }
                    if (counter > 10000000) {
                        unsolvable = true
                        return true
                    }
                    return false
                }
            }
        }
        return true
    }

    /**
     * Find the number in the selected row. Loops through all the rows.
     * @param row The selected row
     * @param number The selected number.
     * @return Boolean true or false
     */
    private fun isInRow(number: Int, row: Int): Boolean {
        for (c in 0 until size) {
            if (board.getCell(row, c).value == number) {
                return true
            }
        }
        return false
    }

    /**
     * Find the number in the selected column. Loops through all the columns.
     * @param row The selected column
     * @param number The selected number.
     * @return Boolean true or false
     */
    private fun isInCol(number: Int, col: Int): Boolean {
        for (r in 0 until size) {
            if (board.getCell(r, col).value == number) {
                return true
            }
        }
        return false
    }

    /**
     * Find the number for each box by using the modulus operator.
     * Will return the same number for each box regardless of which row or column
     * inside the box is chosen, but will use a unique number for each box.
     * If the number is found inside the box, true will be returned, else
     * false will be returned.
     * @return Boolean true or false
     */
    private fun isInBox(number: Int, row: Int, col: Int): Boolean {
        val r = row - row % 3
        val c = col - col % 3
        for (i in r until r + 3) {
            for (j in c until c + 3) {
                if (board.getCell(i, j).value == number) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks the number and if if it's valid to enter inside the board.
     * If it's a duplicate number the value is changed to 0.
     * @return Int the Result
     */
    fun checkSelectedNumber(number: Int): Int {
        var result: Int = 0

        if (selectedRow == -1 || selectedCol == -1) {
            return -1
        }

        if (number != 0) {

            when {
                isInRow(number, selectedRow) -> {
                    result = 1
                }
                isInCol(number, selectedCol) -> {
                    result = 2
                }
                isInBox(number, selectedRow, selectedCol) -> {
                    result = 3
                }
            }
            // Duplicate number
            if (board.getCell(selectedRow, selectedCol).value == number) {
                result = 0
            }
        }
        return result
    }

    /**
     * FOR DEBUGGING
     */
    fun printSudoku() {
        println("Solution: ")
        for (i in 0 until board.size) {
            for (j in 0 until board.size) {
                print(" " + board.getCell(i, j).value)
            }
            println()
        }
    }
}