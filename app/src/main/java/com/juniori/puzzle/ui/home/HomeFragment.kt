package com.juniori.puzzle.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.location.LocationListenerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.juniori.puzzle.R
import com.juniori.puzzle.adapter.WeatherRecyclerViewAdapter
import com.juniori.puzzle.data.Resource
import com.juniori.puzzle.databinding.FragmentHomeBinding
import com.juniori.puzzle.databinding.LoadingLayoutBinding
import com.juniori.puzzle.util.DialogManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val random = Random(System.currentTimeMillis())
    private val homeViewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var dialogManager: DialogManager
    private val locationManager: LocationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private lateinit var adapter: WeatherRecyclerViewAdapter
    private val geoCoder: Geocoder by lazy {
        Geocoder(requireContext())
    }
    private val locationListener = object : LocationListenerCompat {
        override fun onLocationChanged(loc: Location) {
            getWeatherInfo(loc.latitude, loc.longitude)
        }

        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
            homeViewModel.setWeatherInfoText(getString(R.string.location_service_off))
        }

        override fun onProviderEnabled(provider: String) {
            super.onProviderEnabled(provider)
            getWeatherByLocation()
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isPermitted ->
        if (isPermitted) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                homeViewModel.setUiState(Resource.Loading)
                getWeatherByLocation()
            } else {
                homeViewModel.setWeatherInfoText(getString(R.string.location_service_off))
            }
        } else {
            homeViewModel.setWeatherInfoText(getString(R.string.location_permission))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            vm = homeViewModel
        }
        dialogManager.createLoadingDialog(LoadingLayoutBinding.inflate(inflater, container, false).root)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val welcomeTextArray = resources.getStringArray(R.array.welcome_text)
        checkPermission()

        adapter = WeatherRecyclerViewAdapter()

        binding.weatherNotPermittedLayout.setOnClickListener {
            checkPermission()
        }

        binding.weatherRefreshBtn.setOnClickListener {
            checkPermission()
        }

        binding.weatherDetailRecyclerView.adapter = adapter

        homeViewModel.run {
            setWelcomeText(welcomeTextArray.random(random))
            setDisplayName()
            weatherInfoText.observe(viewLifecycleOwner) { text ->
                binding.weatherLayout.isVisible = text.isEmpty()
                binding.weatherNotPermittedLayout.isVisible = text.isNotEmpty()
            }

            uiState.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        dialogManager.dismissDialog()
                    }
                    is Resource.Failure -> {

                    }
                    is Resource.Loading -> {
                        dialogManager.showDialog()
                    }
                    is Resource.Empty -> TODO()
                    is Resource.Wait -> {
                        dialogManager.dismissDialog()
                    }
                }
            }
        }

    }

    private fun checkPermission(){
        homeViewModel.setUiState(Resource.Wait)
        locationPermissionRequest.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun getWeatherByLocation() {
        var location = locationManager.getLastKnownLocation(
            LocationManager.GPS_PROVIDER
        )
        if (location == null) {
            location =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_MIN_TIME_INTERVAL,
            LOCATION_MIN_DISTANCE_INTERVAL,
            locationListener
        )
        val latitude = location?.latitude ?: DEFAULT_LATITUDE
        val longitude = location?.longitude ?: DEFAULT_LONGITUDE
        getWeatherInfo(latitude, longitude)
    }

    private fun getWeatherInfo(latitude: Double, longitude: Double) {
        val address = geoCoder.getFromLocation(latitude, longitude, ADDRESS_MAX_RESULT)
        homeViewModel.setCurrentAddress(address)
        homeViewModel.getWeather(latitude, longitude)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationManager.removeUpdates(locationListener)
        _binding = null
    }

    companion object {
        private const val DEFAULT_LATITUDE = 37.0
        private const val DEFAULT_LONGITUDE = 127.0
        private const val LOCATION_MIN_TIME_INTERVAL = 3000L
        private const val LOCATION_MIN_DISTANCE_INTERVAL = 30f
        private const val ADDRESS_MAX_RESULT = 1
    }
}
