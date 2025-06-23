package com.example.pet_walking.feature.Running.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.R
import com.example.pet_walking.feature.Running.Map.MapFragment
import com.example.pet_walking.feature.Running.Map.PathManager
import com.example.pet_walking.feature.Running.Map.SnapToRoadsService
import com.naver.maps.geometry.LatLng

class RunSummaryFragment : Fragment() {

    private lateinit var distanceText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var timeText: TextView
    private lateinit var backButton: Button
    private lateinit var mapFragment: MapFragment
    private var totalDistance: Double = 0.0
    private var totalCalories: Double = 0.0
    private var durationMin: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_run_summary, container, false)

        distanceText = view.findViewById(R.id.summaryDistance)
        caloriesText = view.findViewById(R.id.summaryCalories)
        timeText = view.findViewById(R.id.summaryTime)
        backButton = view.findViewById(R.id.buttonBackToRun)

        // 데이터 전달 받기
        arguments?.let {
            totalDistance = it.getDouble("distance", 0.0)
            totalCalories = it.getDouble("calories", 0.0)
            durationMin = it.getLong("durationMin", 0L)
        }

        distanceText.text = "총 거리: %.2f km".format(totalDistance)
        caloriesText.text = "소모 칼로리: %.0f kcal".format(totalCalories)
        timeText.text = "운동 시간: ${durationMin}분"

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_runSummaryFragment_to_runningFragment)
        }

        // MapFragment 로딩
        mapFragment = MapFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Roads API 보정 다시 적용
        val apiKey = getString(R.string.google_roads_api_key)
        val originalPath: List<LatLng> = PathManager.getPoints()

        SnapToRoadsService.snapToRoads(
            path = originalPath,
            apiKey = apiKey,
            onResult = { correctedPath ->
                mapFragment.drawCorrectedPath(correctedPath)
            },
            onError = { error ->
                requireActivity().runOnUiThread{
                    //Toast.makeText(requireContext(), "경로 보정 실패: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}