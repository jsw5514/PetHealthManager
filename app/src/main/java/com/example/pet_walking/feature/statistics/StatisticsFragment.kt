// com/example/pet_walking/statistics/StatisticsFragment.kt
/*
/**
 * 수정정 8월 22일
 */
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
}*/
// com/example/pet_walking/statistics/StatisticsFragment.kt
package com.example.pet_walking.feature.statistics

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.pet_walking.R
import com.example.pet_walking.databinding.StatisticsFragmentBinding
import com.example.pet_walking.feature.Running.model.RunStats
import com.example.pet_walking.feature.profile.repository.PetRepository
import com.example.pet_walking.feature.profile.repository.UserRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class StatisticsFragment : Fragment() {

    private lateinit var binding: StatisticsFragmentBinding
    private lateinit var barChart: BarChart

    private var currentPeriod = "daily"

    // 합계/구간 데이터 보관
    private var stats: ProfileStats = ProfileStats()

    // 서버에서 한 번 받아 캐싱 (스피너 변경 시 재계산 전용)
    private var runsCache: List<RunStats> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = StatisticsFragmentBinding.inflate(inflater, container, false)
        barChart = binding.barChart

        setupBarChart()
        setupSpinner()
        loadStatsFromServer()

        return binding.root
    }

    /** 서버에서 한 번만 받아 캐시에 저장 */
    private fun loadStatsFromServer() {
        val userId = UserRepository.getCurrentUser()?.userId ?: return
        val petId  = PetRepository.getCurrentPet()?.id?.toString() ?: return

        Log.d("StatsFragment", "📡 러닝 로그 로드 시작 user=$userId, pet=$petId")
        StatsLoader.loadRunLogs(
            userId = userId,
            petId  = petId,
            onResult = { runs ->
                runsCache = runs
                Log.d("StatsFragment", "📥 수신: ${runs.size}개 세션")
                recomputeFromCache()   // 현재 선택된 기간 기준으로 재계산/갱신
            },
            onError = { e ->
                Log.e("StatsFragment", "러닝 로그 로딩 실패", e)
            }
        )
    }

    /** 캐시(runsCache)로부터 합계/구간별 데이터 재계산 → UI 갱신 */
    private fun recomputeFromCache() {
        // 1) 전체 합계
        stats.totalDistance = runsCache.sumOf { it.distance }
        stats.totalCalories = runsCache.sumOf { it.calories }

        // 2) 그룹핑(칼로리 합)
        val grouped = runsCache.groupBy { log ->
            keyForTimestamp(log.timestamp, currentPeriod)
        }.mapValues { (_, list) -> list.sumOf { it.calories } }

        // 3) 고정 라벨 순서 생성 + 값 채우기(없으면 0)
        val orderedKeys = orderedLabels(currentPeriod)
        val orderedMap  = LinkedHashMap<String, Double>()
        for (k in orderedKeys) orderedMap[k] = grouped[k] ?: 0.0

        // 4) stats 모델 갱신
        val periodMap = stats.getMapForPeriod(currentPeriod)
        periodMap.clear()
        periodMap.putAll(orderedMap)

        // 5) UI 갱신
        requireActivity().runOnUiThread { updateUI() }
    }

    private fun updateUI() {
        binding.distanceTextView.text = "총 이동 거리: %.2f km".format(stats.totalDistance)
        binding.calorieTextView.text  = "소모 칼로리: %.2f kcal".format(stats.totalCalories)
        updateChartData(stats.getMapForPeriod(currentPeriod))
    }

    /** timestamp → 구간 key */
    private fun keyForTimestamp(ts: Long, period: String): String {
        val dt = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault())
        return when (period) {
            "daily"   -> "${dt.hour}시"                  // 0시 ~ 23시
            "weekly"  -> listOf("월","화","수","목","금","토","일")[dt.dayOfWeek.value - 1]
            "monthly" -> "${dt.dayOfMonth}일"            // 1일 ~ 말일
            "yearly"  -> "${dt.monthValue}월"            // 1월 ~ 12월
            else      -> "Unknown"
        }
    }

    /** 각 기간의 고정 라벨 순서 */
    private fun orderedLabels(period: String): List<String> = when (period) {
        "daily"   -> (0..23).map { "${it}시" }
        "weekly"  -> listOf("월","화","수","목","금","토","일")
        "monthly" -> {
            val days = YearMonth.now().lengthOfMonth()
            (1..days).map { "${it}일" }
        }
        "yearly"  -> (1..12).map { "${it}월" }
        else      -> emptyList()
    }

    private fun updateChartData(dataMap: Map<String, Double>) {
        val labels  = dataMap.keys.toList()
        val entries = labels.mapIndexed { i, k ->
            BarEntry(i.toFloat(), (dataMap[k] ?: 0.0).toFloat())
        }

        val dataSet = BarDataSet(entries, "소모 칼로리 (kcal)").apply {
            color = Color.parseColor("#42A5F5")
            valueTextColor = Color.BLACK
        }

        barChart.data = BarData(dataSet).apply { barWidth = 0.45f }
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
                    0 -> "daily"
                    1 -> "weekly"
                    2 -> "monthly"
                    3 -> "yearly"
                    else -> "daily"
                }
                // ❌ 네트워크 다시 호출하지 않고
                // ✅ 캐시 기반으로만 재계산/갱신
                recomputeFromCache()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawValueAboveBar(true)
            setFitBars(true)
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled  = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
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