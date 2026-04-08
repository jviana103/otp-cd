    package pt.isel

    import android.Manifest
    import android.content.Context
    import android.os.Build
    import android.os.Bundle
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.activity.viewModels
    import androidx.annotation.StringRes
    import androidx.appcompat.app.AppCompatActivity
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Home
    import androidx.compose.material.icons.filled.Settings
    import androidx.compose.material3.Icon
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.saveable.rememberSaveable
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.datastore.preferences.preferencesDataStore
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import androidx.lifecycle.lifecycleScope
    import kotlinx.coroutines.launch
    import pt.isel.datascan.screen.DataScanScreen
    import pt.isel.settings.screen.SettingsScreen
    import pt.isel.datascan.viewmodel.DataScanViewModel
    import pt.isel.settings.viewmodel.SettingsViewModel
    import pt.isel.ui.theme.FirstAppTheme

    val Context.dataStore by preferencesDataStore(name = "settings")

    class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val settingsRepository = (application as OTPCDApplication).settingsRepository

            lifecycleScope.launch {
                settingsRepository.createUserId()
            }

            val dataScanViewModel: DataScanViewModel by viewModels {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DataScanViewModel(settingsRepository) as T
                    }
                }
            }

            val settingsViewModel: SettingsViewModel by viewModels {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(settingsRepository) as T
                    }
                }
            }


            val permissionsToRequest = mutableListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            val permissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { }

            enableEdgeToEdge()
            setContent {
                FirstAppTheme {
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(
                            permissionsToRequest.toTypedArray()
                        )
                    }

                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainAppContainer(dataScanViewModel, settingsViewModel)
                    }
                }
            }
        }
    }

    @Composable
    fun MainAppContainer(
        dataScanViewModel: DataScanViewModel,
        settingsViewModel: SettingsViewModel
    ) {
        var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
        val context = LocalContext.current

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {}

        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(destination.label)) },
                        selected = destination == currentDestination,
                        onClick = { currentDestination = destination }
                    )
                }
            }
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentDestination) {
                        AppDestinations.HOME -> {
                            DataScanScreen(
                                viewModel = dataScanViewModel,
                                onStartService = { rating ->
                                    val missing = dataScanViewModel.checkPermissions(context)

                                    if (missing.isEmpty()) {
                                        dataScanViewModel.confirmInitialRating(context, rating)
                                    } else {
                                        permissionLauncher.launch(missing.toTypedArray())
                                    }
                                },
                                onStopService = {
                                    dataScanViewModel.stopRide(context)
                                }
                            )
                        }
                        AppDestinations.SETTINGS -> SettingsScreen(settingsViewModel)
                    }
                }
            }
        }
    }

    enum class AppDestinations(
        @param:StringRes val label: Int,
        val icon: ImageVector,
    ) {
        HOME(R.string.nav_home, Icons.Default.Home),
        SETTINGS(R.string.nav_settings, Icons.Default.Settings),
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        FirstAppTheme {
            Greeting("Android")
        }
    }
