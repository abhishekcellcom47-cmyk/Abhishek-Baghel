package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailOverlay(
    product: ProductEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val isFav by viewModel.isWishlisted(product.id).collectAsState(initial = false)
    val reviews by viewModel.getProductReviews(product.id).collectAsState(initial = emptyList())
    val recentlyViewed by viewModel.recentlyViewed.collectAsState()

    var showWriteReviewSection by remember { mutableStateOf(false) }
    var ratingStars by remember { mutableStateOf(5) }
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(product) {
        viewModel.addToRecentViewed(product)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("detail_close_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    Text("Product Details", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)

                    IconButton(onClick = { viewModel.toggleWishlist(product.id) }, modifier = Modifier.testTag("detail_fav_btn")) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (isFav) Color.Red else Color.Gray
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // 1. Gallery
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Text(
                            text = "Category: ${product.category}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Title & Ratings
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Stars rating", tint = GoldYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${product.rating}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${product.reviewCount} customer reviews)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Pricing
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "₹${product.discountPrice.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "₹${product.originalPrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough),
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    val discountPercent = ((1.0 - (product.discountPrice / product.originalPrice)) * 100).toInt()
                    Badge(
                        containerColor = Color(0xFFC8E6C9),
                        contentColor = Color(0xFF1B5E20),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text("$discountPercent% OFF", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                // Stock badges
                val isLowStock = product.stockAvailability <= 10
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (isLowStock) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLowStock) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = "Stock validation",
                            tint = if (isLowStock) Color.Red else Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLowStock) "Hurry! Only ${product.stockAvailability} Left in Stock!" else "Stock Status: In Stock (${product.stockAvailability} pieces ready)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) Color.Red else Color(0xFF2E7D32)
                        )
                    }
                }

                // 3. CTA Main Buttons Drawer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.addToCart(product)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("detail_add_cart_btn"),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Shopping Cart", fontWeight = FontWeight.Black)
                    }
                }

                // 4. Description specifications
                Text(
                    text = "Product Specifications",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // 5. Dynamic Reviews section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer Ratings & Reviews (${reviews.size})",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )

                    TextButton(onClick = { showWriteReviewSection = !showWriteReviewSection }) {
                        Text(if (showWriteReviewSection) "Close Form" else "Write a Review", fontWeight = FontWeight.Bold)
                    }
                }

                if (showWriteReviewSection) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Rate this product:", fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                                (1..5).forEach { star ->
                                    IconButton(onClick = { ratingStars = star }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            imageVector = if (star <= ratingStars) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "$star stars rating trigger",
                                            tint = GoldYellow
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Write your honest experience with this accessory...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Button(
                                onClick = {
                                    if (commentText.isNotEmpty()) {
                                        viewModel.addReview(product.id, commentText, ratingStars)
                                        commentText = ""
                                        showWriteReviewSection = false
                                    }
                                },
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .align(Alignment.End)
                            ) {
                                Text("Submit Review")
                            }
                        }
                    }
                }

                // Reviews List
                if (reviews.isEmpty()) {
                    Text(
                        text = "No written reviews yet. Be the first to review this accessory!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    reviews.forEach { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(r.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                    // Render selected stars
                                    Row {
                                        (1..5).forEach { star ->
                                            Icon(
                                                imageVector = if (star <= r.userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "ratings",
                                                tint = GoldYellow,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(r.comment, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f))
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // 6. Recently viewed items
                val cleanRecent = recentlyViewed.filter { it.id != product.id }
                if (cleanRecent.isNotEmpty()) {
                    Text(
                        text = "Recently Viewed Items 🕒",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cleanRecent) { prod ->
                            Card(
                                modifier = Modifier
                                    .width(110.dp)
                                    .clickable {
                                        viewModel.selectedProduct = prod
                                    },
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column {
                                    AsyncImage(
                                        model = prod.imageUrl,
                                        contentDescription = prod.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        prod.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    )
}
