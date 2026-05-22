package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddressEntity
import com.example.data.OrderEntity
import com.example.ui.MainViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AccountScreen(
    viewModel: MainViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val ordersList by viewModel.orders.collectAsState()
    val addressesList by viewModel.addresses.collectAsState()

    var showAddressFormInAccount by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 75.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your Profile 👤",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        if (userProfile == null) {
            // UN-AUTHENTICATED: Interactive Mobile SMS login flow
            LoginFormBlock(viewModel = viewModel)
        } else {
            // AUTHENTICATED: Display active profiles, addresses and historic orders
            val profile = userProfile!!

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.fullName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.fullName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Phone: +91 ${profile.phone}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                        if (profile.email.isNotEmpty()) {
                            Text(
                                text = profile.email,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = Color.White)
                    }
                }
            }

            // Order History Section
            Text(
                text = "My Order History 📦",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (ordersList.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "You haven't placed any orders yet.",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                ordersList.forEach { order ->
                    OrderHistoryItemCard(order = order)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved Addresses Admin Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Delivery addresses 📍",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                IconButton(onClick = { showAddressFormInAccount = !showAddressFormInAccount }) {
                    Icon(
                        imageVector = if (showAddressFormInAccount) Icons.Default.Close else Icons.Default.AddCircle,
                        contentDescription = "New Address Toggle",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showAddressFormInAccount) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    var inputName by remember { mutableStateOf(profile.fullName) }
                    var inputPhone by remember { mutableStateOf(profile.phone) }
                    var inputArea by remember { mutableStateOf("") }
                    var inputLandmark by remember { mutableStateOf("") }
                    var inputCity by remember { mutableStateOf("") }
                    var inputState by remember { mutableStateOf("") }
                    var inputPincode by remember { mutableStateOf("") }
                    var inputLabel by remember { mutableStateOf("Home") }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Register Delivery Location", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

                        OutlinedTextField(value = inputName, onValueChange = { inputName = it }, label = { Text("Contact Name") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                        OutlinedTextField(value = inputPhone, onValueChange = { inputPhone = it }, label = { Text("Contact Phone") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                        OutlinedTextField(value = inputArea, onValueChange = { inputArea = it }, label = { Text("Flat No, Sector, Area Road") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                        OutlinedTextField(value = inputLandmark, onValueChange = { inputLandmark = it }, label = { Text("Landmark (Optional)") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = inputCity, onValueChange = { inputCity = it }, label = { Text("City") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f))
                            OutlinedTextField(value = inputState, onValueChange = { inputState = it }, label = { Text("State") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f))
                        }

                        OutlinedTextField(value = inputPincode, onValueChange = { inputPincode = it }, label = { Text("Pincode (6-digit)") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = inputLabel == "Home", onClick = { inputLabel = "Home" }, label = { Text("Home Address") }, shape = RoundedCornerShape(4.dp))
                            FilterChip(selected = inputLabel == "Work", onClick = { inputLabel = "Work" }, label = { Text("Office/Work") }, shape = RoundedCornerShape(4.dp))
                        }

                        Button(
                            onClick = {
                                if (inputName.isNotEmpty() && inputPhone.isNotEmpty() && inputArea.isNotEmpty() && inputCity.isNotEmpty() && inputPincode.isNotEmpty()) {
                                    viewModel.saveAddress(inputName, inputPhone, inputArea, inputLandmark, inputCity, inputState, inputPincode, inputLabel)
                                    showAddressFormInAccount = false
                                }
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Register Address Location", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            addressesList.forEach { addr ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location pins", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(addr.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge { Text(addr.label) }
                            }
                            Text("${addr.area}, ${addr.landmark.ifEmpty { "" }}", fontSize = 11.sp, color = Color.Gray)
                            Text("${addr.city}, ${addr.state} - ${addr.pincode}", fontSize = 11.sp, color = Color.Gray)
                            Text("Phone: ${addr.phone}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { viewModel.deleteAddress(addr.addressId) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete address", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginFormBlock(viewModel: MainViewModel) {
    var rawName by remember { mutableStateOf("") }
    var rawPhone by remember { mutableStateOf("") }
    var rawEmail by remember { mutableStateOf("") }
    var rawOtp by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Access Account Profile 📱",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Enter phone number and name credentials to login with dynamic OTP validation.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            if (!viewModel.isOtpSent) {
                OutlinedTextField(
                    value = rawName,
                    onValueChange = { rawName = it },
                    label = { Text("Your Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person") },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("login_name_input")
                )

                OutlinedTextField(
                    value = rawPhone,
                    onValueChange = { rawPhone = it },
                    label = { Text("Mobile Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("login_phone_input")
                )

                OutlinedTextField(
                    value = rawEmail,
                    onValueChange = { rawEmail = it },
                    label = { Text("Email Address (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("login_email_input")
                )

                if (viewModel.loginErrorMessage.isNotEmpty()) {
                    Text(viewModel.loginErrorMessage, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.sendOtp(rawPhone, rawName, rawEmail) },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("otp_send_btn"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Request OTP Verification Code", fontWeight = FontWeight.Black)
                }
            } else {
                // SMS verification OTP stage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "OTP SMS code sent successfully to +91 ${viewModel.loginPhoneNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = rawOtp,
                    onValueChange = { rawOtp = it },
                    label = { Text("Enter 6-digit Security code") },
                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = "Shield") },
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).testTag("login_otp_input")
                )

                if (viewModel.loginErrorMessage.isNotEmpty()) {
                    Text(viewModel.loginErrorMessage, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.verifyOtp(rawOtp, onSuccess = {
                            rawName = ""
                            rawPhone = ""
                            rawEmail = ""
                            rawOtp = ""
                        })
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("otp_verify_btn"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Secure OTP Verification", fontWeight = FontWeight.Black)
                }

                TextButton(
                    onClick = { viewModel.isOtpSent = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                ) {
                    Text("Change login credentials")
                }
            }
        }
    }
}

@Composable
fun OrderHistoryItemCard(order: OrderEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Order ID: #ME${order.orderId}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Badge(
                    containerColor = when (order.status) {
                        "Pending" -> Color(0xFFFFF9C4)
                        "Confirmed" -> Color(0xFFC8E6C9)
                        "Shipped" -> Color(0xFFE1BEE7)
                        else -> Color(0xFFB2DFDB)
                    }
                ) {
                    Text(
                        order.status.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.itemsSummary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Billing total payment", fontSize = 10.sp, color = Color.Gray)
                    Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Dispatch type", fontSize = 10.sp, color = Color.Gray)
                    Text(order.paymentMethod, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step timeline tracker
            TimelineTrackerWidget(status = order.status)
        }
    }
}

@Composable
fun TimelineTrackerWidget(status: String) {
    val steps = listOf("Confirmed", "Shipped", "Delivered")
    val currentIndex = when (status) {
        "Confirmed" -> 0
        "Shipped" -> 1
        "Delivered" -> 2
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, step ->
            val isPassed = idx <= currentIndex
            val isCurrent = idx == currentIndex

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            if (isPassed) MaterialTheme.colorScheme.primary else Color.LightGray,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(10.dp))
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = step,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                    color = if (isPassed) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.weight(1f)
                )

                if (idx < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .weight(1f)
                            .background(if (idx < currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray)
                    )
                }
            }
        }
    }
}
