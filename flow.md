**Response Flow:**
1. ViewModel calls Repository method
2. Repository calls API Service suspend function
3. Retrofit makes HTTP request (on IO thread via withContext)
4. OkHttp Interceptor adds Authorization header automatically
5. Response received and parsed by Gson to DTO
6. Repository returns Result<DTO> to ViewModel
7. ViewModel updates StateFlow
8. Composable observes StateFlow and recomposes UI

- `LazyColumn` - RecyclerView equivalent in Compose (lazy rendering)
- `items()` - Iterates over list
- Each item is a composable function

Retrofit delegates actual network I/O to an OkHttpClient you configure. 
OkHttp handles transport; 
Retrofit maps your API interfaces and data models.

- logcat filter: package:mine level:info AL/ n  

### **CRITICAL UNDERSTANDING:**
**These lifecycle events (ON_CREATE, ON_START, etc.) are MainActivity's lifecycle, NOT the Composable's!**
"Each Composable screen observes MainActivity's lifecycle, so you see the same lifecycle 
events logged multiple times with different screen tags. 
ViewModels (init/onCleared) are much clearer for tracking which screen the user is actually on."

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
