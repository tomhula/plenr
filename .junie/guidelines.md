# Compose requirements

- Each screen has its own ViewModel.
- Each screen + viewmodel has a separate State data class, which has only val properties.
- The viewmodel has the state as a compose mutable state declared with by keyword and has a private setter. (`var uiState by mutableStateOf(State()) private set`)
- The screen composable then stores the state from viewmodel into a val. (`val uiState = viewModel.state`)
