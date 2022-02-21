package se.umu.bio17dkg.sudokusolver

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class BoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private val size: Int = 9
    private var selectedRow: Int = -1
    private var selectedCol: Int = -1

    private var cells: List<Cell>? = null

    private var boardColor = 0
    private var cellSelectColor = 0
    private var cellNeighbourColor = 0
    private var numberColor = 0
    private var numberColorSolver = 0

    private val boardColorPaint: Paint = Paint()
    private val cellSelectColorPaint: Paint = Paint()
    private val cellNeighbourColorPaint: Paint = Paint()
    private val numberColorPaint: Paint = Paint()
    private val numberColorBounds: Rect = Rect()

    private var typedArray: TypedArray =
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.Board, 0, 0)
    private var cellsize: Float = 0F
    private val squareRoot = 3

    private var listener: CellInterface? = null

    /**
     * Gets the style resources from the attrs.xml file and inserts the values into the
     * different colors using the TypedArray.
     */
    init {
        try {
            boardColor = typedArray.getInteger(R.styleable.Board_boardColor, 0)
            cellSelectColor = typedArray.getInteger(R.styleable.Board_cellSelectColor, 0)
            cellNeighbourColor = typedArray.getInteger(R.styleable.Board_cellNeighbourColor, 0)
            numberColor = typedArray.getInteger(R.styleable.Board_numberColor, 0)
            numberColorSolver = typedArray.getInteger(R.styleable.Board_numberColorSolver, 0)
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * Override the measure method uses the minimum value from the height and width
     * to calculate the dimension. The dimension is then used to calculate the cellsize.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val dimension = measuredWidth.coerceAtMost(measuredHeight)
        cellsize = (dimension / 9).toFloat()
        setMeasuredDimension(dimension, dimension)
    }

    /**
     * Draws the whole board and calls the different methods used to draw the numbers, cells.
     * @param canvas The canvas to draw
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        boardColorPaint.style = Paint.Style.STROKE
        boardColorPaint.strokeWidth = 16F
        boardColorPaint.color = boardColor
        boardColorPaint.isAntiAlias = true

        cellNeighbourColorPaint.style = Paint.Style.FILL
        cellNeighbourColorPaint.color = cellNeighbourColor
        cellNeighbourColorPaint.isAntiAlias = true

        cellSelectColorPaint.style = Paint.Style.FILL
        cellSelectColorPaint.color = cellSelectColor
        cellSelectColorPaint.isAntiAlias = true

        numberColorPaint.style = Paint.Style.FILL
        numberColorPaint.color = numberColor
        numberColorPaint.isAntiAlias = true



        canvas?.drawRect(0F, 0F, width.toFloat(), height.toFloat(), boardColorPaint)


        fillCells(canvas)
        drawNumber(canvas)
        drawBoard(canvas)
    }

    /**
     * Override of the OnTouchEvent, and if the screen is touched then selected row is calculated
     * using the x-coordinate and the cellsize, while the selected column is calculated
     * the y-coordinate and the cellsize and true. If no touch is recognized the false is returned.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                handleTouchEvent(event.x, event.y)
                true
            }
            else -> false
        }
    }

    /**
     * The selected row is calculated using the x-coordinate and the cellsize,
     * while the selected column is calculated the y-coordinate and the cellsize
     */
    private fun handleTouchEvent(x: Float, y: Float) {
        val touchedRow = (y / cellsize).toInt()
        val touchedCol = (x / cellsize).toInt()
        listener?.coordinateData(touchedRow, touchedCol)
    }

    /**
     * Checks where the number isn't zero and uses that to draws a number on the
     * row and column where the number was found from the sudoku. Checks the width and height
     * by measuring the text
     * @param canvas The canvas to draw
     */
    private fun drawNumber(canvas: Canvas?) {
        numberColorPaint.textSize = cellsize

        cells?.forEach {
            val r = it.row
            val c = it.col

            if (it.value != 0) {
                val number = it.value.toString()
                numberColorPaint.getTextBounds(number, 0, number.length, numberColorBounds)
                val width: Float = numberColorPaint.measureText(number)
                val height: Float = numberColorBounds.height().toFloat()
                canvas?.drawText(
                    number,
                    (c * cellsize + ((cellsize - width) / 2)),
                    (r * cellsize + cellsize) - ((cellsize - height) / 2),
                    numberColorPaint
                )
            }
        }
    }


    private fun fillCell(canvas: Canvas?, r: Int, c: Int, paint: Paint) {
        canvas?.drawRect(c * cellsize, r * cellsize, (c + 1) * cellsize, (r + 1) * cellsize, paint)
    }

    /**
     * Fills the cells with color in the same row and the same column.
     * Uses a cellNeighbourColorPaint for the neighbours.
     * Also paints the selected row and column Uses a cellSelectorColorPaint for the
     * selected row.
     * @param canvas The canvas to draw
     */
    private fun fillCells(canvas: Canvas?) {
        if (selectedRow == -1 || selectedCol == -1) {
            return
        }
        cells?.forEach {
            val r = it.row
            val c = it.col

            if (selectedRow == r && selectedCol == c) {
                fillCell(canvas, r, c, cellSelectColorPaint)
            } else if (selectedRow == r || selectedCol == c) {
                fillCell(canvas, r, c, cellNeighbourColorPaint)
            }
        }
    }

    /**
     * Draws a thick line.
     */
    private fun drawThickLine() {
        boardColorPaint.style = Paint.Style.STROKE
        boardColorPaint.strokeWidth = 10F
        boardColorPaint.color = boardColor
    }


    /**
     * Draws a thin line.
     */
    private fun drawThinLine() {
        boardColorPaint.style = Paint.Style.STROKE
        boardColorPaint.strokeWidth = 4F
        boardColorPaint.color = boardColor
    }

    /**
     * Draws up the whole sudoku board.
     * @param canvas The canvas to draw
     */
    private fun drawBoard(canvas: Canvas?) {
        for (r in 0..size) {
            if (r % squareRoot == 0) {
                drawThickLine()
            } else {
                drawThinLine()
            }
            canvas?.drawLine(
                0F, (cellsize * r),
                width.toFloat(), (cellsize * r), boardColorPaint,
            )
        }

        for (c in 0..size) {
            if (c % squareRoot == 0) {
                drawThickLine()
            } else {
                drawThinLine()
            }
            canvas?.drawLine(
                (cellsize * c), 0F, (cellsize * c),
                width.toFloat(), boardColorPaint
            )
        }
    }

    /**
     * Updates the selected row and selected column to the chosen ones.
     * @param row Int
     * @param col Int
     */
    fun updateSelectedCellUI(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        invalidate()
    }

    /**
     * Updates the cells
     * @param cells: List<Cell>
     */
    fun updateCells(cells: List<Cell>) {
        this.cells = cells
        invalidate()
    }

    /**
     * Registers the listener for the CellInterface
     */
    fun registerListener(listener: CellInterface) {
        this.listener = listener
    }
}