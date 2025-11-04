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