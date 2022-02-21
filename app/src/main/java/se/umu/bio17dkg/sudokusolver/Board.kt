package se.umu.bio17dkg.sudokusolver

class Board {

    val size = 9

    /**
     * Initializes the number of cells in the board
     */
    val cells: MutableList<Cell> = MutableList(size * size) {
            i -> Cell(i / size, i % size, 0)
    }

    /**
     * Gets the selected cell and col
     */
    fun getCell(row: Int, col: Int) = cells[row * size + col]

}