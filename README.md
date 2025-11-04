# Laci's Smexy App - Android Habit Tracking Application

## 📱 Project Overview

This is a modern Android application built with **Kotlin** and **Jetpack Compose** for tracking habits and managing schedules. The app follows the **MVVM (Model-View-ViewModel)** architecture pattern and uses **Clean Architecture** principles to separate concerns between UI, business logic, and data layers.

## 🏗️ Architecture

### Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (declarative UI)
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit + OkHttp
- **Dependency Injection**: Manual (using factory functions)
- **Async Operations**: Kotlin Coroutines + Flow
- **State Management**: StateFlow (reactive state)
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Authentication**: JWT tokens + Google Sign-In

### Project Structure

```
app/
├── data/
│   ├── api/              # Retrofit API interfaces
│   │   ├── AuthApiService.kt
│   │   └── ScheduleApiService.kt
│   ├── models/           # Data Transfer Objects (DTOs)
│   ├── network/          # Network configuration
│   │   └── NetworkModule.kt
│   └── repository/       # Repository layer (data abstraction)
│       ├── AuthRepository.kt
│       └── ScheduleRepository.kt
├── ui/
│   ├── navigation/       # Navigation graph
│   │   └── AppNavigation.kt
│   ├── screens/          # UI screens (Composables)
│   │   ├── splash/
│   │   ├── login/
│   │   ├── register/
│   │   ├── home/
│   │   ├── addhabit/
│   │   ├── createschedule/
│   │   ├── scheduledetails/
│   │   ├── editschedule/
│   │   ├── profile/
│   │   └── editprofile/
│   └── theme/            # Material Design theme
├── util/                 # Utility classes
│   └── Logging.kt
└── MainActivity.kt       # Single Activity
```

---

## 🔄 Application Flow - Deep Dive

### 1️⃣ Application Startup

#### MainActivity (Single Activity Architecture)

```kotlin
class MainActivity : ComponentActivity()
```

**Lifecycle Events:**

1. **onCreate()** - Called when the activity is first created
   - Calls `enableEdgeToEdge()` for modern UI
   - Sets up Compose content using `setContent {}`
   - Initializes the app theme (`CSLKotlinProjektTheme`)
   - Launches `AppNavigation()` composable
   - Logs: "MainActivity onCreate"

2. **onStart()** - Activity becomes visible
3. **onResume()** - Activity gains focus and becomes interactive
4. **onPause()** - User navigates away
5. **onStop()** - Activity no longer visible
6. **onDestroy()** - Activity is destroyed

**Important**: This app uses a **Single Activity Architecture**. All screens are Composable functions, not separate Activities. The MainActivity only goes through its lifecycle once (unless recreated due to configuration changes).

---

### 2️⃣ Navigation Setup

#### AppNavigation.kt

The navigation is handled by Jetpack Navigation Compose using a `NavHost`:

```kotlin
@Composable
fun AppNavigation(navController: NavHostController, startDestination: String = "splash")
```

**Navigation Graph:**
- `splash` → Initial screen
- `login` → Login screen
- `register` → Registration screen
- `home` → Main home screen
- `add_schedule` → Create schedule
- `add_habit` → Add new habit
- `schedule_details/{id}` → Schedule details (with ID parameter)
- `edit_schedule/{id}` → Edit schedule
- `profile` → User profile
- `edit_profile` → Edit profile

**Navigation Actions:**
- `navController.navigate("destination")` - Navigate forward
- `navController.popBackStack()` - Go back
- `popUpTo("destination") { inclusive = true }` - Clear backstack (prevents back navigation)

---

### 3️⃣ Splash Screen Flow

#### SplashScreen.kt (Composable)

**Purpose**: Initial loading screen that determines if user should go to Home or Login.

**Composable Lifecycle:**
1. **Composition** - Screen is first drawn
   - `LogComposableLifecycle("SplashScreen")` tracks composition/recomposition
   - `remember {}` creates animation state
   - `LaunchedEffect(Unit)` runs side effects once

2. **Animation Setup:**
   ```kotlin
   val alphaAnimatable = remember { Animatable(0f) }
   val scaleAnimatable = remember { Animatable(0.5f) }
   ```
   - Animates from transparent (0f) to opaque (1f)
   - Scales from 0.5x to 1x size

