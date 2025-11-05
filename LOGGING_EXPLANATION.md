# Logging Explanation - Where and Why

## Overview
Logging is strategically placed at key lifecycle points to track the application flow and diagnose issues. All logs use the "AL/" prefix for easy filtering.

---

## 1. MainActivity.kt - Activity Lifecycle Logging

### Location: Activity Lifecycle Methods
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppLog.i("AL/MainActivity", "onCreate")
        // ...
    }
    
    override fun onStart() {
        AppLog.i("AL/MainActivity", "onStart")
    }
    
    override fun onResume() {
        AppLog.i("AL/MainActivity", "onResume")
    }
    
    override fun onDestroy() {
        AppLog.i("AL/MainActivity", "onDestroy")
    }
}
```

### Why Here?
- **onCreate**: The entry point of your app - confirms the app is starting
- **onStart/onResume**: Track when the activity becomes visible and interactive
- **onDestroy**: Confirms proper cleanup when the app closes

### What You'll See:
When you launch the app, you'll see:
```
AL/MainActivity: onCreate
AL/MainActivity: onStart
AL/MainActivity: onResume
```

**Important**: Since this is a **single Activity** app (Jetpack Compose), these lifecycle events happen ONCE when the app starts. All screen navigation happens within Composables, NOT by creating new Activities.

---

## 2. ViewModels - Business Logic Lifecycle

### Location: init block and onCleared()
```kotlin
class LoginViewModel : ViewModel() {
    init { 
        AppLog.i("AL/LoginViewModel", "init") 
    }
    
    override fun onCleared() {
        AppLog.i("AL/LoginViewModel", "onCleared")
        super.onCleared()
    }
}
```

### Applied to ALL ViewModels:
- LoginViewModel
- RegisterViewModel
- SplashViewModel
- HomeViewModel
- ProfileViewModel
- EditProfileViewModel
- ScheduleDetailsViewModel
- EditScheduleViewModel
- CreateScheduleViewModel

### Why Here?
- **init block**: Logs when the ViewModel is created (when you navigate to a screen)
- **onCleared()**: Logs when the ViewModel is destroyed (when you leave the screen or app)

### What You'll See:
When navigating from Login → Home → Profile:
```
AL/LoginViewModel: init
AL/HomeViewModel: init
AL/LoginViewModel: onCleared
AL/ProfileViewModel: init
```

### Why This Matters:
- Shows which screen the user is on
- Reveals navigation flow
- Helps debug memory leaks (if onCleared never happens)
- Confirms ViewModels are properly scoped to navigation

---

## 3. Composables - UI Lifecycle (Optional but Used in Your App)

### Location: LogComposableLifecycle utility function
```kotlin
@Composable
fun LogComposableLifecycle(tag: String) {
    val lifecycleOwner = LocalLifecycleOwner.current  // ← Gets MainActivity
    DisposableEffect(lifecycleOwner) {
        AppLog.i(tag, "Composable ENTER (composition)")
        val observer = LifecycleEventObserver { _, event ->
            AppLog.i(tag, "Lifecycle ${event.name}")  // ← MainActivity events!
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            AppLog.i(tag, "Composable EXIT (dispose)")
        }
    }
}
```

### How It's Used:
```kotlin
@Composable
fun HomeScreen(...) {
    LogComposableLifecycle("AL/HomeScreen")  // Called at top of screen
    // ... rest of composable
}
```

### Why Here?
- **ENTER**: When the Composable is first composed (drawn on screen)
- **Lifecycle events**: Observes MainActivity's lifecycle (ON_CREATE, ON_START, ON_RESUME, etc.)
- **EXIT**: When the Composable is removed from composition (disposed)

### What You'll See:
```
AL/HomeScreen: Composable ENTER (composition)
AL/HomeScreen: Lifecycle ON_CREATE       ← MainActivity's ON_CREATE
AL/HomeScreen: Lifecycle ON_START        ← MainActivity's ON_START
AL/HomeScreen: Lifecycle ON_RESUME       ← MainActivity's ON_RESUME
[Navigate to Profile]
AL/ProfileScreen: Composable ENTER (composition)
AL/ProfileScreen: Lifecycle ON_RESUME    ← MainActivity already created, just ON_RESUME
AL/HomeScreen: Composable EXIT (dispose)
```

### **CRITICAL UNDERSTANDING:**
**These lifecycle events (ON_CREATE, ON_START, etc.) are MainActivity's lifecycle, NOT the Composable's!**

- Each Composable screen **observes** MainActivity's lifecycle
- When you see "AL/SplashScreen: Lifecycle ON_CREATE", it means:
  - SplashScreen is observing MainActivity
  - MainActivity fired its ON_CREATE event
  - The log is tagged with "SplashScreen" to show which screen is observing
  
**Why you see ON_CREATE multiple times:**
- First screen (Splash) logs: "MainActivity ON_CREATE, ON_START, ON_RESUME" 
- When Splash is removed and Login appears, Login starts observing
- If the app goes to background and comes back, you'll see ON_PAUSE/ON_STOP/ON_START/ON_RESUME

**This is confusing and not very useful!** That's why ViewModels are better for tracking screens.

---

## 4. AppLog Utility - Centralized Logging

### Location: util/Logging.kt
```kotlin
object AppLog {
    private const val ROOT = "AL"
    
