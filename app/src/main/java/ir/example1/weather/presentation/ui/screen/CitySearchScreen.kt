package ir.example1.weather.presentation.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import ir.example1.weather.R
import ir.example1.weather.domain.model.City
import ir.example1.weather.presentation.viewmodel.CitySearchViewModel

@Composable
fun CitySearchScreen(
    viewModel: CitySearchViewModel = hiltViewModel(),
    onCitySelected: (City) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    // Location
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val settingsClient = remember { LocationServices.getSettingsClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            getLocationAndAddCity(
                context = context,
                fusedLocationClient = fusedLocationClient,
                settingsClient = settingsClient,
                onLocationResult = { lat, lon ->
                    viewModel.searchCitiesLatLon(lat, lon)
                }
            )
        } else {
            android.widget.Toast.makeText(
                context,
                "Location permission denied",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04082E))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // ===== Header =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Adding City",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Location Image =====
            AsyncImage(
                model = R.drawable.img_location,
                contentDescription = "Location",
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Search Input with GPS =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.searchCities(it.text)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                    placeholder = {
                        Text(
                            text = "Enter city name",
                            color = Color(0xFF7F8C8D)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB8C2CA),
                        unfocusedBorderColor = Color(0xFFB8C2CA),
                        focusedContainerColor = Color(0xFFFDFDFD),
                        unfocusedContainerColor = Color(0xFFFDFDFD),
                        focusedTextColor = Color(0xFF2C3E50),
                        unfocusedTextColor = Color(0xFF2C3E50),
                        cursorColor = Color(0xFF2C3E50),
                        focusedPlaceholderColor = Color(0xFF7F8C8D),
                        unfocusedPlaceholderColor = Color(0xFF7F8C8D)
                    ),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        bottomStart = 12.dp,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    singleLine = true
                )

                Card(
                    modifier = Modifier
                        .height(56.dp)
                        .width(60.dp),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 12.dp,
                        bottomEnd = 12.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEFF3F8)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    IconButton(
                        onClick = {
                            val hasFineLocation = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasFineLocation || hasCoarseLocation) {
                                getLocationAndAddCity(
                                    context = context,
                                    fusedLocationClient = fusedLocationClient,
                                    settingsClient = settingsClient,
                                    onLocationResult = { lat, lon ->
                                        viewModel.searchCitiesLatLon(lat, lon)
                                    }
                                )
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_gps),
                            contentDescription = "Use GPS",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== City List =====
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.cities) { city ->
                    CityItem(
                        city = city,
                        onClick = { onCitySelected(city) }
                    )
                }
            }
        }
    }
}

@Composable
fun CityItem(city: City, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF15205F)
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = city.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = city.country,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ======================== GPS Helper Functions ========================

private fun getLocationAndAddCity(
    context: android.content.Context,
    fusedLocationClient: FusedLocationProviderClient,
    settingsClient: SettingsClient,
    onLocationResult: (Double, Double) -> Unit
) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000
    ).build()

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)

    settingsClient.checkLocationSettings(builder.build())
        .addOnSuccessListener {
            requestCurrentLocation(
                fusedLocationClient = fusedLocationClient,
                onLocationResult = onLocationResult,
                context = context
            )
        }
        .addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                android.widget.Toast.makeText(
                    context,
                    "Please enable GPS to get your location",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Location service not available",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
}

private fun requestCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (Double, Double) -> Unit,
    context: android.content.Context
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationResult(location.latitude, location.longitude)
        } else {
            android.widget.Toast.makeText(
                context,
                "Couldn't get current location",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }.addOnFailureListener {
        android.widget.Toast.makeText(
            context,
            "Failed to get location",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}