3. **State Observation:**
   ```kotlin
   val uiState by viewModel.uiState.collectAsState()
   ```
   - Collects StateFlow from ViewModel
   - Automatically recomposes when state changes

#### SplashViewModel.kt

**Initialization:**
```kotlin
init { AppLog.i("SplashViewModel", "init") }
```

**State Management:**
```kotlin
data class SplashUiState(
    val isLoading: Boolean = true,
    val shouldNavigateToHome: Boolean = false,
    val shouldNavigateToLogin: Boolean = false,
    val error: String? = null
)
```

**Auto-Login Flow:**

1. **checkAutoLogin()** is called from LaunchedEffect
2. Uses `viewModelScope.launch {}` - coroutine scoped to ViewModel lifecycle
3. **Delay 3 seconds** minimum for splash animation
4. Creates `AuthRepository` instance
5. Checks `authRepository.isLoggedIn()`:
   - Reads SharedPreferences for `refresh_token`
   - If token exists → user is logged in
   - If no token → navigate to Login

6. **Token Refresh Attempt:**
   ```kotlin
   val refreshResult = authRepository.refreshAccessToken()
   ```
   - Calls `POST /auth/local/refresh` endpoint
   - Sends refresh token in Authorization header (via interceptor)
   - If successful → new access & refresh tokens saved
   - If failed → clear tokens, go to Login

7. **State Update:**
   ```kotlin
   _uiState.value = _uiState.value.copy(
       isLoading = false,
       shouldNavigateToHome = true
   )
   ```

8. **Navigation Trigger:**
   ```kotlin
   LaunchedEffect(uiState.shouldNavigateToHome, uiState.shouldNavigateToLogin) {
       when {
           uiState.shouldNavigateToHome -> onNavigateToHome()
           uiState.shouldNavigateToLogin -> onNavigateToLogin()
       }
   }
   ```

**ViewModel Lifecycle:**
- `onCleared()` - Called when ViewModel is destroyed (logs cleanup)

---

### 4️⃣ Login Screen Flow

#### LoginScreen.kt (Composable)

**UI Components:**
- Email TextField (with email validation)
- Password TextField (with visibility toggle)
- Login Button
- Register Button
- Google Sign-In Button

**State Observation:**
```kotlin
val uiState by viewModel.uiState.collectAsState()
```

**User Input Handling:**
```kotlin
OutlinedTextField(
    value = uiState.email,
    onValueChange = viewModel::updateEmail,  // Function reference
    // ...
)
```

**Login Button Click:**
```kotlin
Button(onClick = { viewModel.login(context) { onNavigateToHome() } })
```

**Google Sign-In Setup:**
1. Creates `GoogleSignInOptions` with web client ID from `strings.xml`
2. Registers activity result launcher:
   ```kotlin
   val googleLauncher = rememberLauncherForActivityResult(
       ActivityResultContracts.StartActivityForResult()
   ) { result -> /* handle result */ }
   ```
3. Extracts ID token from Google account
4. Calls `viewModel.loginWithGoogle(context, idToken, onNavigateToHome)`

**Success Navigation:**
```kotlin
LaunchedEffect(uiState.isLoginSuccessful) {
    if (uiState.isLoginSuccessful) {
        onNavigateToHome()
        viewModel.resetLoginSuccess()
    }
}
```

#### LoginViewModel.kt

**State:**
```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoginSuccessful: Boolean = false
)
```

**State Updates are Immutable:**
```kotlin
fun updateEmail(email: String) {
    _uiState.value = _uiState.value.copy(
        email = email,
        emailError = null,  // Clear previous errors
        generalError = null
    )
}
```

**Login Process:**

1. **Validation:**
   ```kotlin
   private fun validateEmail(email: String): String? {
       return when {
           email.isBlank() -> "Email is required"
           !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email"
           else -> null
       }
   }
   ```

2. **Network Call:**
   ```kotlin
   viewModelScope.launch {
       _uiState.value = currentState.copy(isLoading = true)
       
       val authRepository = createAuthRepository(context)
       val result = authRepository.signIn(email, password)
       
       if (result.isSuccess) {
           _uiState.value = _uiState.value.copy(
               isLoading = false,
               isLoginSuccessful = true
           )
           onSuccess()
       } else {
           _uiState.value = _uiState.value.copy(
               isLoading = false,
               generalError = result.exceptionOrNull()?.message
           )
       }
   }
   ```

