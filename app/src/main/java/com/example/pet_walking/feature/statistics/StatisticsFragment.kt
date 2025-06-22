// com/example/pet_walking/statistics/StatisticsFragment.kt
package com.example.pet_walking.feature.statistics

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.pet_walking.R
import com.example.pet_walking.databinding.StatisticsFragmentBinding
import com.example.pet_walking.feature.profile.repository.PetRepository
import com.example.pet_walking.feature.profile.repository.UserRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.Instant
import java.time.ZoneId

class StatisticsFragment : Fragment() {

    private lateinit var binding:   StatisticsFragmentBinding
    private lateinit var barChart:  BarChart
    private var currentPeriod = "daily"
    private var stats: ProfileStats = ProfileStats()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = StatisticsFragmentBinding.inflate(inflater, container, false)
        barChart = binding.barChart

        setupBarChart()
        setupSpinner()
        loadStatsFromServer()

        return binding.root
    }

    private fun loadStatsFromServer() {
        val userId = UserRepository.getCurrentUser()?.userId ?: return
        val petId  = PetRepository.getCurrentPet()?.id?.toString() ?: return

        StatsLoader.loadRunLogs(userId, petId, { runs ->
            // 1) 전체 합계
            stats.totalDistance = runs.sumOf { it.distance }
            stats.totalCalories = runs.sumOf { it.calories }

            // 2) 기간별 그룹핑 + 합산 (칼로리 기준)
            val grouped = runs.groupBy { log ->
                getKeyForTimestamp(log.timestamp, currentPeriod)
            }.mapValues { entry -> entry.value.sumOf { it.calories } }

            // 3) stats 모델에 채우기
            val periodMap = stats.getMapForPeriod(currentPeriod)
            periodMap.clear()
            grouped.forEach { (key, sumCal) -> periodMap[key] = sumCal }

            // 4) UI 갱신
            requireActivity().runOnUiThread { updateUI() }
        }, { e ->
            Log.e("StatsFragment", "러닝 로그 로딩 실패", e)
        })
    }

    private fun updateUI() {
        binding.distanceTextView.text = "총 이동 거리: %.2f km".format(stats.totalDistance)
        binding.calorieTextView.text  = "소모 칼로리: %.2f kcal".format(stats.totalCalories)
        updateChartData(stats.getMapForPeriod(currentPeriod))
    }

    // timestamp→key 변환
    private fun getKeyForTimestamp(ts: Long, period: String): String {
        val dt = Instant.ofEpochMilli(ts)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        return when (period) {
            "daily"   -> "${dt.hour}시"
            "weekly"  -> listOf("월","화","수","목","금","토","일")[dt.dayOfWeek.value - 1]
            "monthly" -> "${dt.dayOfMonth}일"
            "yearly"  -> "${dt.monthValue}월"
            else      -> "Unknown"
        }
    }

    private fun updateChartData(dataMap: Map<String, Double>) {
        val entries = dataMap.entries.mapIndexed { i, e ->
            BarEntry(i.toFloat(), e.value.toFloat())
        }
        val labels = dataMap.keys.toList()

        val dataSet = BarDataSet(entries, "소모 칼로리 (kcal)").apply {
            color = Color.parseColor("#42A5F5")
        }
        barChart.data = BarData(dataSet).apply { barWidth = 0.4f }
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.invalidate()
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.date_range_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.dateRangeSpinner.adapter = adapter
        }
        binding.dateRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                currentPeriod = when (pos) {
                    0 -> "daily"; 1 -> "weekly"
                    2 -> "monthly"; 3 -> "yearly"
                    else -> "daily"
                }
                Log.d("StatsFragment", "📡 loadStatsFromServer() 호출됨")
                loadStatsFromServer()
                Log.d("StatsFragment", "📡 loadStatsFromServer() 호출됨")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled    = false
            setDrawValueAboveBar(true)
            setFitBars(true)
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled  = false
            xAxis.apply {
                position         = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity      = 1f
                labelRotationAngle = -30f
            }
            legend.isEnabled = true
        }
    }

    data class ProfileStats(
        var totalDistance: Double = 0.0,
        var totalCalories: Double = 0.0,
        val daily:   LinkedHashMap<String, Double> = linkedMapOf(),
        val weekly:  LinkedHashMap<String, Double> = linkedMapOf(),
        val monthly: LinkedHashMap<String, Double> = linkedMapOf(),
        val yearly:  LinkedHashMap<String, Double> = linkedMapOf()
    ) {
        fun getMapForPeriod(period: String) = when (period) {
            "daily"   -> daily
            "weekly"  -> weekly
            "monthly" -> monthly
            "yearly"  -> yearly
            else      -> daily
        }
    }
}