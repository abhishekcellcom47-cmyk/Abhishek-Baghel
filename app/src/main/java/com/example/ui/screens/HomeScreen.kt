package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onCategorySelected: (String) -> Unit,
    onProductSelected: (ProductEntity) -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val context = LocalContext.current

    // Offers banner list
    val banners = remember {
        listOf(
            Triple("SUPER SUNDAY DEALS!", "Flat ₹100 Off on top accessories!\nApply Coupon Code: WELCOME100", Brush.linearGradient(colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))),
            Triple("FAST CHARGING SALE", "Up to 40% Off on heavy GaN Chargers!\nPower up your handsets now.", Brush.linearGradient(colors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)))),
            Triple("PROTECTION PARADISE", "Military Grade 11D Matte Glass.\nStarts from only ₹149. Order Now!", Brush.linearGradient(colors = listOf(Color(0xFFE65100), Color(0xFFFF9800)))),
            Triple("FESTIVE ACCESSORIES", "Extra 15% Off with FESTIVE15 code.\nFull cart discounts applied live.", Brush.linearGradient(colors = listOf(Color(0xFF880E4F), Color(0xFFD81B60))))
        )
    }

    var activeBannerIdx by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            activeBannerIdx = (activeBannerIdx + 1) % banners.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp) // Leave space for bottom bar
    ) {
        // Welcome and Store header Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "Madhavi Enterprises",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Quick notifications trigger button
            IconButton(
                onClick = { viewModel.addNotification("Direct Offer alert", "Get up to 30% Off on Bluetooth speakers today!") },
                modifier = Modifier.testTag("home_noti_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Notifications active tag",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 1. Search Bar
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            placeholder = { Text("Search chargers, cables, earbuds...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            trailingIcon = {
                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("search_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        // 2. Sliders banners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(banners[activeBannerIdx].third)
                .clickable {
                    viewModel.addNotification(
                        "Offer applied",
                        "Make sure to apply coupons during checkout to avail of this exclusive deal!"
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = banners[activeBannerIdx].first,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = banners[activeBannerIdx].second,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            // Dot indicators on bottom right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                banners.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == activeBannerIdx) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }

        // 3. Categories Icons list (horizontal scroll)
        val categoryIconsList = remember {
            listOf(
                "Chargers" to Icons.Default.Bolt,
                "Data Cables" to Icons.Default.Cable,
                "Earphones" to Icons.Default.Headphones,
                "Earbuds" to Icons.Default.Bluetooth,
                "Covers" to Icons.Default.Smartphone,
                "Tempered Glass" to Icons.Default.Layers,
                "Spare Parts" to Icons.Default.Build,
                "Smart Watches" to Icons.Default.Watch,
                "Bluetooth Speakers" to Icons.Default.VolumeUp,
                "Accessories" to Icons.Default.DevicesOther
            )
        }

        Text(
            text = "Browse Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categoryIconsList) { (catName, iconSource) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onCategorySelected(catName) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconSource,
                            contentDescription = catName,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = catName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // 4. Trending Deals section
        val trendingItems = products.filter { it.isTrending }
        if (trendingItems.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending Offers 🔥",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                TextButton(onClick = { onCategorySelected("All") }) {
                    Text("See All")
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trendingItems) { prod ->
                    ProductCardMini(prod = prod, onProductClick = onProductSelected, onAddToCart = { viewModel.addToCart(prod) })
                }
            }
        }

        // 5. New Arrivals
        val newArrivals = products.filter { it.isNewArrival }
        if (newArrivals.isNotEmpty()) {
            Text(
                text = "New Launch Arrivals 🌟",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(newArrivals) { prod ->
                    ProductCardMini(prod = prod, onProductClick = onProductSelected, onAddToCart = { viewModel.addToCart(prod) })
                }
            }
        }

        // 6. Best Sellers section
        val bestSellers = products.filter { it.isBestSeller }
        if (bestSellers.isNotEmpty()) {
            Text(
                text = "Best Selling Accessories 🏆",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
            )

            // Let's list best sellers in a neat flowing vertical item flow
            bestSellers.take(4).forEach { prod ->
                BestSellerRowItem(prod = prod, onProductClick = onProductSelected, onAddToCart = { viewModel.addToCart(prod) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProductCardMini(
    prod: ProductEntity,
    onProductClick: (ProductEntity) -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(145.dp)
            .clickable { onProductClick(prod) }
            .testTag("prod_mini_${prod.id}"),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Product image
            AsyncImage(
                model = prod.imageUrl,
                contentDescription = prod.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
            )

            // Category tag
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomEnd = 4.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = prod.category,
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Rating tag
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 4.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Rating star", tint = GoldYellow, modifier = Modifier.size(8.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(prod.rating.toString(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(modifier = Modifier.padding(6.dp)) {
            Text(
                text = prod.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Pricing blocks
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${prod.discountPrice.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "₹${prod.originalPrice.toInt()}",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textDecoration = TextDecoration.LineThrough
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Add-to-cart mini action
            Button(
                onClick = onAddToCart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .testTag("add_cart_btn_${prod.id}"),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = "Add Icon", modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun BestSellerRowItem(
    prod: ProductEntity,
    onProductClick: (ProductEntity) -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onProductClick(prod) }
            .testTag("best_seller_${prod.id}"),
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
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prod.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating star", tint = GoldYellow, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "${prod.rating} (${prod.reviewCount} reviews)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${prod.discountPrice.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹${prod.originalPrice.toInt()}",
                        fontSize = 9.sp,
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${((1.0 - (prod.discountPrice / prod.originalPrice)) * 100).toInt()}% OFF",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            IconButton(
                onClick = onAddToCart,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    .size(32.dp)
                    .testTag("best_seller_cart_${prod.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = "Quick add to cart",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

val GoldYellow = Color(0xFFFFB300)