**Understanding `viewModelScope.launch`:**

- `viewModelScope` is a CoroutineScope tied to the ViewModel's lifecycle
- `launch` starts a new coroutine (async operation)
- Automatically cancelled when ViewModel is cleared
- Uses `Dispatchers.Main` by default (UI thread)
- Suspends without blocking the main thread

---

### 5️⃣ Repository Layer

#### AuthRepository.kt

**Purpose**: Abstracts data sources and business logic from ViewModel.

**Dependencies:**
- `AuthApiService` - Retrofit interface
- `SharedPreferences` - Local storage for tokens

**Token Storage:**
```kotlin
companion object {
    const val PREF_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
}
```

**Sign In Method:**
```kotlin
suspend fun signIn(email: String, password: String): Result<AuthResponseDto> = 
    withContext(Dispatchers.IO) {
        try {
            val request = SignInDto(email, password)
            val response = authApiService.signIn(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveTokens(authResponse)  // Save to SharedPreferences
                Result.success(authResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Login failed: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
```

**Understanding `withContext(Dispatchers.IO)`:**
- Switches coroutine to IO thread pool (optimized for network/disk operations)
- `suspend` function can only be called from coroutine or another suspend function
- Returns Result<T> (sealed class for success/failure)

withContext is a coroutine builder that allows you to switch the execution context (thread) of your coroutine temporarily. It's like saying "run this block of code on a different thread, then come back to where I was."
What is Dispatchers.IO?
Dispatchers.IO is a thread pool specifically optimized for:
Network operations (API calls, downloading)
Database operations (reading/writing to database)
File I/O (reading/writing files)
Any blocking I/O operations

**Token Saving:**
```kotlin
fun saveTokens(response: AuthResponseDto) {
    sharedPreferences.edit().apply {
        putString(KEY_ACCESS_TOKEN, response.tokens.accessToken)
        putString(KEY_REFRESH_TOKEN, response.tokens.refreshToken)
        putInt(KEY_USER_ID, response.user.id)
        putString(KEY_USERNAME, response.user.username)
        putString(KEY_EMAIL, response.user.email)
        apply()  // Async save
    }
}
```

---

### 6️⃣ Network Layer

#### NetworkModule.kt

**Singleton Object** - Provides configured Retrofit instances.

**Base URL:**
```kotlin
const val BASE_URL = "http://192.168.1.56:8080/"
```

**OkHttp Client Configuration:**

1. **Auth Interceptor** - Automatically adds Bearer token:
   ```kotlin
   private fun createAuthInterceptor(context: Context): Interceptor {
       return Interceptor { chain ->
           val request = chain.request()
           
           // Skip if Authorization header already present
           if (request.header("Authorization") != null) {
               return@Interceptor chain.proceed(request)
           }
           
           val sharedPreferences = context.getSharedPreferences("auth_prefs", MODE_PRIVATE)
           
           // Use refresh token for refresh endpoint, access token for others
           val isRefreshEndpoint = request.url.encodedPath.contains("/auth/local/refresh")
           val token = if (isRefreshEndpoint) {
               sharedPreferences.getString("refresh_token", null)
           } else {
               sharedPreferences.getString("access_token", null)
           }
           
           val newRequest = if (!token.isNullOrEmpty()) {
               request.newBuilder()
                   .addHeader("Authorization", "Bearer $token")
                   .build()
           } else request
           
           chain.proceed(newRequest)
       }
   }
   ```

2. **Token Authenticator** - Handles 401 responses automatically:
   ```kotlin
   private class TokenAuthenticator(private val context: Context) : okhttp3.Authenticator {
       override fun authenticate(route: Route?, response: Response): Request? {
           // Avoid infinite loop if refresh also fails
           if (response.priorResponse?.code == 401) {
               return null
           }
           
           val refreshToken = prefs.getString("refresh_token", null) ?: return null
           
           // Synchronously refresh tokens
           val newTokens = refreshTokens(refreshToken) ?: return null
           
           // Update stored tokens
           prefs.edit()
               .putString("access_token", newTokens.accessToken)
               .putString("refresh_token", newTokens.refreshToken)
               .apply()
           
           // Retry original request with new token
           return response.request.newBuilder()
               .header("Authorization", "Bearer ${newTokens.accessToken}")
               .build()
       }
   }
   ```

