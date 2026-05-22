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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: MainViewModel,
    initialCategory: String?,
    onProductSelected: (ProductEntity) -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val products = remember(allProducts, viewModel.searchQuery, viewModel.selectedCategory, viewModel.sortAscending) {
        var list = allProducts.filter {
            (viewModel.selectedCategory == null || it.category.equals(viewModel.selectedCategory, ignoreCase = true)) &&
            (viewModel.searchQuery.isEmpty() || it.name.contains(viewModel.searchQuery, ignoreCase = true) || it.category.contains(viewModel.searchQuery, ignoreCase = true))
        }
        if (viewModel.sortAscending != null) {
            list = if (viewModel.sortAscending!!) {
                list.sortedBy { it.discountPrice }
            } else {
                list.sortedByDescending { it.discountPrice }
            }
        }
        list
    }
    var currentFilterCategory by remember { mutableStateOf(initialCategory ?: "All") }

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            currentFilterCategory = initialCategory
            viewModel.selectedCategory = if (initialCategory == "All") null else initialCategory
        }
    }

    val categories = remember {
        listOf("All", "Chargers", "Data Cables", "Earphones", "Earbuds", "Covers", "Tempered Glass", "Spare Parts", "Smart Watches", "Bluetooth Speakers", "Accessories")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 75.dp)
    ) {
        // 1. Horizontal Category Selector Header
        Text(
            text = "Product Catalogue",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(currentFilterCategory).coerceAtLeast(0),
            edgePadding = 16.dp,
            divider = {},
            indicator = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { cat ->
                val isSelected = currentFilterCategory == cat
                Tab(
                    selected = isSelected,
                    onClick = {
                        currentFilterCategory = cat
                        viewModel.selectedCategory = if (cat == "All") null else cat
                    },
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            currentFilterCategory = cat
                            viewModel.selectedCategory = if (cat == "All") null else cat
                        },
                        label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            labelColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }

        // 2. Filter & Sorting Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        viewModel.sortAscending = when (viewModel.sortAscending) {
                            null -> true     // Low to High
                            true -> false    // High to Low
                            false -> null    // Default
                        }
                    }
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = when (viewModel.sortAscending) {
                        true -> Icons.Default.ArrowUpward
                        false -> Icons.Default.ArrowDownward
                        else -> Icons.Default.Sort
                    },
                    contentDescription = "Sort Icon",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (viewModel.sortAscending) {
                        true -> "Price: Low to High"
                        false -> "Price: High to Low"
                        else -> "Sort: Default"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick reset filters helper
            if (viewModel.searchQuery.isNotEmpty() || viewModel.sortAscending != null) {
                TextButton(onClick = {
                    viewModel.searchQuery = ""
                    viewModel.sortAscending = null
                    currentFilterCategory = "All"
                    viewModel.selectedCategory = null
                }) {
                    Text("Reset Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Grid Catalogue listing
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty products list",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Accessories found matching filters.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("catalog_grid")
            ) {
                items(products) { prod ->
                    ProductGridCard(
                        prod = prod,
                        viewModel = viewModel,
                        onClick = { onProductSelected(prod) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGridCard(
    prod: ProductEntity,
    viewModel: MainViewModel,
    onClick: () -> Unit
) {
    val isFav by viewModel.isWishlisted(prod.id).collectAsState(initial = false)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("catalog_item_${prod.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = prod.imageUrl,
                contentDescription = prod.name,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            )

            // Favorited/Wishlist Toggle Float button
            IconButton(
                onClick = { viewModel.toggleWishlist(prod.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .size(28.dp)
                    .testTag("fav_btn_${prod.id}")
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Wishlist tag",
                    tint = if (isFav) Color.Red else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Quick Stock Alert banner if stock is low - High Density badge style
            if (prod.stockAvailability <= 10) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(bottomEnd = 4.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "LOW STOCK: ${prod.stockAvailability}",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            } else {
                // Stock Badge: In Stock green
                Surface(
                    color = EmeraldGreen,
                    shape = RoundedCornerShape(bottomEnd = 4.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "IN STOCK",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = prod.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 8.sp
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = prod.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                modifier = Modifier.height(28.dp),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rating Stars Summary
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = "Star ratings", tint = GoldYellow, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${prod.rating}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${prod.reviewCount})",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Price lines & Cart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${prod.discountPrice.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "₹${prod.originalPrice.toInt()}",
                        fontSize = 9.sp,
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                // Small add button
                Button(
                    onClick = { viewModel.addToCart(prod) },
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .width(36.dp)
                        .testTag("catalog_cart_btn_${prod.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Quick Add", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
