package com.efisteiner.slidingviews

import PuzzleAdapter
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PuzzleSelectionActivity : AppCompatActivity() {
    companion object {
        const val TAG: String = "PuzzleSelectionActivity"
    }

    private var puzzle: Puzzle? = null
    private lateinit var adapter: PuzzleAdapter
    private lateinit var resourcePuzzles: MutableList<Puzzle>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selection)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.puzzleRecyclerView)

        // Set GridLayoutManager with 2 columns
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = gridLayoutManager

        val resourcePuzzles = loadResourcePuzzles()

        val allPuzzles = resourcePuzzles

        adapter = PuzzleAdapter(allPuzzles) { selectedPuzzle ->
            openPuzzle(selectedPuzzle)
        }

        // If you have spacing between items, add an ItemDecoration (optional)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(spanCount = 2, spacing = spacingInPixels, includeEdge = true)
        )
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mi_create_puzzle -> {
                if(mediaPermissionGranted(this@PuzzleSelectionActivity)){
                    launchIntentForPhotos()
                } else {
                    requestMediaPermissions()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Function to load puzzles from app resources
    private fun loadResourcePuzzles(): List<Puzzle> {
        resourcePuzzles = mutableListOf<Puzzle>()
        val puzzleImages = listOf(R.drawable.cat, R.drawable.mouse, R.drawable.hypo) // Add your resource IDs
        puzzleImages.forEachIndexed { index, resId ->
            val puzzle = Puzzle(
                id = index,
                name = "Puzzle ${index + 1}",
                imageUriString = "android.resource://$packageName/$resId",
            )
            resourcePuzzles.add(puzzle)
        }
        return resourcePuzzles
    }

    private fun openPuzzle(puzzle: Puzzle) {
        // Handle the puzzle selection
        puzzleConfigurationDialog(puzzle)

    }



    private val getImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ activityResult: ActivityResult? ->
        if (activityResult?.resultCode != Activity.RESULT_OK|| activityResult.data == null){
            Log.w(TAG, "Did not get data back from launched activity, user likely canceled flow")
        } else {
            val selectedUri : Uri? = activityResult.data?.data
            val clipData: ClipData? = activityResult.data?.clipData

            if(clipData != null){
                Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
                for (i in 0 until clipData.itemCount){
                    // make sure that uri isn't already in the list
                    // save puzzle image
                    // for now we simply adding to list
                    val puzzle = Puzzle(
                        id = resourcePuzzles.size,
                        name = "Puzzle ${resourcePuzzles.size + 1}",
                        imageUriString = clipData.getItemAt(i).uri.toString(),
                    )
                    resourcePuzzles.add(puzzle)
                }
            } else if (selectedUri != null){
                val puzzle = Puzzle(
                    id = resourcePuzzles.size,
                    name = "Puzzle ${resourcePuzzles.size + 1}",
                    imageUriString = selectedUri.toString(),
                )
                resourcePuzzles.add(puzzle)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        getImage.launch(Intent.createChooser(intent, "Choose Pics"))
    }

    // Register ActivityResult handler
    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        // Handle permission requests results
        // See the permission example in the Android platform samples: https://github.com/android/platform-samples
        if(results.containsValue(false)) {
            Log.d("PERMISSIONS", "At least one of the permissions was not granted, launching again...")
        } else {
            launchIntentForPhotos()
        }
    }

    private fun requestMediaPermissions() {
        // Permission request logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissions.launch(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO))
        } else {
            requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }

    private fun puzzleConfigurationDialog(puzzle: Puzzle) {
        val puzzleDifficultyDialog = LayoutInflater.from(this).inflate(R.layout.puzzle_difficulty_dialog, null)
        val boardSizeRadioGroup = puzzleDifficultyDialog.findViewById<RadioGroup>(R.id.rg_size)
        val boardDifficultyGroup = puzzleDifficultyDialog.findViewById<RadioGroup>(R.id.rg_difficulty)
        showAlertDialog( "Create your own memory board", puzzleDifficultyDialog, View.OnClickListener {
            puzzle.size = when ( boardSizeRadioGroup.checkedRadioButtonId ){
                R.id.rb_3by3 -> {
                    BoardSize.SMALL
                }
                R.id.rb_4by4 -> {
                    BoardSize.MEDIUM
                }
                R.id.rb_5by5 -> {
                    BoardSize.LARGE
                }

                else -> BoardSize.SMALL
            }

            puzzle.difficulty = when ( boardDifficultyGroup.checkedRadioButtonId ){
                R.id.rb_easy -> {
                    Puzzle.Difficulty.EASY
            }
                R.id.rb_medium -> {
                Puzzle.Difficulty.MEDIUM
            }
                R.id.rb_hard -> {
                Puzzle.Difficulty.HARD
            }

                else -> Puzzle.Difficulty.EASY
            }
        } )

        this.puzzle = puzzle
    }

    private fun showAlertDialog(title: String, view: View?, positiveButtonClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                val resultIntent = Intent().apply {
                    putExtra("selected_puzzle", puzzle) // Puzzle now implements Serializable
                }

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }.show()

    }

}


