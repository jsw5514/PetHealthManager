package com.example.pet_walking

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.pet_walking.databinding.StatisticsFragmentBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDateTime
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class StatisticsFragment : Fragment() {

    private lateinit var binding: StatisticsFragmentBinding
    private lateinit var barChart: BarChart
    private var currentPeriod = "daily"

    // ✅ 유저 → 펫 → 통계 구조
    private val userStatsMap = mutableMapOf<String, MutableMap<UUID, ProfileStats>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StatisticsFragmentBinding.inflate(inflater, container, false)
        barChart = binding.barChart

        setupBarChart()
        setupSpinner()
        updateUI()

        return binding.root
    }

    // ✅ 통계 갱신
    fun updateStats() {
        val currentPet = PetRepository.getCurrentPet() ?: return
        val userId = UserRepository.getCurrentUser()?.userId ?: return
        val petId = currentPet.id

        val petMap = userStatsMap.getOrPut(userId) { mutableMapOf() }
        val stats = petMap.getOrPut(petId) { ProfileStats() }

        stats.totalDistance = SharedStatsRepository.totalDistance
        stats.totalCalories = SharedStatsRepository.totalCalories

        val key = getKeyForPeriod(currentPeriod)
        val periodMap = stats.getMapForPeriod(currentPeriod)
        periodMap[key] = (periodMap[key] ?: 0.0) + SharedStatsRepository.totalCalories

        updateUI()
    }

    // ✅ UI 업데이트
    private fun updateUI() {
        val currentPet = PetRepository.getCurrentPet() ?: return
        val userId = UserRepository.getCurrentUser()?.userId ?: return

        val stats = userStatsMap[userId]?.get(currentPet.id) ?: return

        binding.distanceTextView.text = "총 이동 거리: %.2f m".format(stats.totalDistance)
        binding.calorieTextView.text = "소모 칼로리: %.2f kcal".format(stats.totalCalories)

        updateChartData(stats.getMapForPeriod(currentPeriod))
    }

    // ✅ 현재 기간 기준 키 반환
    private fun getKeyForPeriod(period: String): String {
        val now = LocalDateTime.now()
        return when (period) {
            "daily" -> "${now.hour}시"
            "weekly" -> listOf("월", "화", "수", "목", "금", "토", "일")[now.dayOfWeek.value - 1]
            "monthly" -> "${now.dayOfMonth}일"
            "yearly" -> "${now.monthValue}월"
            else -> "Unknown"
        }
    }

    // ✅ 차트 데이터 갱신
    private fun updateChartData(dataMap: Map<String, Double>) {
        val (entries, labels) = convertToBarEntries(dataMap)

        val dataSet = BarDataSet(entries, "소모 칼로리 (kcal)").apply {
            color = Color.parseColor("#42A5F5")
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f

        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.invalidate()
    }

    private fun convertToBarEntries(dataMap: Map<String, Double>): Pair<List<BarEntry>, List<String>> {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        dataMap.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }
        return Pair(entries, labels)
    }

    // ✅ 스피너 구성
    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.date_range_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dateRangeSpinner.adapter = adapter

        binding.dateRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentPeriod = when (position) {
                    0 -> "daily"
                    1 -> "weekly"
                    2 -> "monthly"
                    3 -> "yearly"
                    else -> "daily"
                }
                updateUI()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ✅ 바차트 설정
    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawValueAboveBar(true)
            setFitBars(true)
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textSize = 12f
                labelRotationAngle = -30f
            }

            legend.isEnabled = true
        }
    }

    // ✅ 통계 저장 데이터 클래스
    data class ProfileStats(
        var totalDistance: Double = 0.0,
        var totalCalories: Double = 0.0,
        val daily: LinkedHashMap<String, Double> = linkedMapOf(),
        val weekly: LinkedHashMap<String, Double> = linkedMapOf(),
        val monthly: LinkedHashMap<String, Double> = linkedMapOf(),
        val yearly: LinkedHashMap<String, Double> = linkedMapOf()
    ) {
        fun getMapForPeriod(period: String): LinkedHashMap<String, Double> {
            return when (period) {
                "daily" -> daily
                "weekly" -> weekly
                "monthly" -> monthly
                "yearly" -> yearly
                else -> daily
            }
        }
    }
}