**Retrofit Instance:**
```kotlin
private fun getOrCreateRetrofit(context: Context): Retrofit {
    val existing = retrofit
    if (existing != null) return existing
    
    synchronized(this) {
        val again = retrofit
        if (again != null) return again
        
        val r = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOrCreateOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())  // JSON parsing
            .build()
        retrofit = r
        return r
    }
}
```

**Thread-Safe Singleton Pattern:**
- Uses `@Volatile` for thread visibility
- Double-checked locking with `synchronized`
- Ensures only one instance created

What it does: Ensures that all threads see the most recent value of retrofit.
Without @Volatile:
Thread A creates retrofit and stores it
Thread B might read cached old value (null) from its CPU cache
Thread B creates ANOTHER instance (duplicate!)
With @Volatile:
Writes are immediately visible to all threads
Reads always get the latest value from main memory
Prevents CPU cache inconsistencies

synchronized(this) {
// Only ONE thread can execute this block at a time
}
What it does:
Acts like a lock/mutex
First thread to arrive enters, others wait outside
When first thread exits, next thread enters

Why?
Performance:
First check avoids expensive locking after initialization
Only lock during initial creation (rare case)
Correctness:
@Volatile ensures visibility across threads
synchronized ensures only one thread creates instance
Double-check ensures no duplicates
---

### 7️⃣ API Service Layer

#### AuthApiService.kt

**Retrofit Interface** - Defines HTTP endpoints:

```kotlin
interface AuthApiService {
    @POST("auth/local/signin")
    suspend fun signIn(@Body request: SignInDto): Response<AuthResponseDto>
    
    @POST("auth/google")
    suspend fun googleSignIn(@Body request: GoogleSignInDto): Response<AuthResponseDto>
    
    @POST("auth/local/refresh")
    suspend fun refreshToken(): Response<TokensDto>
    
    @POST("auth/local/logout")
    suspend fun logout(): Response<Unit>
    
    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponseDto>
    
    @PATCH("profile")
    suspend fun updateProfile(@Body dto: UpdateProfileDto): Response<ProfileResponseDto>
    
    @Multipart
    @POST("profile/upload-profile-image")
    suspend fun uploadProfileImage(
        @Part profileImage: MultipartBody.Part
    ): Response<ProfileResponseDto>
}
```

**Annotations:**
- `@POST`, `@GET`, `@PATCH`, `@DELETE` - HTTP methods
- `@Body` - Request body (JSON)
- `@Path("id")` - URL path parameter
- `@Query("date")` - URL query parameter
- `@Multipart` + `@Part` - File upload
- `suspend` - Coroutine function

**Response Flow:**
1. ViewModel calls Repository method
2. Repository calls API Service suspend function
3. Retrofit makes HTTP request (on IO thread via withContext)
4. OkHttp Interceptor adds Authorization header automatically
5. Response received and parsed by Gson to DTO
6. Repository returns Result<DTO> to ViewModel
7. ViewModel updates StateFlow
8. Composable observes StateFlow and recomposes UI

---

### 8️⃣ Home Screen Flow

#### HomeScreen.kt

**Screen Responsibilities:**
- Display user's schedules for selected date
- Show user profile image
- Allow schedule creation
- Navigate to schedule details
- Toggle schedule completion
- Add progress to schedules
- Logout functionality

**State Observation:**
```kotlin
val uiState by viewModel.uiState.collectAsState()
```

**Data Loading:**
```kotlin
LaunchedEffect(Unit) {
    viewModel.loadUserInfo(context)
    viewModel.loadSchedule(context, selectedDate)
}
```

**Lifecycle-Aware Reloading:**
```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
DisposableEffect(lifecycleOwner, selectedDate) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            viewModel.loadSchedule(context, selectedDate)
            viewModel.loadUserInfo(context)
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```
- Reloads data when returning to screen
- Cleans up observer on disposal

