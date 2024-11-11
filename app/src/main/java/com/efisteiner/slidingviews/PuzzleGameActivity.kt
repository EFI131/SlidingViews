package com.efisteiner.slidingviews

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PuzzleGameActivity : AppCompatActivity(),DraggableGridLayout.InteractionListener {
    companion object {
        const val TAG ="MAIN ACTIVITY"
    }
    private lateinit var model: SlidingPuzzleGame
    private lateinit var gridLayout: DraggableGridLayout
    private lateinit var bitmap: Bitmap
    private lateinit var tiles: List<Bitmap>
    private lateinit var getPuzzleResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Register the activity result launcher
        getPuzzleResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handlePuzzleSelectionResult(result)
        }

        // Check for ongoing game
        if (!isGameOngoing()) {
            // No ongoing game, start puzzle selection activity
            val intent = Intent(this, PuzzleSelectionActivity::class.java)
            getPuzzleResultLauncher.launch(intent)
        } else {
            // Continue with the ongoing game
            resumeGame()
        }



    }

    private fun resumeGame() {
        TODO("Not yet implemented")
    }

    private fun isGameOngoing(): Boolean {
        return false
    }

    private fun handlePuzzleSelectionResult(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK) {
            val puzzle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra("selected_puzzle", Puzzle::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getSerializableExtra("selected_puzzle") as? Puzzle
            }

            if (puzzle != null) {
                startGameWithPuzzle(puzzle)
            } else {
                // Handle error: puzzle data not received
                showError("Puzzle selection failed.")
            }
        } else {
            // Handle cancellation or failure
            showError("Puzzle selection was canceled.")
        }
    }

    private fun showError(message: String) {
        Log.i(TAG, "showError: $message")
    }

    private fun startGameWithPuzzle(puzzle: Puzzle) {
        Log.i(TAG, "startGameWithPuzzle: ${puzzle.size}")
        val (numRows, numCols) = when (puzzle.size){
            BoardSize.SMALL -> {
                Pair(3,3)
            }
            BoardSize.MEDIUM -> {
                Pair(4,4)
            }
            BoardSize.LARGE -> {
                Pair(5,5)
            }

            else ->Pair(3,3)
        }
        // create and populate grid
        gridLayout = DraggableGridLayout(this, numRows, numCols)
        gridLayout.id = View.generateViewId()

        // Set initial layout params with 0dp width or height for constraint-based sizing
        val params = ConstraintLayout.LayoutParams(0, 0)
        gridLayout.layoutParams = params

        val main:ConstraintLayout = findViewById<ConstraintLayout>(R.id.main)
        main.addView(gridLayout)

        // ConstraintSet
        val constraintSet = ConstraintSet()
        constraintSet.clone(main)

        // Constrain gridLayout with center positioning and apply the dimension ratio
        constraintSet.connect(gridLayout.id, ConstraintSet.START, main.id, ConstraintSet.START)
        constraintSet.connect(gridLayout.id, ConstraintSet.END, main.id, ConstraintSet.END)
        constraintSet.connect(gridLayout.id, ConstraintSet.TOP, main.id, ConstraintSet.TOP)
//        constraintSet.connect(gridLayout.id, ConstraintSet.BOTTOM, main.id, ConstraintSet.BOTTOM)


        bitmap = BitmapUtils.getBitmap(this, Uri.parse(puzzle.imageUriString))


        constraintSet.setDimensionRatio(gridLayout.id, "H,${bitmap.width.toFloat() / bitmap.height.toFloat()}")
        constraintSet.applyTo(main)
        //val gridLayout:DraggableGridLayout = findViewById<DraggableGridLayout>(R.id.gridLayout)
        model = SlidingPuzzleGame(numRows, numCols)

        // get puzzle bimap from uri and only then do


        // Use a coroutine to split the bitmap in the background
        lifecycleScope.launch(Dispatchers.IO) {
            tiles = BitmapUtils.splitBitmap(bitmap, numRows, numCols)

            withContext(Dispatchers.Main) {
                populateGrid()
            }
        }

        gridLayout.interactionListener = this
    }




    private fun populateGrid() {
        // grid view population w' ready data set
        for (row in 0 until gridLayout.numRows){
            for (col in 0 until gridLayout.numColumns){
                if (row == gridLayout.numRows - 1 && col == gridLayout.numColumns - 1) break
                // this call should be part of the model
                val view = getViewFor(model.getTileAt(row,  col))
                view?.let {
                    view.setBackgroundColor(Color.RED)
                    gridLayout.addView(view, GridLayoutParams(0,0,row, col))
                }
            }
        }
    }

    private fun getViewFor(num: Int): View? {
        if (num <=0 || num >=  tiles.size){
            return null
        }
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.tile, null)
//        val tv = view.findViewById<TextView>(R.id.textView)
//        tv.text = text
        val iv = view.findViewById<ImageView>(R.id.imageView)
        iv.setImageBitmap(tiles[num-1])
        return view
    }

    override fun onTileDragged(startRow: Int, startCol: Int, endRow: Int, endCol: Int) {
        model.move(startRow, startCol, endRow, endCol)
        if (model.isSolved()){
            // todo: add a meaningful animation, stop game, show dialog, points?, next puzzle
            gridLayout.solved = true
            gridLayout.addView(
                getViewFor(tiles[tiles.size - 1]),
                GridLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, gridLayout.numRows -1, gridLayout.numColumns -1))
        }
    }

    private fun getViewFor(bitmap: Bitmap): View {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.tile, null)
//        val tv = view.findViewById<TextView>(R.id.textView)
//        tv.text = text
        val iv = view.findViewById<ImageView>(R.id.imageView)
        iv.setImageBitmap(bitmap)
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        bitmap.recycle()
        // Also recycle the tiles if they're no longer needed
        tiles.forEach { it.recycle() }
    }

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            // Open an input stream to the Uri
            context.contentResolver.openInputStream(uri).use { inputStream ->
                // Decode the input stream into a Bitmap
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}