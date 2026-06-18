package ir.example1.weather.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import ir.example1.weather.R
import ir.example1.weather.domain.model.City
import ir.example1.weather.presentation.ui.utils.WeatherIconMapper
import ir.example1.weather.presentation.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherApp(cityToSave: City?) {
    val navController = rememberNavController()
    val viewModel: WeatherViewModel = hiltViewModel()

    LaunchedEffect(cityToSave) {
        cityToSave?.let {
            viewModel.saveSelectedCity(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitialWeather()
    }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onAddCityClick = { navController.navigate("search") },
                onRefreshClick = { viewModel.refreshWeather() }
            )
        }
        composable("search") {
            CitySearchScreen(
                onCitySelected = { city ->
                    viewModel.saveSelectedCity(city)
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun darkColorScheme() = darkColorScheme(
    background = Color(0xFF04082E),
    surface = Color(0xFF1B1E48),
    primary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

// ======================== Main Screen ========================

@Composable
fun MainScreen(
    viewModel: WeatherViewModel = viewModel(),
    onAddCityClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    var showCityDropdown by remember { mutableStateOf(false) }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Refresh Button
                Card(
                    modifier = Modifier.size(35.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF04082D)
                    )
                ) {
                    IconButton(
                        onClick = onRefreshClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.refresh),
                            contentDescription = "Refresh",
                            tint = Color(0xFF2B8FDC)

                        )
                    }
                }

                // City Name
                Text(
                    text = uiState.currentWeather?.cityName ?: "No city added",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),

                    textAlign = TextAlign.Center,
                    maxLines = 1

                )

                // More Cities Dropdown
                Box {
                    Card(
                        modifier = Modifier.size(35.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF04082D)
                        )
                    ) {
                        IconButton(
                            onClick = { showCityDropdown = !showCityDropdown },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_down),
                                contentDescription = "More cities",
                                tint = Color(0xFF2B8FDC)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCityDropdown,
                        onDismissRequest = { showCityDropdown = false },
                        modifier = Modifier
                            .width(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B1E48)),
                        containerColor = Color(0xFF1B1E48)
                    ) {
                        if (uiState.savedCities.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "No saved cities",
                                        color = Color.White
                                    )
                                },
                                onClick = { showCityDropdown = false }
                            )
                        } else {
                            uiState.savedCities.forEach { city ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${city.name}, ${city.country}",
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteCity(city.id)
                                                    showCityDropdown = false
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.icn_delete),
                                                    contentDescription = "Delete",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectCity(city.id)
                                        showCityDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Add City Button
                Card(
                    modifier = Modifier.size(35.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF04082D)
                    )
                ) {
                    IconButton(
                        onClick = onAddCityClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "Add city",
                            tint = Color(0xFF2B8FDC)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Content =====
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "About Today",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                uiState.currentWeather?.let { weather ->
                    item {
                        TodayWeatherSection(weather = weather)
                    }
                }

                item {
                    Text(
                        text = "5 Days Forecast",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    ForecastSection(forecast = uiState.forecast)
                }

            }
        }
    }
}

// ======================== Today Weather Section ========================

@Composable
fun TodayWeatherSection(weather: ir.example1.weather.domain.model.Weather) {
    val date = Date(weather.timestamp)
    val configuration = LocalConfiguration.current
    val formatter = SimpleDateFormat(
        "yyyy/MM/dd  HH:mm",
        configuration.locale
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Last update: ${formatter.format(date)}",
            color = Color(0xFFC5FFFFFF),
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        AsyncImage(
            model = WeatherIconMapper.getIconResource(weather.icon),
            contentDescription = "Weather icon",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = weather.condition,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${weather.temperature.toInt()}°",
            color = Color.White,
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "L: ${weather.minTemp.toInt()}°",
                color = Color.White,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "H: ${weather.maxTemp.toInt()}°",
                color = Color.White,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DescriptionCard(weather = weather, modifier = Modifier.weight(1f))
            WindCard(weather = weather, modifier = Modifier.weight(1f))
            HumidityCard(weather = weather, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun DescriptionCard(
    weather: ir.example1.weather.domain.model.Weather,
    modifier: Modifier = Modifier
) {
    val (iconRes, description, statusValue) = when (weather.condition) {
        "Rain", "Drizzle", "Thunderstorm" -> {
            Triple(R.drawable.img_rainy, "Last 1h rain", "${weather.rain} mm/h")
        }
        "Clouds" -> {
            Triple(R.drawable.img_cloudy, "Cloudiness", "${weather.clouds} %")
        }
        "Snow" -> {
            Triple(R.drawable.img_snowy, "Status", weather.description)
        }
        "Mist", "Fog", "Haze" -> {
            Triple(R.drawable.img_visibility, "Visibility", "${weather.visibility ?: 0} m")
        }
        else -> {
            Triple(R.drawable.img_pressure, "Pressure", "${weather.pressure} hPa")
        }
    }

    Card(
        modifier = modifier
            .height(140.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1E48)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = iconRes,
                contentDescription = description,
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = statusValue,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = description,
                color = Color(0xFFA9A5A5),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun WindCard(
    weather: ir.example1.weather.domain.model.Weather,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1E48)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = R.drawable.img_windy,
                contentDescription = "Wind",
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "${weather.windSpeed.toInt()} Km/h",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Wind",
                color = Color(0xFFA9A5A5),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun HumidityCard(
    weather: ir.example1.weather.domain.model.Weather,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1E48)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = R.drawable.img_humidity,
                contentDescription = "Humidity",
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "${weather.humidity}%",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Humidity",
                color = Color(0xFFA9A5A5),
                fontSize = 14.sp
            )
        }
    }
}

// ======================== Forecast Section ========================

@Composable
fun ForecastSection(forecast: List<ir.example1.weather.domain.model.Forecast>) {
    if (forecast.isEmpty()) {
        Text(
            text = "No forecast data available",
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(forecast) { item ->
            ForecastItem(forecast = item)
        }
    }
}

@Composable
fun ForecastItem(forecast: ir.example1.weather.domain.model.Forecast) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = forecast.dateTime
    }

    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val dayName = when (dayOfWeek) {
        Calendar.SUNDAY -> "Sun"
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        else -> "-"
    }

    val hour = calendar.get(Calendar.HOUR)
    val amPm = if (calendar.get(Calendar.HOUR_OF_DAY) < 12) "am" else "pm"

    Column(
        modifier = Modifier
            .width(70.dp)
            .height(140.dp)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "$hour$amPm",
            color = Color.White,
            fontSize = 14.sp
        )

        AsyncImage(
            model = WeatherIconMapper.getIconResource(forecast.icon),
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .padding(vertical = 8.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "${forecast.temperature.toInt()}°",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}