**Profile Image Display:**
```kotlin
val profileImageModel by remember(uiState.profileImageUrl, uiState.profileImageBase64) {
    mutableStateOf(
        when {
            !uiState.profileImageUrl.isNullOrBlank() -> uiState.profileImageUrl
            !uiState.profileImageBase64.isNullOrBlank() -> {
                val cleaned = uiState.profileImageBase64.substringAfter("base64,")
                Base64.decode(cleaned, Base64.DEFAULT)
            }
            else -> null
        }
    )
}
```
- Supports both URL and base64 encoded images
- Uses Coil library for async image loading

**Schedule List:**
```kotlin
LazyColumn {
    items(uiState.schedule) { schedule ->
        ScheduleCard(
            schedule = schedule,
            onScheduleClick = { onNavigateToScheduleDetails(schedule.id) },
            onToggleCompletion = { viewModel.toggleScheduleCompletion(context, schedule.id) },
            onAddProgress = { viewModel.openProgressDialog(schedule.id) }
        )
    }
}
```
- `LazyColumn` - RecyclerView equivalent in Compose (lazy rendering)
- `items()` - Iterates over list
- Each item is a composable function

#### HomeViewModel.kt

**State:**
```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val username: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val profileImageBase64: String? = null,
    val schedule: List<ScheduleResponseDto> = emptyList(),
    val scheduleError: String? = null,
    val currentDate: Date = Date(),
    val showProgressDialog: Boolean = false,
    val progressScheduleId: Int? = null,
    val togglingScheduleIds: Set<Int> = emptySet()
    // ... more fields
)
```

**Load Schedule:**
```kotlin
fun loadSchedule(context: Context, date: Date? = Date()) {
    _uiState.value = _uiState.value.copy(isLoading = true, scheduleError = null)
    
    viewModelScope.launch {
        val scheduleRepository = ScheduleRepository(
            NetworkModule.createScheduleApiService(context)
        )
        val result = scheduleRepository.getSchedulesByDay(date)
        
        if (result.isSuccess) {
            val list = result.getOrNull().orEmpty().sortedBy { it.startTime }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                schedule = list
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                scheduleError = result.exceptionOrNull()?.message
            )
        }
    }
}
```

**Toggle Schedule Completion:**
```kotlin
fun toggleScheduleCompletion(context: Context, scheduleId: Int) {
    val schedule = _uiState.value.schedule.find { it.id == scheduleId } ?: return
    val currentCompleted = schedule.progress?.lastOrNull()?.isCompleted ?: false
    val newCompleted = !currentCompleted
    
    // Optimistic UI update
    _uiState.value = _uiState.value.copy(
        togglingScheduleIds = _uiState.value.togglingScheduleIds + scheduleId,
        togglingDesired = _uiState.value.togglingDesired + (scheduleId to newCompleted)
    )
    
    viewModelScope.launch {
        val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
        val dto = CreateProgressDto(
            scheduleId = scheduleId,
            isCompleted = newCompleted,
            date = schedule.date
        )
        val result = repo.createProgress(dto)
        
        if (result.isSuccess) {
            // Reload to get accurate data
            loadSchedule(context, _uiState.value.currentDate)
        } else {
            // Revert optimistic update on failure
            _uiState.value = _uiState.value.copy(
                togglingScheduleIds = _uiState.value.togglingScheduleIds - scheduleId,
                togglingDesired = _uiState.value.togglingDesired - scheduleId
            )
        }
    }
}
```

---

## 🔑 Key Concepts Explained

### Compose State Management

**remember:**
```kotlin
var selectedDate by remember { mutableStateOf(Date()) }
```
- Stores value across recompositions
- Lost on configuration change (unless using rememberSaveable)
- Scoped to composable lifecycle

**StateFlow (ViewModel):**
```kotlin
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
```
- Hot stream that always has a value
- Survives configuration changes (tied to ViewModel)
- Collected in composables with `collectAsState()`

**Why Immutable Updates?**
```kotlin
_uiState.value = _uiState.value.copy(isLoading = false)
```
- Jetpack Compose detects changes by reference equality
- Creating new object triggers recomposition
- Immutability prevents bugs from shared mutable state

