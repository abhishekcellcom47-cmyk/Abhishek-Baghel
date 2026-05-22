package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.NotificationModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer() {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf("home") }
    var selectedCategoryForCatalog by remember { mutableStateOf<String?>("All") }

    // Overlays toggle controllers
    val selectedProduct = viewModel.selectedProduct
    var showWishlistDialog by remember { mutableStateOf(false) }
    var showNotiHistoryDialog by remember { mutableStateOf(false) }

    // Collect variables
    val notificationsList by viewModel.notifications.collectAsState()
    val wishlistList by viewModel.wishlistItems.collectAsState()
    val productsList by viewModel.allProducts.collectAsState()

    // 1. Popup HUD State Manager
    var activePopNotification by remember { mutableStateOf<NotificationModel?>(null) }
    LaunchedEffect(notificationsList) {
        if (notificationsList.isNotEmpty()) {
            activePopNotification = notificationsList.first()
            delay(4000)
            activePopNotification = null
        }
    }

    // Splash screen finish logic
    var isSplashFinished by remember { mutableStateOf(false) }

    if (!isSplashFinished) {
        SplashScreen(onSplashFinished = { isSplashFinished = true })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Store, contentDescription = "Store head", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Madhavi Enterprises",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                        },
                        actions = {
                            // Favorites list button
                            IconButton(onClick = { showWishlistDialog = true }, modifier = Modifier.testTag("wishlist_top_btn")) {
                                Box {
                                    Icon(Icons.Default.Favorite, contentDescription = "Favorites list", tint = Color.Red)
                                    if (wishlistList.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                .align(Alignment.TopEnd)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${wishlistList.size}", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Notifications History list button
                            IconButton(onClick = { showNotiHistoryDialog = true }, modifier = Modifier.testTag("noti_top_btn")) {
                                Box {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications list", tint = MaterialTheme.colorScheme.primary)
                                    if (notificationsList.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(Color.Red, CircleShape)
                                                .align(Alignment.TopEnd)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${notificationsList.size}", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = activeTab == "home",
                            onClick = { activeTab = "home" },
                            icon = { Icon(if (activeTab == "home") Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home tab") },
                            label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_home")
                        )

                        NavigationBarItem(
                            selected = activeTab == "catalog",
                            onClick = {
                                selectedCategoryForCatalog = "All"
                                activeTab = "catalog"
                            },
                            icon = { Icon(if (activeTab == "catalog") Icons.Default.Category else Icons.Outlined.Category, contentDescription = "Catalogue tab") },
                            label = { Text("Catalogue", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_catalog")
                        )

                        NavigationBarItem(
                            selected = activeTab == "cart",
                            onClick = { activeTab = "cart" },
                            icon = { Icon(if (activeTab == "cart") Icons.Default.ShoppingCart else Icons.Outlined.ShoppingCart, contentDescription = "Cart tab") },
                            label = { Text("Cart", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_cart")
                        )

                        NavigationBarItem(
                            selected = activeTab == "account",
                            onClick = { activeTab = "account" },
                            icon = { Icon(if (activeTab == "account") Icons.Default.AccountCircle else Icons.Outlined.AccountCircle, contentDescription = "Account tab") },
                            label = { Text("Account", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_account")
                        )

                        NavigationBarItem(
                            selected = activeTab == "admin",
                            onClick = { activeTab = "admin" },
                            icon = { Icon(if (activeTab == "admin") Icons.Default.Settings else Icons.Outlined.Settings, contentDescription = "Admin tab") },
                            label = { Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_admin")
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (activeTab) {
                        "home" -> HomeScreen(
                            viewModel = viewModel,
                            onCategorySelected = { cat ->
                                selectedCategoryForCatalog = cat
                                activeTab = "catalog"
                            },
                            onProductSelected = { prod ->
                                viewModel.selectedProduct = prod
                            }
                        )

                        "catalog" -> CatalogScreen(
                            viewModel = viewModel,
                            initialCategory = selectedCategoryForCatalog,
                            onProductSelected = { prod ->
                                viewModel.selectedProduct = prod
                            }
                        )

                        "cart" -> CartScreen(
                            viewModel = viewModel,
                            onNavigateToHome = { activeTab = "home" },
                            onNavigateToOrders = { activeTab = "account" }
                        )

                        "account" -> AccountScreen(viewModel = viewModel)

                        "admin" -> AdminScreen(viewModel = viewModel)
                    }
                }
            }

            // 2. GREEN FLOATING ACTION Support Whatapp Button
            FloatingActionButton(
                onClick = {
                    openWhatsAppChat(context, "+919876543210")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 84.dp, end = 16.dp)
                    .size(54.dp)
                    .testTag("whatsapp_fab"),
                shape = CircleShape,
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = "Contact WhatsApp support button link",
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3. Dynamic Realtime Push alerts overlay HUD
            AnimatedVisibility(
                visible = activePopNotification != null,
                enter = slideInVertically(initialOffsetY = { -200 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -200 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                activePopNotification?.let { noti ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNotiHistoryDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Active alarms", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(noti.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(noti.body, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // Wishlist Overlay Dialog Board
            if (showWishlistDialog) {
                WishlistDialogBoard(
                    viewModel = viewModel,
                    onDismiss = { showWishlistDialog = false },
                    onProductClick = { prod ->
                        showWishlistDialog = false
                        viewModel.selectedProduct = prod
                    }
                )
            }

            // Notifications history overlaydialog
            if (showNotiHistoryDialog) {
                NotificationsLogBoard(
                    viewModel = viewModel,
                    onDismiss = { showNotiHistoryDialog = false }
                )
            }

            // Product catalogue detailed overlay dialog
            selectedProduct?.let { product ->
                ProductDetailOverlay(
                    product = product,
                    viewModel = viewModel,
                    onDismiss = { viewModel.selectedProduct = null }
                )
            }
        }
    }
}

fun openWhatsAppChat(context: Context, number: String) {
    try {
        val message = "Hello Madhavi Enterprises, I am browsing your mobile accessories app and would like to coordinate sales details."
        val url = "https://api.whatsapp.com/send?phone=$number&text=" + Uri.encode(message)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Redirecting to Support: +91 9876543210 (WhatsApp not installed)", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistDialogBoard(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onProductClick: (ProductEntity) -> Unit
) {
    val wishlistList by viewModel.wishlistItems.collectAsState()
    val productsList by viewModel.allProducts.collectAsState()

    val favProducts = productsList.filter { p -> wishlistList.any { w -> w.id == p.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Wishlist Favorites ❤️", fontWeight = FontWeight.Black) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Back to shopping")
            }
        },
        text = {
            Column(modifier = Modifier.height(300.dp)) {
                if (favProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Your wishlist is empty.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn {
                        items(favProducts) { prod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { onProductClick(prod) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = prod.imageUrl,
                                    contentDescription = prod.name,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("₹${prod.discountPrice.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.toggleWishlist(prod.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsLogBoard(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val notificationsList by viewModel.notifications.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Notifications log 📢", fontWeight = FontWeight.Black)
                TextButton(onClick = { viewModel.clearNotifications() }) {
                    Text("Clear All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        text = {
            Column(modifier = Modifier.height(300.dp)) {
                if (notificationsList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notifications recorded.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn {
                        items(notificationsList) { noti ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(noti.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(noti.body, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}