    fun i(tag: String, message: String) {
        Log.i(tag(tag), message)
    }
    
    fun e(tag: String, message: String, tr: Throwable? = null) {
        Log.e(tag(tag), message, tr)
    }
}
```

### Why This Design?
1. **Centralized**: All logging goes through one place
2. **Consistent Tagging**: Automatically adds "AL/" prefix
3. **Easy to Disable**: Can add a flag to turn off logging in production
4. **Type Safety**: Ensures proper log levels (info, error, etc.)

---

## Why You See Lifecycle Events Multiple Times

### The Single Activity Architecture:
```
MainActivity (ONE ACTIVITY - lives entire app lifetime)
  ↓
  onCreate() → onStart() → onResume() (happens ONCE in MainActivity)
  ↓
  AppNavigation (Jetpack Compose Navigation)
    ├─ SplashScreen (Composable - observes MainActivity lifecycle)
    ├─ LoginScreen (Composable - observes MainActivity lifecycle)
    ├─ HomeScreen (Composable - observes MainActivity lifecycle)
    └─ ProfileScreen (Composable - observes MainActivity lifecycle)
```

### What This Means:
- **MainActivity lifecycle events happen ONCE** (onCreate, onStart, onResume when app starts)
- **BUT** each Composable screen uses `LogComposableLifecycle()` which attaches an observer to MainActivity
- **So you see the same lifecycle events logged multiple times** with different screen tags

### Example Flow:
```
1. App starts:
   AL/MainActivity: onCreate
   AL/MainActivity: onStart
   AL/MainActivity: onResume
   AL/SplashViewModel: init
   AL/SplashScreen: Composable ENTER
   AL/SplashScreen: Lifecycle ON_CREATE    ← SplashScreen observing MainActivity
   AL/SplashScreen: Lifecycle ON_START     ← SplashScreen observing MainActivity
   AL/SplashScreen: Lifecycle ON_RESUME    ← SplashScreen observing MainActivity

2. Navigate to Login:
   AL/LoginViewModel: init
   AL/LoginScreen: Composable ENTER
   AL/LoginScreen: Lifecycle ON_RESUME     ← LoginScreen observing MainActivity (already created)
   AL/SplashViewModel: onCleared
   AL/SplashScreen: Composable EXIT

3. App goes to background (home button pressed):
   AL/MainActivity: onPause
   AL/LoginScreen: Lifecycle ON_PAUSE      ← LoginScreen observing MainActivity
   AL/MainActivity: onStop
   AL/LoginScreen: Lifecycle ON_STOP       ← LoginScreen observing MainActivity

4. App comes back to foreground:
   AL/MainActivity: onStart
   AL/LoginScreen: Lifecycle ON_START      ← LoginScreen observing MainActivity
   AL/MainActivity: onResume
   AL/LoginScreen: Lifecycle ON_RESUME     ← LoginScreen observing MainActivity
```

### Key Understanding:
- **MainActivity logs its own lifecycle** (happens once per event)
- **Each visible Composable also logs MainActivity's lifecycle** (with the screen's tag)
- **This creates duplicate logs** - you'll see the same event multiple times
- **ViewModels are clearer** for tracking which screen is active

---

## Viewing Logs in Logcat

### Filter Options:

1. **Simple Text Search**:
   ```
   AL/
   ```

2. **Package + Level + Tag**:
   ```
   package:mine level:info AL/
   ```

3. **Regex Filter**:
   - Log Tag: `^AL/`

### Expected Output When Navigating:
```
AL/MainActivity: onCreate
AL/MainActivity: onStart  
AL/MainActivity: onResume
AL/SplashViewModel: init
AL/LoginViewModel: init
AL/SplashViewModel: onCleared
[User logs in]
AL/HomeViewModel: init
AL/LoginViewModel: onCleared
[User goes to Profile]
AL/ProfileViewModel: init
[User logs out]
AL/ProfileViewModel: onCleared
AL/LoginViewModel: init
```

---

## Summary - Why These Locations?

| Location | Why Logged | What It Tells You |
|----------|-----------|-------------------|
| **MainActivity lifecycle** | App-level lifecycle events | App start/stop |
| **ViewModel init** | Screen creation | User navigated to screen |
| **ViewModel onCleared** | Screen destruction | User left screen |
| **Composable ENTER/EXIT** | UI composition | Screen rendering |
| **Lifecycle events** | Android lifecycle | App foreground/background |

---

## Key Takeaway for Your Presentation:

**"In a single-Activity Compose app, the Activity lifecycle happens once. ViewModels track screen navigation, and their init/onCleared logs show the user's journey through the app."**

This is the modern Android architecture - one Activity manages the entire UI container, while Composables and ViewModels handle individual screens and their logic.