### Coroutines & Threading

**viewModelScope.launch:**
- Structured concurrency (child coroutines cancelled with parent)
- Runs on Main dispatcher by default
- Perfect for UI updates

**withContext(Dispatchers.IO):**
- Switches to background thread for network/database
- Returns to original dispatcher after block completes
- Non-blocking (suspends coroutine, not thread)

**Why suspend functions?**
- Marks function as potentially long-running
- Can only be called from coroutine or another suspend function
- Compiler enforces proper async handling

### Why No RecyclerView?

**LazyColumn (Compose equivalent):**
- Automatically lazy loads items (virtualization)
- No ViewHolder pattern needed
- Simpler API than RecyclerView
- Built-in animations
- Example:
  ```kotlin
  LazyColumn {
      items(schedules) { schedule ->
          ScheduleCard(schedule)  // Composable for each item
      }
  }
  ```

### return@launch vs return

```kotlin
viewModelScope.launch {
    if (error) return@launch  // Returns from launch block
    // More code...
}

viewModelScope.launch {
   if (someCondition) {
      return@launch // Exits this coroutine block
   }
   // Other code in the coroutine still gets executed
}
```

- `return@launch` - Returns from labeled lambda (the launch block)
- `return` alone - Would try to return from enclosing function
- Same applies to `return@async`, `return@forEach`, etc.

### Single Activity Architecture

**Why?**
- All screens are Composables, not Activities
- Single Activity lifecycle
- Faster navigation (no Activity overhead)
- Easier state sharing
- Modern Android best practice

**Implication for Lifecycle:**
- MainActivity lifecycle happens once
- Individual screens don't have Activity lifecycles
- Use `DisposableEffect` + `LifecycleEventObserver` for per-screen lifecycle
- ViewModels survive configuration changes automatically

### Logging Implementation 

All logging is done through `AppLog` utility:
```kotlin
AppLog.i("MainActivity", "onCreate")
AppLog.i("LoginViewModel", "init")
```

**ViewModel Logging:**
- `init {}` block logs initialization
- `onCleared()` logs when ViewModel is destroyed

**Composable Logging:**
```kotlin
LogComposableLifecycle("LoginScreen")
```
- Logs composition, recomposition, and disposal
- Helps understand when UI rebuilds

**Single Activity = Limited Lifecycle Logs:**
- You'll see MainActivity lifecycle once
- Screen changes don't trigger Activity lifecycle
- ViewModel init/onCleared shows screen transitions
- Composable logs show UI recompositions

---

