package com.jaydeep.kaamly.ui.screens.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jaydeep.kaamly.data.model.Equipment
import com.jaydeep.kaamly.data.model.EquipmentProvider
import com.jaydeep.kaamly.data.model.Location
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskCategory
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for creating a new task
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onTaskCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.OTHER) }
    var equipmentList by remember { mutableStateOf(listOf<Equipment>()) }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var isInstantJob by remember { mutableStateOf(false) }
    
    // Validation errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var budgetError by remember { mutableStateOf<String?>(null) }
    
    // Equipment dialog
    var showEquipmentDialog by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    // AI dialog
    var showAIDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val taskCreated by viewModel.taskCreated.collectAsState()
    val aiSuggestion by viewModel.aiSuggestion.collectAsState()
    
    // Handle task creation success
    LaunchedEffect(taskCreated) {
        if (taskCreated != null) {
            onTaskCreated()
            viewModel.clearTaskCreated()
        }
    }
    
    // Handle AI suggestion
    LaunchedEffect(aiSuggestion) {
        aiSuggestion?.let { suggestion ->
            title = suggestion.title
            description = suggestion.description
            selectedCategory = suggestion.category
            equipmentList = suggestion.requiredEquipment.map { 
                Equipment(name = it, providedBy = EquipmentProvider.USER)
            }
            budget = suggestion.suggestedPriceRange.min.toString()
            // Clear the suggestion after autofilling
            viewModel.clearAISuggestion()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // AI Assistant Button
            OutlinedButton(
                onClick = { showAIDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create with AI")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Use Demo Data Button
            OutlinedButton(
                onClick = {
                    // Fill with demo data
                    title = "Fix Leaking Bathroom Tap"
                    description = "My bathroom tap has been leaking for a few days. Need someone to fix it urgently. The tap is a standard mixer tap and water is dripping constantly."
                    selectedCategory = TaskCategory.PLUMBING
                    equipmentList = listOf(
                        Equipment("Wrench", EquipmentProvider.WORKER),
                        Equipment("Plumber's tape", EquipmentProvider.WORKER)
                    )
                    city = "Mumbai"
                    address = "Andheri West, Mumbai"
                    selectedDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
                    selectedTime = "10:00 AM"
                    budget = "500"
                    isInstantJob = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Icon(Icons.Default.Science, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Use Demo Data")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("Task Title *") },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    descriptionError = null
                },
                label = { Text("Description *") },
                isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    TaskCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.replace("_", " ")) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Equipment section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Required Equipment",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { showEquipmentDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add equipment")
                        }
                    }
                    
                    if (equipmentList.isEmpty()) {
                        Text(
                            text = "No equipment added",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        equipmentList.forEach { equipment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(equipment.name)
                                    Text(
                                        text = "Provided by: ${equipment.providedBy.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        equipmentList = equipmentList.filter { it != equipment }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location section
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = city,
                onValueChange = {
                    city = it
                    cityError = null
                },
                label = { Text("City *") },
                isError = cityError != null,
                supportingText = cityError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address (Optional)") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = selectedDate?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Date *") },
                    readOnly = true,
                    isError = dateError != null,
                    supportingText = dateError?.let { { Text(it) } },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        selectedDate = calendar.timeInMillis
                                        dateError = null
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = {},
                    label = { Text("Time *") },
                    readOnly = true,
                    isError = timeError != null,
                    supportingText = timeError?.let { { Text(it) } },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        selectedTime = String.format("%02d:%02d", hour, minute)
                                        timeError = null
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            }
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Pick time")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Budget field
            OutlinedTextField(
                value = budget,
                onValueChange = {
                    budget = it
                    budgetError = null
                },
                label = { Text("Budget (₹) *") },
                isError = budgetError != null,
                supportingText = budgetError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Instant job toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Instant/Ultra-short job",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "10-60 minutes duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isInstantJob,
                    onCheckedChange = { isInstantJob = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message
            error?.let { errorState ->
                Text(
                    text = errorState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Create button
            Button(
                onClick = {
                    if (validateInputs(
                        title, description, city, selectedDate, selectedTime, budget,
                        onTitleError = { titleError = it },
                        onDescriptionError = { descriptionError = it },
                        onCityError = { cityError = it },
                        onDateError = { dateError = it },
                        onTimeError = { timeError = it },
                        onBudgetError = { budgetError = it }
                    )) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            val task = Task(
                                userId = currentUser.uid,
                                title = title,
                                description = description,
                                category = selectedCategory,
                                requiredEquipment = equipmentList,
                                location = Location(
                                    city = city,
                                    address = address.ifBlank { null }
                                ),
                                scheduledDate = selectedDate ?: 0L,
                                scheduledTime = selectedTime,
                                budget = budget.toDoubleOrNull() ?: 0.0,
                                isInstantJob = isInstantJob
                            )
                            viewModel.createTask(task)
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Task", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // AI Input Dialog
    if (showAIDialog) {
        AIInputDialog(
            onDismiss = { showAIDialog = false },
            onGenerate = { briefDescription ->
                viewModel.generateTaskWithAI(briefDescription)
                showAIDialog = false
            },
            isLoading = isLoading
        )
    }
    
    // Equipment dialog
    if (showEquipmentDialog) {
        AddEquipmentDialog(
            onDismiss = { showEquipmentDialog = false },
            onAdd = { equipment ->
                equipmentList = equipmentList + equipment
                showEquipmentDialog = false
            }
        )
    }
}

/**
 * Dialog for adding equipment
 */
@Composable
fun AddEquipmentDialog(
    onDismiss: () -> Unit,
    onAdd: (Equipment) -> Unit
) {
    var equipmentName by remember { mutableStateOf("") }
    var providedBy by remember { mutableStateOf(EquipmentProvider.USER) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Equipment") },
        text = {
            Column {
                OutlinedTextField(
                    value = equipmentName,
                    onValueChange = { equipmentName = it },
                    label = { Text("Equipment Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Provided by:", style = MaterialTheme.typography.labelMedium)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = providedBy == EquipmentProvider.USER,
                        onClick = { providedBy = EquipmentProvider.USER },
                        label = { Text("User") }
                    )
                    FilterChip(
                        selected = providedBy == EquipmentProvider.WORKER,
                        onClick = { providedBy = EquipmentProvider.WORKER },
                        label = { Text("Worker") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (equipmentName.isNotBlank()) {
                        onAdd(Equipment(name = equipmentName, providedBy = providedBy))
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for AI task generation input
 */
@Composable
fun AIInputDialog(
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit,
    isLoading: Boolean
) {
    var briefDescription by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Create with AI")
            }
        },
        text = {
            Column {
                Text(
                    text = "Describe your task briefly and AI will generate a professional task description for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = briefDescription,
                    onValueChange = { briefDescription = it },
                    label = { Text("Brief task description") },
                    placeholder = { Text("e.g., Need to clean my house") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generating task details...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (briefDescription.isNotBlank()) {
                        onGenerate(briefDescription)
                    }
                },
                enabled = briefDescription.isNotBlank() && !isLoading
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Validate task creation inputs
 */
private fun validateInputs(
    title: String,
    description: String,
    city: String,
    selectedDate: Long?,
    selectedTime: String,
    budget: String,
    onTitleError: (String) -> Unit,
    onDescriptionError: (String) -> Unit,
    onCityError: (String) -> Unit,
    onDateError: (String) -> Unit,
    onTimeError: (String) -> Unit,
    onBudgetError: (String) -> Unit
): Boolean {
    var isValid = true
    
    if (title.isBlank()) {
        onTitleError("Title is required")
        isValid = false
    }
    
    if (description.isBlank()) {
        onDescriptionError("Description is required")
        isValid = false
    }
    
    if (city.isBlank()) {
        onCityError("City is required")
        isValid = false
    }
    
    if (selectedDate == null) {
        onDateError("Date is required")
        isValid = false
    } else if (selectedDate < System.currentTimeMillis()) {
        onDateError("Please select a future date")
        isValid = false
    }
    
    if (selectedTime.isBlank()) {
        onTimeError("Time is required")
        isValid = false
    }
    
    if (budget.isBlank()) {
        onBudgetError("Budget is required")
        isValid = false
    } else {
        val budgetValue = budget.toDoubleOrNull()
        if (budgetValue == null || budgetValue <= 0) {
            onBudgetError("Budget must be a positive number")
            isValid = false
        }
    }
    
    return isValid
}
