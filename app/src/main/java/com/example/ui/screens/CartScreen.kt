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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AddressEntity
import com.example.data.CartItemEntity
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val cartList by viewModel.cartItems.collectAsState()
    val addressesList by viewModel.addresses.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showCheckoutSheet by remember { mutableStateOf(false) }
    var selectedAddressIdx by remember { mutableStateOf(0) }
    var showAddAddressForm by remember { mutableStateOf(false) }

    // Shipping calculations
    val subtotal = cartList.sumOf { it.discountPrice * it.quantity }
    val shipping = if (subtotal == 0.0 || subtotal > 500) 0.0 else 40.0
    val totalWithShippingBeforeCoupon = subtotal + shipping
    val finalTotal = (totalWithShippingBeforeCoupon - viewModel.discountAmount).coerceAtLeast(0.0)

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 75.dp)
    ) {
        Text(
            text = "Your Cart 🛒",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        if (cartList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty shopping cart bag",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your shopping cart is empty!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Browse our high-quality accessories to fill it.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateToHome) {
                        Text("Start Shopping Now", fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                // Cart Items list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(cartList) { item ->
                        CartListItemRow(item = item, viewModel = viewModel)
                    }

                    // Coupon block
                    item {
                        CouponCardBlock(viewModel = viewModel, subtotal = subtotal)
                    }

                    // Bill Details
                    item {
                        BillDetailsCard(
                            subtotal = subtotal,
                            shipping = shipping,
                            discount = viewModel.discountAmount,
                            finalTotal = finalTotal,
                            appliedCoupon = viewModel.appliedCoupon
                        )
                    }
                }

                // Checkout controls bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Grand Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            Text(
                                text = "₹${finalTotal.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showCheckoutSheet = true },
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("checkout_start_btn"),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Arrow", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Checkout Integration Details
    if (showCheckoutSheet) {
        AlertDialog(
            onDismissRequest = { showCheckoutSheet = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            confirmButton = {},
            title = null,
            text = {
                var paymentMethod by remember { mutableStateOf("Razorpay") }
                var shippingAddress by remember { mutableStateOf<AddressEntity?>(null) }
                var showAddAddressInCheckout by remember { mutableStateOf(false) }

                // Address fields
                var formName by remember { mutableStateOf(userProfile?.fullName ?: "") }
                var formPhone by remember { mutableStateOf(userProfile?.phone ?: "") }
                var formArea by remember { mutableStateOf("") }
                var formLandmark by remember { mutableStateOf("") }
                var formCity by remember { mutableStateOf("") }
                var formState by remember { mutableStateOf("") }
                var formPincode by remember { mutableStateOf("") }
                var formLabel by remember { mutableStateOf("Home") }

                var isPayingState by remember { mutableStateOf(false) }
                var transactionFinished by remember { mutableStateOf(false) }
                var generatedOrderId by remember { mutableStateOf<Long?>(null) }

                LaunchedEffect(addressesList) {
                    if (addressesList.isNotEmpty() && selectedAddressIdx < addressesList.size) {
                        shippingAddress = addressesList[selectedAddressIdx]
                    }
                }

                if (isPayingState) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Connecting securely to Razorpay checkout gateway...", fontWeight = FontWeight.Bold)
                            Text("Please do not press back or reload", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                } else if (transactionFinished) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color(0xFF00897B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Check", tint = Color.White, modifier = Modifier.size(44.dp))
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Order Placed Successfully! 🎉", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your order #ME${generatedOrderId} for ₹${finalTotal.toInt()} has been successfully authorized and recorded.",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    showCheckoutSheet = false
                                    viewModel.clearCart()
                                    viewModel.removeCoupon()
                                    onNavigateToOrders()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("View Order Details", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Checkout Store order",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                            IconButton(onClick = { showCheckoutSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close checkout dialog")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // 1. Delivery Address Block
                        Text("1. DELIVERY ADDRESS CHOICE", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

                        if (addressesList.isEmpty() || showAddAddressInCheckout) {
                            // Render Address input forms right inside checkout
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Add Delivery Address", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                                    OutlinedTextField(value = formName, onValueChange = { formName = it }, label = { Text("Customer Full Name") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                    OutlinedTextField(value = formPhone, onValueChange = { formPhone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                    OutlinedTextField(value = formArea, onValueChange = { formArea = it }, label = { Text("Flat No, Building, Street / Sector") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                    OutlinedTextField(value = formLandmark, onValueChange = { formLandmark = it }, label = { Text("Landmark (Optional)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = formCity, onValueChange = { formCity = it }, label = { Text("City") }, modifier = Modifier.weight(1f))
                                        OutlinedTextField(value = formState, onValueChange = { formState = it }, label = { Text("State") }, modifier = Modifier.weight(1f))
                                    }

                                    OutlinedTextField(value = formPincode, onValueChange = { formPincode = it }, label = { Text("Pincode (6-digit)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                                    // Tag selector
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        FilterChip(selected = formLabel == "Home", onClick = { formLabel = "Home" }, label = { Text("Home Recipient") })
                                        FilterChip(selected = formLabel == "Work", onClick = { formLabel = "Work" }, label = { Text("Office/Work") })
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (formName.isNotEmpty() && formPhone.isNotEmpty() && formArea.isNotEmpty() && formCity.isNotEmpty() && formPincode.isNotEmpty()) {
                                                viewModel.saveAddress(formName, formPhone, formArea, formLandmark, formCity, formState, formPincode, formLabel)
                                                showAddAddressInCheckout = false
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save Address and Select", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // Display selectable addresses
                            addressesList.forEachIndexed { idx, addr ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable {
                                            selectedAddressIdx = idx
                                            shippingAddress = addr
                                        },
                                    border = BorderStroke(
                                        width = if (selectedAddressIdx == idx) 2.dp else 1.dp,
                                        color = if (selectedAddressIdx == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = selectedAddressIdx == idx, onClick = {
                                            selectedAddressIdx = idx
                                            shippingAddress = addr
                                        })
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(addr.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Badge { Text(addr.label) }
                                            }
                                            Text("${addr.area}, ${addr.landmark.ifEmpty { "" }}", fontSize = 12.sp, color = Color.Gray)
                                            Text("${addr.city}, ${addr.state} - ${addr.pincode}", fontSize = 12.sp, color = Color.Gray)
                                            Text("Contact: ${addr.phone}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }

                            TextButton(onClick = { showAddAddressInCheckout = true }, modifier = Modifier.align(Alignment.End)) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("Deliver to a New Address", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Payment Interface
                        Text("2. PAYMENT OPTIONS METHOD", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

                        val methods = listOf("Razorpay Secure Pay", "BHIM UPI Payment Link", "Cash on Delivery (COD)")
                        methods.forEach { m ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { paymentMethod = m },
                                border = BorderStroke(
                                    1.dp,
                                    if (paymentMethod == m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = paymentMethod == m, onClick = { paymentMethod = m })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = when {
                                            m.contains("Razorpay") -> Icons.Default.Security
                                            m.contains("UPI") -> Icons.Default.QrCode
                                            else -> Icons.Default.LocalShipping
                                        },
                                        contentDescription = m
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(m, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Trigger Checkout Pay
                        Button(
                            onClick = {
                                val addr = shippingAddress
                                if (addr == null) {
                                    viewModel.addNotification(
                                        "Validation Error",
                                        "Please provide and select a delivery address for Madhavi Enterprises dispatch coordinates."
                                    )
                                    return@Button
                                }

                                isPayingState = true
                                scope.launch {
                                    // Simulate payment processing latency
                                    delay(2000)
                                    viewModel.checkAndPlaceOrder(
                                        fullName = addr.fullName,
                                        phone = addr.phone,
                                        addressString = "${addr.area}, ${addr.landmark.ifEmpty { "" }}, ${addr.city}, ${addr.state} - ${addr.pincode}",
                                        paymentMethod = paymentMethod,
                                        totalAmount = finalTotal,
                                        itemsList = cartList,
                                        onSuccess = { orderId ->
                                            generatedOrderId = orderId
                                            isPayingState = false
                                            transactionFinished = true
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("pay_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("PAY & CONFIRM ORDER  (₹${finalTotal.toInt()})", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun CartListItemRow(
    item: CartItemEntity,
    viewModel: MainViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("cart_item_${item.id}"),
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
                model = item.imageUrl,
                contentDescription = item.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.category,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${item.discountPrice.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${item.originalPrice.toInt()}",
                        fontSize = 9.sp,
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                        color = Color.LightGray
                    )
                }
            }

            // Quantity adjusters
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { viewModel.removeFromCart(item.id) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete item", tint = Color.Red, modifier = Modifier.size(14.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.updateCartQty(item.id, item.quantity - 1) },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                            .size(22.dp)
                            .testTag("cart_qty_minus_${item.id}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Text(
                        text = item.quantity.toString(),
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontSize = 11.sp
                    )

                    IconButton(
                        onClick = { viewModel.updateCartQty(item.id, item.quantity + 1) },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .size(22.dp)
                            .testTag("cart_qty_plus_${item.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(10.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponCardBlock(
    viewModel: MainViewModel,
    subtotal: Double
) {
    var couponText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text("Apply Promo Discount Coupon", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = couponText,
                    onValueChange = { couponText = it },
                    placeholder = { Text("Coupon Code, e.g. FESTIVE15") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("coupon_input"),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = {
                        if (couponText.trim().isNotEmpty()) {
                            viewModel.applyCouponCode(couponText, subtotal)
                        }
                    },
                    modifier = Modifier.testTag("apply_coupon_btn"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Coupon applied feedback status
            AnimatedVisibility(visible = viewModel.appliedCoupon.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Celebration, contentDescription = "Active coupon", tint = Color(0xFF004D40))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Code ${viewModel.appliedCoupon} applied! Saved ₹${viewModel.discountAmount.toInt()}",
                            color = Color(0xFF004D40),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = {
                        viewModel.removeCoupon()
                        couponText = ""
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel coupon", tint = Color(0xFF004D40))
                    }
                }
            }

            AnimatedVisibility(visible = viewModel.couponErrorMessage != null) {
                Text(
                    text = viewModel.couponErrorMessage ?: "",
                    color = Color.Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Divider(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))

            // Short coupon catalog tips
            Text("Tip: Try code WELCOME100 or FESTIVE15 on products", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BillDetailsCard(
    subtotal: Double,
    shipping: Double,
    discount: Double,
    finalTotal: Double,
    appliedCoupon: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Detailed Bill summary", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Basket Subtotal", fontSize = 12.sp, color = Color.Gray)
                Text("₹${subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Shipping Fee", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = if (shipping == 0.0) "FREE" else "₹${shipping.toInt()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (shipping == 0.0) Color(0xFF00897B) else MaterialTheme.colorScheme.onSurface
                )
            }

            if (discount > 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Coupon Discount ($appliedCoupon)", fontSize = 12.sp, color = Color(0xFF00897B))
                    Text("-₹${discount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00897B))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Customer Total due", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                Text("₹${finalTotal.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