## 📊 Complete Login Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│ MainActivity.onCreate()                                         │
│  ├─ enableEdgeToEdge()                                         │
│  ├─ setContent { CSLKotlinProjektTheme { AppNavigation() } }  │
│  └─ Log: "MainActivity onCreate"                               │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ AppNavigation (NavHost)                                         │
│  └─ startDestination = "splash"                                │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ SplashScreen (Composable)                                       │
│  ├─ Composition: LogComposableLifecycle("SplashScreen")        │
│  ├─ remember { Animatable } - Animation state                  │
│  ├─ LaunchedEffect(Unit) {                                     │
│  │    ├─ viewModel.checkAutoLogin(context)                    │
│  │    └─ Start animations                                      │
│  │ }                                                            │
│  ├─ val uiState by viewModel.uiState.collectAsState()         │
│  └─ LaunchedEffect(uiState.shouldNavigateToLogin) {           │
│       └─ if (true) onNavigateToLogin()                         │
│    }                                                            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ SplashViewModel                                                 │
│  ├─ init { AppLog.i("SplashViewModel", "init") }              │
│  ├─ StateFlow<SplashUiState> (initial: isLoading=true)        │
│  └─ checkAutoLogin(context) {                                  │
│       viewModelScope.launch {                                  │
│         ├─ delay(3000) // Splash animation                     │
│         ├─ val repo = createAuthRepository(context)           │
│         ├─ if (!repo.isLoggedIn()) {                          │
│         │    └─ Update state: shouldNavigateToLogin = true    │
│         │ } else {                                             │
│         │    ├─ val result = repo.refreshAccessToken()       │
│         │    └─ if (success) shouldNavigateToHome = true     │
│         │ }                                                    │
│       }                                                         │
│     }                                                           │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ AuthRepository.refreshAccessToken()                             │
│  └─ withContext(Dispatchers.IO) {                              │
│       ├─ val response = authApiService.refreshToken()         │
│       └─ if (success) save new tokens to SharedPreferences    │
│    }                                                            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ NetworkModule → OkHttp → AuthInterceptor                        │
│  ├─ Read refresh_token from SharedPreferences                  │
│  ├─ Add header: "Authorization: Bearer <refresh_token>"       │
│  └─ Execute HTTP POST /auth/local/refresh                     │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ Backend API Response                                            │
│  └─ 200 OK: { accessToken, refreshToken }                     │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ Repository saves tokens → ViewModel updates state               │
│  └─ SplashUiState.copy(shouldNavigateToLogin = true)          │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ NavController.navigate("login")                                 │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ LoginScreen (Composable)                                        │
│  ├─ Composition: LogComposableLifecycle("LoginScreen")         │
│  ├─ val viewModel: LoginViewModel = viewModel()               │
│  ├─ val uiState by viewModel.uiState.collectAsState()        │
│  ├─ UI: OutlinedTextField(                                     │
│  │      value = uiState.email,                                 │
│  │      onValueChange = viewModel::updateEmail                │
│  │    )                                                         │
│  ├─ Button(onClick = {                                         │
│  │      viewModel.login(context) { onNavigateToHome() }       │
│  │    })                                                        │
│  └─ LaunchedEffect(uiState.isLoginSuccessful) {               │
│       if (true) onNavigateToHome()                             │
│    }                                                            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼ (User enters email/password and clicks Login)
┌─────────────────────────────────────────────────────────────────┐
│ LoginViewModel.login(context, onSuccess)                        │
│  ├─ Validate email & password                                  │
│  ├─ Update state: isLoading = true                            │
│  └─ viewModelScope.launch {                                    │
│       ├─ val repo = createAuthRepository(context)             │
│       ├─ val result = repo.signIn(email, password)           │
│       └─ if (success) {                                        │
│            ├─ Update state: isLoginSuccessful = true          │
│            └─ onSuccess() // Callback                          │
│         }                                                       │
│    }                                                            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ AuthRepository.signIn(email, password)                          │
│  └─ withContext(Dispatchers.IO) {                              │
│       ├─ val dto = SignInDto(email, password)                 │
│       ├─ val response = authApiService.signIn(dto)            │
│       ├─ if (response.isSuccessful) {                         │
│       │    ├─ saveTokens(response.body())                     │
│       │    └─ return Result.success(response.body())          │
│       │ }                                                       │
│       └─ else return Result.failure(Exception(...))           │
│    }                                                            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ Retrofit + OkHttp                                               │
│  ├─ POST /auth/local/signin                                    │
│  ├─ Body: { "email": "...", "password": "..." }              │
│  └─ GsonConverterFactory parses JSON response                 │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ Backend API Response                                            │
│  └─ 200 OK: {                                                  │
│       tokens: { accessToken, refreshToken },                   │
│       user: { id, email, username }                            │
│     }                                                           │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ AuthRepository.saveTokens(authResponse)                         │
│  └─ sharedPreferences.edit().apply {                           │
│       putString("access_token", ...)                           │
│       putString("refresh_token", ...)                          │
│       putInt("user_id", ...)                                   │
│       apply()                                                   │
│    }                                                            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ Return to ViewModel                                             │
│  ├─ Result.success → Update StateFlow                          │
│  └─ _uiState.value = _uiState.value.copy(                     │
│       isLoading = false,                                        │
│       isLoginSuccessful = true                                 │
│    )                                                            │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ LoginScreen observes state change                               │
│  └─ LaunchedEffect triggers → onNavigateToHome()              │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ NavController.navigate("home") {                                │
│   popUpTo("login") { inclusive = true }  // Clear backstack   │
│ }                                                               │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ HomeScreen (Composable)                                         │
│  ├─ Composition: LogComposableLifecycle("HomeScreen")          │
│  ├─ val viewModel: HomeViewModel = viewModel()                │
│  ├─ LaunchedEffect(Unit) {                                     │
│  │    ├─ viewModel.loadUserInfo(context)                      │
│  │    └─ viewModel.loadSchedule(context, Date())              │
│  │ }                                                            │
│  └─ Display user's schedules, profile image, etc.             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Security Features

