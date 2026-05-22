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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.OrderEntity
import com.example.data.ProductEntity
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MainViewModel
) {
    val products by viewModel.allProducts.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Analytics") } // Analytics, Inventory, Orders

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 75.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Admin Dashboard 🛠️",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            if (viewModel.adminIsLoggedIn) {
                IconButton(onClick = { viewModel.adminIsLoggedIn = false }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Exit Admin", tint = Color.Red)
                }
            }
        }

        if (!viewModel.adminIsLoggedIn) {
            // Admin Gating authorization credentials page
            AdminGateBlock(viewModel = viewModel)
        } else {
            // Tab switch controllers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Analytics", "Inventory", "Orders").forEach { t ->
                    ElevatedFilterChip(
                        selected = selectedTab == t,
                        onClick = { selectedTab = t },
                        label = { Text(t, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("admin_tab_$t")
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Body rendering relative to selected section
            when (selectedTab) {
                "Analytics" -> AnalyticsViewBlock(viewModel = viewModel)
                "Inventory" -> InventoryViewBlock(viewModel = viewModel, products = products, onAddNewProduct = { showAddProductDialog = true })
                "Orders" -> OrdersViewBlock(viewModel = viewModel, orders = orders)
            }
        }
    }

    // Modal Add/Edit product launcher sheet
    if (showAddProductDialog) {
        ProductEditorDialog(
            viewModel = viewModel,
            onDismiss = { showAddProductDialog = false }
        )
    }
}

@Composable
fun AdminGateBlock(viewModel: MainViewModel) {
    var userVal by remember { mutableStateOf("") }
    var passVal by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Administrative Gated Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text("Enter administrator pass keys to manage inventory, update shipping order stages, and check sales charts.", fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userVal,
                onValueChange = { userVal = it },
                label = { Text("Admin Username") },
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_user_field")
            )

            OutlinedTextField(
                value = passVal,
                onValueChange = { passVal = it },
                label = { Text("Password Key") },
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("admin_pass_field")
            )

            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (userVal == viewModel.adminUsername && passVal == viewModel.adminPassword) {
                        viewModel.adminIsLoggedIn = true
                        errorMsg = ""
                    } else {
                        errorMsg = "Invalid username or password"
                    }
                },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("admin_login_btn")
            ) {
                Text("Verify Pass Keys", fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Hint: username is 'admin', password is 'password123'",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun AnalyticsViewBlock(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text("Store Revenue Analytics Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(10.dp))

        // Total sales value box card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Simulated Business Revenue", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp)
                Text(
                    text = "₹${String.format("%.2f", viewModel.getTotalSalesRevenue())}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom canvas graphics line chart e.g. Monthly Revenue
        Text("Monthly Revenue trend curve", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Draws customized 5-point data curve
                val points = listOf(12000f, 18000f, 15000f, 29000f, 38000f)
                val width = size.width
                val height = size.height
                val pointCount = points.size

                val path = Path()
                points.forEachIndexed { index, value ->
                    val x = (width / (pointCount - 1)) * index
                    val maxVal = 50000f
                    val y = height - (height * (value / maxVal))
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    // draw circular nodes
                    drawCircle(color = Color(0xFF2196F3), radius = 4f, center = Offset(x, y))
                }

                // draw the line overlay
                drawPath(path = path, color = Color(0xFF0D47A1), style = Stroke(width = 3.5f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category breakdown visual bars represent
        Text("Sales Breakdown by Accessories Category", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        val salesData = viewModel.getSalesByCategory()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                salesData.forEach { (cat, valData) ->
                    val total = salesData.values.sum()
                    val fraction = if (total == 0.0) 0f else (valData / total).toFloat()

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("₹${valData.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Custom horizontal indicator bar representation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryViewBlock(
    viewModel: MainViewModel,
    products: List<ProductEntity>,
    onAddNewProduct: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Store Catalogue Inventory (${products.size} Items)", fontWeight = FontWeight.ExtraBold)

            IconButton(onClick = onAddNewProduct, modifier = Modifier.testTag("admin_fab_product")) {
                Icon(Icons.Default.AddBox, contentDescription = "Add Product tag", tint = MaterialTheme.colorScheme.primary)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(products) { prod ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = prod.imageUrl,
                            contentDescription = prod.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Category: ${prod.category} | Stock: ${prod.stockAvailability}", fontSize = 11.sp, color = Color.Gray)
                            Text("Selling price: ₹${prod.discountPrice.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        // Edit / Delete ops
                        IconButton(onClick = { viewModel.deleteAdminProduct(prod.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete admin prod", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersViewBlock(
    viewModel: MainViewModel,
    orders: List<OrderEntity>
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer orders recorded locally", color = Color.Gray)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order #ME${order.orderId}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Recipient: ${order.addressFullName} (${order.addressPhone})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Address: ${order.addressLine}", fontSize = 11.sp, color = Color.Gray)
                        Text("Items: ${order.itemsSummary}", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Shipping Stage: ${order.status.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            // Quick adjust stage
                            Button(
                                onClick = {
                                    val nextStatus = when (order.status) {
                                        "Pending" -> "Confirmed"
                                        "Confirmed" -> "Shipped"
                                        "Shipped" -> "Delivered"
                                        else -> "Confirmed"
                                    }
                                    viewModel.updateOrderStatusByAdmin(order.orderId, nextStatus)
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Advance Stage", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditorDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var nameVal by remember { mutableStateOf("") }
    var catVal by remember { mutableStateOf("Chargers") } // default
    var descVal by remember { mutableStateOf("") }
    var origVal by remember { mutableStateOf("") }
    var discVal by remember { mutableStateOf("") }
    var imgVal by remember { mutableStateOf("") }
    var stockVal by remember { mutableStateOf("15") }

    var isNew by remember { mutableStateOf(true) }
    var isTrend by remember { mutableStateOf(false) }
    var isBest by remember { mutableStateOf(false) }

    val masterCategories = listOf("Chargers", "Data Cables", "Earphones", "Earbuds", "Covers", "Tempered Glass", "Spare Parts", "Smart Watches", "Bluetooth Speakers", "Accessories")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Catalog Product", fontWeight = FontWeight.Black) },
        shape = RoundedCornerShape(4.dp),
        confirmButton = {
            Button(
                onClick = {
                    if (nameVal.isNotEmpty() && origVal.isNotEmpty() && discVal.isNotEmpty()) {
                        viewModel.saveAdminProduct(
                            name = nameVal,
                            category = catVal,
                            description = descVal.ifEmpty { "High premium mobile accessory item supplied directly by Madhavi Enterprises." },
                            originalPrice = origVal.toDoubleOrNull() ?: 499.0,
                            discountPrice = discVal.toDoubleOrNull() ?: 299.0,
                            imageUrl = imgVal,
                            stock = stockVal.toIntOrNull() ?: 15,
                            isNew = isNew,
                            isTrend = isTrend,
                            isBest = isBest
                        )
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Insert Product")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Cancel")
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = nameVal, onValueChange = { nameVal = it }, label = { Text("Product Name") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))

                // Select category slider mock
                Text("Select Category", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                ScrollableTabRow(
                    selectedTabIndex = masterCategories.indexOf(catVal).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    indicator = {},
                    divider = {}
                ) {
                    masterCategories.forEach { c ->
                        Tab(
                            selected = catVal == c,
                            onClick = { catVal = c }
                        ) {
                            SuggestionChip(
                                onClick = { catVal = c },
                                label = { Text(c) },
                                shape = RoundedCornerShape(4.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (catVal == c) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    labelColor = if (catVal == c) Color.White else MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = descVal, onValueChange = { descVal = it }, label = { Text("Specifications / Description") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = origVal, onValueChange = { origVal = it }, label = { Text("Original Price (MRP)") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = discVal, onValueChange = { discVal = it }, label = { Text("Selling Price (Discounted)") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imgVal, onValueChange = { imgVal = it }, label = { Text("Image URL link illustrative") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stockVal, onValueChange = { stockVal = it }, label = { Text("Quantity in Stock") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))

                Text("Visual Promotional Badges", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isNew, onCheckedChange = { isNew = it })
                    Text("New Product Launch", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isTrend, onCheckedChange = { isTrend = it })
                    Text("Trending product", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isBest, onCheckedChange = { isBest = it })
                    Text("Best Seller", fontSize = 12.sp)
                }
            }
        }
    )
}
