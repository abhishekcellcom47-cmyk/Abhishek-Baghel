package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val description: String,
    val originalPrice: Double,
    val discountPrice: Double,
    val imageUrl: String,
    val stockAvailability: Int,
    val isNewArrival: Boolean = false,
    val isBestSeller: Boolean = false,
    val isTrending: Boolean = false,
    val rating: Float = 4.2f,
    val reviewCount: Int = 12
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: Long, // Matches product id
    val productName: String,
    val originalPrice: Double,
    val discountPrice: Double,
    val imageUrl: String,
    val category: String,
    val quantity: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val orderId: Long = 0,
    val addressFullName: String,
    val addressPhone: String,
    val addressLine: String,
    val paymentMethod: String,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending", // Pending, Confirmed, Shipped, Delivered
    val itemsSummary: String,
    val isCod: Boolean = true
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val addressId: Long = 0,
    val fullName: String,
    val phone: String,
    val area: String,
    val landmark: String,
    val city: String,
    val state: String,
    val pincode: String,
    val label: String // Home, Work
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val phone: String,
    val fullName: String,
    val email: String
)

@Entity(tableName = "product_reviews")
data class ProductReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val userName: String,
    val userRating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey val id: Long // Matches product id
)
