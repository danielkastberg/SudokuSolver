package se.umu.bio17dkg.sudokusolver

/**
 * Interface for the selected Cell. Used for getting the coordinates
 * from the view to the model.
 */
interface CellInterface {
    fun coordinateData(row: Int, col: Int)
}