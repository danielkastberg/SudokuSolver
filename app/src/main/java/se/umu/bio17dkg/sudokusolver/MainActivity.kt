package se.umu.bio17dkg.sudokusolver

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import se.umu.bio17dkg.sudokusolver.databinding.ActivityMainBinding

private const val PHOTO = "photoUri"
private const val ORIENTATION = "photoOrientation"

class MainActivity : AppCompatActivity(), CellInterface, SolverDialog.SolveDialogInterface,
    NumberPicker.OnValueChangeListener {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var boardView: BoardView
    private val sudokuViewModel: SudokuViewModel by lazy {
        ViewModelProvider(this).get(SudokuViewModel::class.java)
    }

    private var photoUri: Uri? = null
    private var photoPortrait: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.myToolbar.inflateMenu(R.menu.menu_item)

        if (!ifPortrait()) {
            sudokuViewModel.solver.selectedRow = -1
            sudokuViewModel.solver.selectedCol = -1
        }

        if (savedInstanceState != null) {
            photoPortrait = savedInstanceState.getBoolean(ORIENTATION)
        }

        boardView = binding.sudokuBoard
        boardView.registerListener(this)


        sudokuViewModel.solver.selectedCellLiveData.observe(this, Observer {
            updateSelectedCellUI(it)
        })

        sudokuViewModel.solver.cellsLiveData.observe(this, Observer {
            updateCells(it)
        })

        if (ifPortrait()) {
            createNumberButtons()
        }

        createMenuItem()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (photoUri != null) {
            outState.putString(PHOTO, photoUri.toString())
        }
        outState.putBoolean(ORIENTATION, photoPortrait)
    }

    //
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val stringUri: String? = savedInstanceState.getString(PHOTO)
        if (stringUri != null) {
            photoUri = Uri.parse(stringUri)
            binding.sudokuView.setImageBitmap(convertUriToBitmap(photoUri!!))
            if (ifPortrait() && checkScreenSize() < 2) {
                downsizeSudokuBoard(4)
            }
        }
    }

    /**
     * Gets the result from the CameraActivity. If the requestCode matches the one from
     * CameraActivity and the resultCode is OK then the Uri is added to a val and then set into
     * the ImageView.
     * @param requestCode The code from the activity
     * @param resultCode The code from the result
     * @param data The data from the other activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            photoUri = data.extras!!["photo"] as Uri
            binding.sudokuView.setImageBitmap(convertUriToBitmap(photoUri!!))

            if (ifPortrait() && checkScreenSize() < 2) {
                downsizeSudokuBoard(4)
            }
        }
    }

    /**
     * Resize the sudoku board to a smaller size.
     */
    private fun downsizeSudokuBoard(downsize: Int) {
        val boardParams: ViewGroup.LayoutParams = binding.sudokuBoard.layoutParams
        val resolution = getScreenResolution()
        val height = resolution.first
        val width = resolution.second
        if (downsize != 0) {
            boardParams.height = height - height / downsize
            boardParams.width = width - width / downsize
        }
        // Resize to original size
        else {
            boardParams.height = height
            boardParams.width = width
        }

        binding.sudokuBoard.requestLayout()
    }

    /**
     * Gets the resolution of the screen
     * @return Pair of Resolution, Height, Width
     */
    private fun getScreenResolution(): Pair<Int, Int> {
        val metrics = DisplayMetrics()

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = this.display
            display?.getRealMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            Pair(width, height)
        } else {
            @Suppress("DEPRECATION")
            val display = this.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            Pair(width, height)
        }
    }

    /**
     * Checks the size of the screen
     * @return Int depending on the size
     */
    private fun checkScreenSize(): Int {
        return when {
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_SMALL -> 0
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_NORMAL -> 1
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE -> 2
            else -> 3
        }
    }

    /**
     * Checks if the phone is held in portrait mode
     * @return Boolean true if portrait, false if landscape
     */
    private fun ifPortrait(): Boolean {
        val orientation = this.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }


    /**
     * Converts the Uri file from the CameraActivity into a bitmap.
     * A different function depending on the Api of the phone.
     * If a API of 28 or newer the ImageDecoder will be used, if the API is older
     * the getBitmap will be used.
     * @param photoUri The Uri containing the path to the photo
     */
    private fun convertUriToBitmap(photoUri: Uri): Bitmap {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(this.contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            if (photoPortrait) {
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri).rotate(90F)
            }
            else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            }
        }
    }

    /**
     * Extension to the Bitmap which rotates the photo
     * @return Bitmap Rotated bitmap
     */
    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    /**
     * Creates the items for the menu and attaches a listener to each item.
     * The solve item will open the SolveDialog and then solve the sudoku.
     * The clear item will clear the sudoku.
     * The camera will open the camera.
     * The remove photo item will empty the ImageView so the bottom of the screen is freed.
     */
    private fun createMenuItem() {
        binding.myToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.solve -> {
                    openSolveDialog()
                    true
                }
                R.id.clear -> {
                    sudokuViewModel.solver.clearBoard()
                    true
                }
                R.id.camera -> {
                    photoPortrait = ifPortrait()
                    val intent = Intent(this, CameraActivity::class.java)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(intent, 1)
                    true
                }
                R.id.remove_photo -> {
                    binding.sudokuView.setImageDrawable(null)
                    photoUri = null
                    if (ifPortrait()) {
                        downsizeSudokuBoard(0)
                    }
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Creates the number buttons and adds a function to each button. The button one will add
     * the value 1 into the sudoku and so on.
     */
    private fun createNumberButtons() {
        val res = resources;
        for (number in 1..9) {
            val resId: Int = res.getIdentifier("number_$number", "id", this.packageName)
            val numberButton: Button = binding.root.findViewById(resId)
            numberButton.setOnClickListener {
                inputNumber(number)
            }
        }
    }

    /**
     * Opens a fragment of the SolveDialog class. Uses a FragmentManager to start it.
     */
    private fun openSolveDialog() {
        val solveDialogFragment = SolverDialog()
        solveDialogFragment.show(supportFragmentManager, "solve")
    }

    /**
     * Opens a fragment of the InvalidSudoku class. Uses a FragmentManager to start it.
     */
    private fun openInvalidSudokuDialog() {
        val sudokuDialogFragment = InvalidSudokuDialog()
        sudokuDialogFragment.show(supportFragmentManager, "invalid")
    }

    /**
     * Opens a fragment of the NumberDialogPicker class. Uses a FragmentManager to start it.
     * If the selected row and selected col is not on the board the dialog doesn't open.
     */
    private fun openNumberPickerDialog() {

        if (sudokuViewModel.solver.selectedRow != -1 ||
            sudokuViewModel.solver.selectedCol != -1
        ) {
            val numberDialogFragment = NumberDialogPicker()
            numberDialogFragment.setValueChangeListener(this)
            numberDialogFragment.show(supportFragmentManager, "pick")
        }
    }

    /**
     * Inputs a number in the board. Uses a check if it's a valid move, if not a Toast is made
     * for the user, else the number is added to the board.
     */
    private fun inputNumber(number: Int) {
        when (sudokuViewModel.solver.checkSelectedNumber(number)) {
            0 -> sudokuViewModel.solver.addNumber(number)
            1 -> Toast.makeText(this, "Duplicate number in row", Toast.LENGTH_SHORT).show()
            2 -> Toast.makeText(this, "Duplicate number in column", Toast.LENGTH_SHORT)
                .show()
            3 -> Toast.makeText(this, "Duplicate number in box", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Updates the view with the selected cell as long as the parameter isn't null
     * If the phone is held in landscape the DialogPicker is also opened when a new cell
     * has been selected.
     * @param Pair<Int, Int> The selected cell
     */
    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) {
        if (cell != null) {
            boardView.updateSelectedCellUI(cell.first, cell.second)
            if (!ifPortrait()) {
                openNumberPickerDialog()
            }
        }
    }

    /**
     * If the cells isn't null, the cells on the view are updated.
     */
    private fun updateCells(cells: List<Cell>?) {
        cells?.let {
            boardView.updateCells(cells)
        }
    }


    /**
     * Override of the CellInterface function. Updates the board in the ViewModel
     * Solver class.
     */
    override fun coordinateData(row: Int, col: Int) {
        sudokuViewModel.solver.updateSelectedCell(row, col)
    }

    /**
     * Listener to the SolveDialogListener. If the user presses yes the action will be executed.
     * In this case a call to the Solver class to solve the sudoku.
     * @param dialog
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        Thread(Runnable {
            sudokuViewModel.solver.solve()
            if (sudokuViewModel.solver.unsolvable) {
                openInvalidSudokuDialog()
            }
        }).start()


    }
    /**
     * Listener to the SolveDialogListener. If the user presses no the action will be executed.
     * In this case no action will be made.
     * @param dialog
     */
    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }

    /**
     * Override of the NumberPicker listener. When a value is chosen in the number picker
     * the number is returned and sent to the inputNumber function.
     */
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        inputNumber(newVal)
    }
}