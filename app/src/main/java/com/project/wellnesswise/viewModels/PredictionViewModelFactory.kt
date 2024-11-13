import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.wellnesswise.viewModels.PredictionsViewModel

class PredictionsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PredictionsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}