import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efisteiner.slidingviews.Puzzle
import com.efisteiner.slidingviews.R

class PuzzleAdapter(
    private val puzzles: List<Puzzle>,
    private val onPuzzleClick: (Puzzle) -> Unit
) : RecyclerView.Adapter<PuzzleAdapter.PuzzleViewHolder>() {

    inner class PuzzleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.puzzle_image)
        val textView: TextView = itemView.findViewById(R.id.puzzle_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuzzleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_puzzle, parent, false)
        return PuzzleViewHolder(view)
    }

    override fun onBindViewHolder(holder: PuzzleViewHolder, position: Int) {
        val puzzle = puzzles[position]
        holder.textView.text = puzzle.name

        val context = holder.itemView.context

        holder.imageView.setImageURI(Uri.parse(puzzle.imageUriString))


        holder.itemView.setOnClickListener {
            onPuzzleClick(puzzle)
        }
    }


    override fun getItemCount(): Int = puzzles.size
}