### Token Management
- **Access Token**: Short-lived, used for API requests
- **Refresh Token**: Long-lived, used to obtain new access tokens
- **Automatic Refresh**: Handled by TokenAuthenticator on 401 responses
- **Secure Storage**: SharedPreferences (encrypted on Android 6+)

### Automatic Authentication
- **Interceptor**: Adds Bearer token to all requests automatically
- **No Manual Headers**: ViewModels don't handle authentication
- **Centralized**: All token logic in NetworkModule

---

## 📱 Other Key Screens

### Schedule Details Screen
- Displays habit information
- Shows progress history
- Visualizes completion with progress bar
- Allows editing notes
- Navigate to edit screen

### Edit Schedule Screen
- Modify start/end times
- Change duration
- Update status (Planned/Completed/Skipped)
- Add/remove participants
- Edit notes

### Profile Screen
- Display user information
- List user's habits
- Calculate completion percentages per habit
- Upload profile image
- Edit profile (username)
- Logout with confirmation

### Create Schedule Screen
- Select habit
- Choose date and time
- Set duration
- Add participants
- Create custom or recurring schedules

---

## 🎯 Best Practices Used

1. **MVVM Architecture**: Clear separation between UI, ViewModel, and data layers
2. **Single Activity**: Modern Android navigation pattern
3. **Compose**: Declarative UI with automatic updates
4. **StateFlow**: Reactive state management
5. **Coroutines**: Structured concurrency for async operations
6. **Repository Pattern**: Abstract data sources
7. **Dependency Injection**: Manual DI with factory functions
8. **Error Handling**: Result<T> for explicit success/failure
9. **Immutable State**: Copy-on-write state updates
10. **Lifecycle Awareness**: Proper cleanup and reloading

---

## 🚀 Running the Project

### Prerequisites
- Android Studio (latest version)
- Android SDK (API 24+)
- JDK 11
- Backend server running at configured IP

### Setup
1. Clone repository
2. Update `BASE_URL` in `NetworkModule.kt` with your backend IP
3. Add `google_web_client_id` in `strings.xml` for Google Sign-In
4. Sync Gradle dependencies
5. Run on emulator or physical device

### Backend Requirements
- REST API endpoints as specified
- JWT token authentication
- Google OAuth support

---

## 📝 Key Files Reference

| File | Purpose |
|------|---------|
| MainActivity.kt | Single activity host |
| AppNavigation.kt | Navigation graph |
| NetworkModule.kt | Network configuration & interceptors |
| AuthRepository.kt | Authentication data layer |
| ScheduleRepository.kt | Schedule data layer |
| LoginViewModel.kt | Login business logic |
| HomeViewModel.kt | Home screen logic |
| LoginScreen.kt | Login UI (Composable) |
| HomeScreen.kt | Home UI (Composable) |

---

## 🎓 Key Learnings for Presentation

### What happens when user logs in?
1. User enters credentials → UI updates state
2. ViewModel validates input
3. Repository makes API call on IO thread
4. Network interceptor adds auth headers
5. Backend validates and returns tokens
6. Repository saves tokens to SharedPreferences
7. ViewModel updates StateFlow (success state)
8. Composable observes state change and recomposes
9. Navigation triggered to Home screen
10. Home screen loads data automatically

### How does automatic token refresh work?
- 401 response triggers TokenAuthenticator
- Synchronously calls refresh endpoint with refresh token
- Updates stored tokens
- Retries original request with new access token
- Transparent to ViewModel/UI

### Why single Activity?
- Faster navigation (no Activity overhead)
- Easier state management across screens
- Modern Android best practice with Compose
- Simplified lifecycle management

### How does Compose know when to update UI?
- StateFlow emits new state
- `collectAsState()` observes emissions
- Compose compares by reference (immutable updates)
- Recomposes only affected composables

---

**Created by**: Laci  
**Architecture**: MVVM + Clean Architecture  
**UI Framework**: Jetpack Compose  
**Language**: Kotlin  
**Backend**: RESTful API with JWT authentication

