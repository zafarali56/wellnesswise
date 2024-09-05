import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.*

class DataVisualizationViewModel : ViewModel() {
    val bloodPressureData = listOf(
        Pair(Entry(0f, 120f), Entry(0f, 80f)),
        Pair(Entry(1f, 118f), Entry(1f, 78f)),
        Pair(Entry(2f, 122f), Entry(2f, 82f)),
        Pair(Entry(3f, 121f), Entry(3f, 79f)),
        Pair(Entry(4f, 119f), Entry(4f, 81f)),
        Pair(Entry(5f, 123f), Entry(5f, 83f)),
        Pair(Entry(6f, 132f), Entry(6f, 80f)),
        Pair(Entry(7f, 120f), Entry(7f, 80f)),
        Pair(Entry(8f, 119f), Entry(8f, 78f)),
        Pair(Entry(9f, 120f), Entry(9f, 73f)),
        Pair(Entry(10f, 111f), Entry(10f, 89f)),
        Pair(Entry(11f, 129f), Entry(11f, 84f)),
        Pair(Entry(12f, 123f), Entry(12f, 82f)),
        Pair(Entry(13f, 130f), Entry(13f, 81f))
    )

    val heartRateData = listOf(
        BarEntry(0f, 72f),
        BarEntry(1f, 62f),
        BarEntry(2f, 61f),
        BarEntry(3f, 73f),
        BarEntry(4f, 75f),
        BarEntry(5f, 77f),
        BarEntry(6f, 72f),
        BarEntry(7f, 92f),
        BarEntry(8f, 75f),
        BarEntry(9f, 76f),
        BarEntry(10f, 53f),
        BarEntry(11f, 90f),
        BarEntry(12f, 74f),
        BarEntry(13f, 92f)

    )

    fun getOverallHealthData(color: Int): RadarData {
        return RadarData(RadarDataSet(listOf(
            RadarEntry(80f),  // Sleep
            RadarEntry(70f),  // Diet
            RadarEntry(85f),  // Exercise
            RadarEntry(65f),  // Stress
            RadarEntry(75f)   // Hydration
        ), "Overall Health").apply {
            this.color = color
            fillColor = color
            setDrawFilled(true)
            fillAlpha = 180
        })
    }
}